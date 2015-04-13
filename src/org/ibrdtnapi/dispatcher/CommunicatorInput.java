package org.ibrdtnapi.dispatcher;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.ApiException;
import org.ibrdtnapi.DaemonException;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;


public class CommunicatorInput implements Runnable {
	private static final Logger log = Logger.getLogger(CommunicatorInput.class.getName());
	private BufferedReader br = null;
	private FifoBundleQueue toFetchBundles = new FifoBundleQueue();
	private Dispatcher dispatcher = null;


	public CommunicatorInput(BufferedReader br, Dispatcher dispatcher) {
		this.br = br;
		this.dispatcher = dispatcher;
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
		try {
			while ((str = this.br.readLine()) != null) {
				this.parse(str);
				//Log the line:
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
		} catch (IOException e) {
			throw new DaemonException("Could not read from the socket. " + e.getMessage());
		}
		CommunicatorInput.log.exiting(CommunicatorInput.class.getName(), "run()");
	}

	private void parse(String str) {
		if(str == null) throw new ApiException("Input string null.");

		if("200 SWITCHED TO EXTENDED".equals(str) && this.dispatcher.getState() == Dispatcher.State.CONNECTED)
			this.dispatcher.setState(Dispatcher.State.EXTENDED);

		if(str.startsWith("602 NOTIFY BUNDLE")) { //i.e.: 602 NOTIFY BUNDLE 482241205 1 dtn://59/wfJQXpkXdWMBWUTv
			String[] parsed = str.split(" ");
			long timestamp = Long.parseLong(parsed[3]);
			int blockNumber = Integer.parseInt(parsed[4]);;
			String source = parsed[5];
			this.toFetchBundles.enqueue(new Bundle(timestamp, blockNumber, source, null, Bundle.State.TO_FETCH));
		}

		if(str.startsWith("200 BUNDLE LOADED")) {
			this.dispatcher.setState(Dispatcher.State.FETCHING);
		}
	}

}
