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
import java.util.Observable;
import java.util.Observer;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.DaemonException;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

public class Dispatcher implements Observer {
	private Dispatcher.State state = State.DISCONNECTED;
	private FifoBundleQueue toFetchBundles = null;
	private FifoBundleQueue receivedBundles = new FifoBundleQueue();
	private FifoBundleQueue toSendBundles = null;
	private CommunicatorInput communicatorInput = null;
	private CommunicatorOutput communicatorOutput = null;
	private Socket socket = null;
	private String eid = null;
	private Bundle fetchingBundle = null;

	public Dispatcher(FifoBundleQueue toSendBundles, BpApplication application, String eid) {
		this.toSendBundles = toSendBundles;
		this.receivedBundles.addObserver(application);
		this.eid = eid;
		this.connect(0, eid);
	}

	private void connect(int port, String eid) {
		port = (port == 0) ? Api.DEFAULT_PORT : port;
		try {
			this.socket = new Socket("127.0.0.1", port);
		} catch (Exception e) {
			throw new DaemonException("Cannot establish connection. Is IBR-DTN daemon running? Is it accessible on 127.0.0.1:" + port + "? " + e.getMessage());
		}
		this.setState(State.CONNECTED);
		
		//Create CommunicatorInput and Output
		try {
			//Bind the input stream of the socket to a BufferReader.
			BufferedReader br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			//Create the CommunicatorInput (that log and parse input from the Daemon) with the BufferReader. We also pass the Communicator itself so CommunicatorInput can change its State.
			this.communicatorInput = new CommunicatorInput(br, this);
			//The CommunicatorInput has the notification of received bundles. We need to set this Fifo to the dispatcher (observer).
			this.toFetchBundles = this.communicatorInput.getToFetchBundles();
			//We start the Thread communicatorInput that will log and parse input. 
			new Thread(this.communicatorInput).start();

			//Bind the output stream of the socket to a dataOutputStream.
			DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
			this.communicatorOutput = new CommunicatorOutput(dos);
		} catch (IOException e) {
			throw new DaemonException("Could not set the bufferReader or the DataOutputStream. " + e.getMessage());
		}

		//Once the connection is established with the daemon, we initiate the protocol extended to receive/send bundles
		this.initEndpoint(eid);
	}

	private void initEndpoint(String eid) {
		this.communicatorOutput.query("protocol extended");
		//Wait for confirmation
		while(this.getState() != State.EXTENDED && this.getState() != State.ERROR);
		if(this.getState() == State.ERROR) throw new DaemonException("Could not initiate the extended protocol.");
		//If protocol extended is initiated we set an endpoint (if it's a singleton) xor add a registration (if it's not a singleton -and the EID is a group EID-)
		if(this.getState() == State.EXTENDED) {
			String query = (eid.contains(Api.NOT_SINGLETON) ? "registration add " : "set endpoint ") + eid + "\n";
			this.communicatorOutput.query(query);
		}
	}

	@Override
	public void update(Observable obs, Object o) {
		if(this.toSendBundles == obs) {
			//TODO: send the bundle
			System.out.println("Bundle to send:" + this.toSendBundles.dequeue());
		} else if(this.toFetchBundles == obs) {
			//TODO: fetch the bundle and add it in receivedBundles
			this.fetch(this.toFetchBundles.dequeue());
		} else {
			System.err.println("An error occured on Dispatcher::update.");
		}
	}

	private void fetch(Bundle bundle) {
		new Thread(new Fetcher(this, this.communicatorOutput, this.communicatorInput, bundle)).start();
	}

	public FifoBundleQueue getReceivedBundles() {
		return receivedBundles;
	}

	

	public void setFetchingBundle(Bundle fetchingBundle) {
		this.fetchingBundle = fetchingBundle;
	}

	public synchronized Dispatcher.State getState() {
		return state;
	}

	public synchronized void setState(Dispatcher.State state) {
		this.state = state;
	}

	public enum State {
		ERROR(-2),			//An error occurred
		DISCONNECTED(-1),	//Socket is not yet established
		CONNECTED(0),		//Socket is established with the daemon
		EXTENDED(1),		//The query 'protocol extended' returned '200 SWITCHED TO EXTENDED'
		SENDING(2),			//The Communicator is sending bundle 
		BDL_LOADED(3),		//The Communicator is fetching bundle
		BDL_FETCHED(4),		//The Communicator is fetching bundle
		INFO_BUFFERED(5);	//The Communicator is buffered info bundle
		public final int value;

		State(int value) {
			this.value = value;
		}
	}
}
