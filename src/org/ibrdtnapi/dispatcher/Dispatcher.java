/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.dispatcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.ApiException;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;


/**
 * 
 * The {@link Dispatcher} process bundles to download
 * (from the daemon) and bundles to send. 
 * It manages all the communication with the daemon to
 * ease the {@link BpApplication} usage.
 * 
 * Due to the shared-nature of the socket (to send *and*
 * receive bundles) some states has been defined to fetch
 * xor send bundles only if the dispatcher is idle. If the
 * {@link Dispatcher} tries to send a bundle while downloading
 * one, the commands would mix-up and the return-codes from
 * the daemon would be too messy to treat.
 * 
 * Anyway, it's fast enough, and no deadlock (as far as I tested it).
 *
 */
public class Dispatcher implements Observer {
	private static final Logger log = Logger.getLogger(Dispatcher.class.getName());
	private volatile Dispatcher.State state = State.DISCONNECTED;
	private ExecutorService bundleFetcherExecutor = Executors.newSingleThreadExecutor();
	private FifoBundleQueue toFetchBundles = null;
	private FifoBundleQueue receivedBundles = new FifoBundleQueue();
	private FifoBundleQueue toSendBundles = null;
	private FifoBundleQueue pendingBundles = new FifoBundleQueue();
	private CommunicatorInput communicatorInput = null;
	private CommunicatorOutput communicatorOutput = null;
	private Socket socket = null;
	private Bundle fetchingBundle = null;
	private String eid = "";
	private volatile String nodeName = null;

	public Dispatcher(FifoBundleQueue toSendBundles, BpApplication application, String eid) {
		this.toSendBundles = toSendBundles;
		this.receivedBundles.addObserver(application);
		this.eid = eid;
		this.connect(0, this.eid);
	}

	private void connect(int port, String eid) {
		port = (port == 0) ? Api.DEFAULT_PORT : port;
		try {
			this.socket = new Socket("127.0.0.1", port);
		} catch (Exception e) {
			throw new ApiException("Cannot establish connection. Is IBR-DTN daemon running? Is it accessible on 127.0.0.1:" + port + "? " + e.getMessage());
		}

		//Create CommunicatorInput and Output
		try {
			//Bind the input stream of the socket to a BufferReader.
			BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			//Create the CommunicatorInput (that log and parse input from the Daemon) with the BufferReader. We also pass the Communicator itself so CommunicatorInput can change its State.
			this.communicatorInput = new CommunicatorInput(br, this);
			//The CommunicatorInput has the notification of received bundles. We need to set this Fifo to the dispatcher (observer).
			this.toFetchBundles = this.communicatorInput.getToFetchBundles();
			//We start the Thread communicatorInput that will log and parse input. 
			Thread threadCommInput = new Thread(this.communicatorInput);
			threadCommInput.setName("CommunicatorInputThread");
			threadCommInput.start();
			//Bind the output stream of the socket to a dataOutputStream.
			DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
			this.communicatorOutput = new CommunicatorOutput(dos);
		} catch (IOException e) {
			throw new ApiException("Could not set the bufferReader or the DataOutputStream. " + e.getMessage());
		}

		//Once the connection is established with the daemon, we initiate the protocol extended to receive/send bundles
		this.initEndpoint(eid);
	}

	private void initEndpoint(String eid) {
		while(this.getState() != State.CONNECTED) { Api.sleepWait(); };
		this.communicatorOutput.query("protocol extended");
		//Wait for confirmation
		while(this.getState() != State.EXTENDED && this.getState() != State.ERROR) { Api.sleepWait(); };
		if(this.getState() == State.ERROR) throw new ApiException("Could not initiate the extended protocol.");
		this.communicatorOutput.query("nodename");
		while(this.nodeName == null);
		//If protocol extended is initiated we set an endpoint (if it's a singleton) xor add a registration (if it's not a singleton -and the EID is a group EID-)
		if(this.getState() == State.EXTENDED) {
			String query = (eid.contains(Api.NOT_SINGLETON) ? "registration add " : "set endpoint ") + eid + "\n";
			this.communicatorOutput.query(query);
		}
		while(this.getState() != State.IDLE) { Api.sleepWait(); };
	}

	@Override
	public void update(Observable obs, Object o) {
		if(this.toSendBundles == obs) {
			this.send(this.toSendBundles.dequeue());
		} else if(!this.toFetchBundles.isEmpty()) {
			this.fetch(this.toFetchBundles.dequeue());
		} else {
			System.err.println("An error occured on Dispatcher::update.");
		}
	}

	private synchronized void send(Bundle bundle) {
		//Wait to be ready to send next bundle
		while(this.getState() != State.IDLE) { Api.sleepWait(); };
		do{
			Thread threadSender = new Thread(new Sender(this, this.communicatorOutput, bundle));
			threadSender.setName("Sender Thread");
			threadSender.start();
			while(this.getState() != State.BDL_SENT) { Api.sleepWait(); };
		}while(!this.toSendBundles.isEmpty());
		this.setState(State.IDLE);
	}

