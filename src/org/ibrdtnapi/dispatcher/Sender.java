package org.ibrdtnapi.dispatcher;

import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.entities.Bundle;

public class Sender implements Runnable {
	private static final Logger log = Logger.getLogger(Fetcher.class.getName());
	private CommunicatorOutput communicatorOutput = null;
	private CommunicatorInput communicatorInput = null;
	private Dispatcher dispatcher = null;
	private Bundle bundle = null;

	public Sender(Dispatcher dispatcher, CommunicatorOutput communicatorOutput,	CommunicatorInput communicatorInput, Bundle bundle) {
		synchronized(Api.lockSender) {
			this.communicatorOutput = communicatorOutput;
			this.communicatorInput = communicatorInput;
			this.dispatcher = dispatcher;
			this.bundle = bundle;
		}
	}

	@Override
	public void run() {
		synchronized(Api.lockSender) {
			System.out.println("Sending" + this.bundle);
		}
	}
}
