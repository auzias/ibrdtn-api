package org.ibrdtnapi.dispatcher;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.ApiException;
import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

/**
 * 
 * The input stream from the socket with the daemon
 * is processed by this class.
 * It log everything, by default (hard-coded) in a file.
 * If the file cannot be open, it logs in the ... log at
 * (info-level).
 * 
 * This class, among others, changes the state of the
 * {@link Dispatcher}. All return-codes from the daemon
 * are received by the {@link CommunicatorInput}. 
 *
 */
public class CommunicatorInput implements Runnable {
	private static final Logger log = Logger.getLogger(CommunicatorInput.class.getName());
	private BufferedReader br = null;
	private FifoBundleQueue toFetchBundles = new FifoBundleQueue();
	private Dispatcher dispatcher = null;
	private StringBuilder buffer = null;
	private List<String> neighborList = new ArrayList<String>();


	public CommunicatorInput(BufferedReader br, Dispatcher dispatcher) {
		this.br = br;
		this.dispatcher = dispatcher;
		this.dispatcher.setState(State.CONNECTED);
		this.toFetchBundles.addObserver(this.dispatcher);
		synchronized(Api.lockFile) {
			try {
				//Open and trunk the file.
				Api.logFile = new FileWriter(Api.LOG_FILE_PATH);
				Api.logFile.close();
			} catch (IOException e) {
				CommunicatorInput.log.info("Could not open the file '" + Api.LOG_FILE_PATH + "' to write it. Log will be display at finest level.");
			}
		}
	}

	public FifoBundleQueue getToFetchBundles() {
		return this.toFetchBundles;
	}

	@Override
	public void run() {
		String str;
		final int payloadLinesToBufferize = 2;
		int emptyLinesCount = 0;
		try {
			while ((str = this.br.readLine()) != null) {
				this.log(str);
				//First thing first, we check if it's a 602 notify
				if(str.startsWith("602 NOTIFY BUNDLE")) {
					this.notifyBundle(str);
				//Otherwise, if bundle is loaded (and info are sent), bufferize them:
				}  else if(this.dispatcher.getState() == State.BDL_LOADED) {
					this.buffer.append(str + "\n");
					if(str.startsWith("Blocks: "))
						this.dispatcher.setState(State.INFO_BUFFERED);
				//Otherwise, if payload is sent, bufferize it:
				}  else if(this.dispatcher.getState() == State.PLD_BUFFERING) {
					//If the line is empty, count it
					if (str.trim().isEmpty()) {
						emptyLinesCount++;
						if(emptyLinesCount == payloadLinesToBufferize) {
							this.dispatcher.setState(State.PLD_BUFFERED);
							emptyLinesCount = 0;
						}
					} else {
						this.buffer.append(str + '\n');
					}
				//Otherwise, the neighbor list has been requested:
				} else if(this.dispatcher.getState() == State.NEIGHBOR_LIST) {
					//If the line is not empty, we add this neighbor
					if(!str.trim().isEmpty()) {
						this.neighborList.add(str);
					//Otherwise, if the line is empty, the listing is done.
					} else {
						this.dispatcher.setState(State.NEIGHBOR_LISTED);
					}
				} else {
					this.parse(str);
				}
			}
		} catch (IOException e) {
			CommunicatorInput.log.severe("CommunicatorInput Interrupted. " + e.getMessage());
		}
	}

	private void log(String str) {
		try {
			synchronized(Api.lockFile) {
				Api.logFile = new FileWriter(Api.LOG_FILE_PATH, Api.APPEND);
				Api.logFile.append(str + "\n");
				Api.logFile.flush();
				Api.logFile.close();
			}
		} catch (IOException e) {
			CommunicatorInput.log.info("Could not open the file '" + Api.LOG_FILE_PATH + "' to write it. Log will be display at finest level.");
			CommunicatorInput.log.finest(str);
		}
	}

	public String getBuffer() {
		String ret = this.buffer.toString();
		this.buffer = new StringBuilder();//Clear the buffer
		return ret;
	}

	public List<String> getNeighborList() {
		List<String> list = new ArrayList<String>();
		list.addAll(this.neighborList);
		this.neighborList.clear();
		return list;
	}

	private void parse(String str) {
		if(str == null) {
			throw new ApiException("Input string null.");
		} else if("200 SWITCHED TO EXTENDED".equals(str)) {
			this.dispatcher.setState(Dispatcher.State.EXTENDED);
		} else if("200 OK".equals(str) && this.dispatcher.getState() == State.EXTENDED) {
			this.dispatcher.setState(Dispatcher.State.IDLE);
		} else if(str.startsWith("200 BUNDLE LOADED")) {
			this.dispatcher.setState(State.BDL_LOADED);
			this.buffer = new StringBuilder();//Clear the buffer
		} else if(str.startsWith("200 BUNDLE DELIVERED ACCEPTED")) {
			this.dispatcher.setState(State.BDL_DELIVERED);
		} else if(str.startsWith("100 PUT BUNDLE PLAIN")) {
			this.dispatcher.setState(State.PUTTING);
		} else if(str.startsWith("200 BUNDLE IN REGISTER")) {
			this.dispatcher.setState(State.BDL_REGISTERED);
		} else if(str.startsWith("200 BUNDLE SENT")) {
			this.dispatcher.setState(State.BDL_SENT);
		} else if(str.startsWith("200 BUNDLE INFO")) {
			this.dispatcher.setState(State.BDL_INFO);
		} else if(str.startsWith("200 PAYLOAD GET")) {
			this.dispatcher.setState(State.PLD_BUFFERING);
		} else if(str.startsWith("200 NODENAME")) {
			this.dispatcher.setNodeName(str.split(" ")[2]);
		} else if(str.startsWith("100 BUNDLE BLOCK ADD")) {
			this.dispatcher.setState(State.BDL_BLOCK_ADDING);
		} else if(str.startsWith("200 BUNDLE BLOCK ADD SUCCESSFUL")) {
			this.dispatcher.setState(State.BLOCK_ADDED);
		} else if(str.startsWith("200 NEIGHBOR LIST")) {
			this.dispatcher.setState(State.NEIGHBOR_LIST);
		}
	}

	private void notifyBundle(String str) {
		Thread fetcherLauncher = new Thread(new FetcherLauncher(this.bundleNotified(str), this.toFetchBundles));
		fetcherLauncher.setName("Fetcher launcher");
		this.dispatcher.addFetcher(fetcherLauncher);
	}

	private Bundle bundleNotified(String str) {
		String[] parsed = str.split(" ");
		long timestamp = Long.parseLong(parsed[3]);
		int blockNumber = Integer.parseInt(parsed[4]);
		String source = parsed[5];
		Bundle bundle = new Bundle(timestamp, blockNumber, source, null);
		return bundle;
	}
}