	private synchronized void fetch(Bundle bundle) {
		//Wait to be ready to fetch next bundle
		while(this.getState() != State.IDLE) { Api.sleepWait(); };
		//Fetch all bundle
		do{
			this.setState(State.FETCHING);
			Thread threadFetcher = new Thread(new Fetcher(this, this.communicatorOutput, this.communicatorInput, bundle));
			threadFetcher.setName("Fetcher Thread");
			threadFetcher.start();
			while(this.getState() != State.BDL_READY && this.getState() != State.BDL_DELIVERED) { Api.sleepWait(); };
			this.receivedBundles.enqueue(this.fetchingBundle);
			while(this.getState() != State.BDL_DELIVERED) { Api.sleepWait(); };
		}while(!this.toFetchBundles.isEmpty());
		this.setState(State.IDLE);
	}

	public FifoBundleQueue getReceivedBundles() {
		return receivedBundles;
	}

	public void setFetchingBundle(Bundle fetchingBundle) {
		this.fetchingBundle = fetchingBundle;
	}

	public void addFetcher(Runnable task) {
		this.bundleFetcherExecutor.execute(task);
	}

	public Dispatcher.State getState() {
		synchronized(this.state) {
			return state;
		}
	}

	public void setState(Dispatcher.State state) {
		synchronized(this.state){
			Dispatcher.log.fine("Dispatcher State changed from " + this.state);
			this.state = state;
			Dispatcher.log.fine(" to " + this.state);
		}
	}

	public enum State {
		ERROR(-2),			//An error occurred
		DISCONNECTED(-1),	//Socket is not yet established

		CONNECTED(0),		//Socket is established with the daemon
		EXTENDED(1),		//The query 'protocol extended' returns '200 SWITCHED TO EXTENDED'
		IDLE(2),
		FETCHING(3),
		FETCHING_READY(4),

		BDL_LOADED(10),		//The Communicator is fetching bundle
		INFO_BUFFERED(11),
		PLD_BUFFERED(12),
		BDL_READY(13),
		BDL_DELIVERED(14),
		BLOCK_ADDED(15),
		BDL_INFO(16),
		PLD_BUFFERING(17),
		PLD_CONSUMED(18),

		SENDING(20),
		PUTTING(21),
		BDL_REGISTERED(22),
		BDL_SENT(23),
		BDL_BLOCK_ADDING(24),

		NEIGHBOR_LIST(30),
		NEIGHBOR_LISTED(31);

		public final int value;

		State(int value) {
			this.value = value;
		}

		public String toString() {
			switch (this.value) {
			case -2: return "ERROR";
			case -1: return "DISCONNECTED";
			case  0: return "CONNECTED";
			case  1: return "EXTENDED";
			case  2: return "IDLE";
			case  3: return "FETCHING";
			case  4: return "FETCHING_READY";

			case 10: return "BDL_LOADED";
			case 11: return "INFO_BUFFERED";
			case 12: return "PLD_BUFFERED";
			case 13: return "BDL_READY";
			case 14: return "BDL_DELIVERED";
			case 15: return "BLOCK_ADDED";
			case 16: return "BDL_INFO";
			case 17: return "PLD_BUFFERING";
			case 18: return "PLD_CONSUMED";

			case 20: return "SENDING";
			case 21: return "PUTTING";
			case 22: return "BDL_REGISTERED";
			case 23: return "BDL_SENT";
			case 24: return "BDL_BLOCK_ADDING";

			case 30: return "NEIGHBOR_LIST";
			case 31: return "NEIGHBOR_LISTED";

			default: return ("UNKNOWN: " + this.value);
			}
		}
	}

	public void addPendingBundle(Bundle bundle) {
		this.pendingBundles.enqueue(bundle);
	}

	public void stop() {
		//Wait for the endpoint to be idle
		while(this.getState() != State.IDLE) { Api.sleepWait(); };

		if(this.socket != null)
		try {
			this.socket.close();
		} catch (IOException e) {
			Dispatcher.log.info("Socket of eid '" + this.eid + "' closed ");
		}
		this.nodeName = null;
		this.setState(State.DISCONNECTED);
	}

	public String getEid() {
		return this.eid;
	}

	public void setNodeName(String name) {
		this.nodeName = new String(name);
	}

	public String getNodeName() {
		return this.nodeName;
	}

	public List<String> getNeighborList() {
		//Wait for the endpoint to be idle
		while(this.getState() != State.IDLE) { Api.sleepWait(); };
		//Request the neighbor list
		this.communicatorOutput.query("neighbor list");
		//Wait for the list to be completed
		while(this.getState() != State.NEIGHBOR_LISTED) { Api.sleepWait(); };
		List<String> list = this.communicatorInput.getNeighborList();
		this.setState(State.IDLE);
		return list;
	}
}
