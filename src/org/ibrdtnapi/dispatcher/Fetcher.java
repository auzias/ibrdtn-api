package org.ibrdtnapi.dispatcher;

import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;

public class Fetcher implements Runnable {
	private CommunicatorOutput communicatorOutput = null;
	private CommunicatorInput communicatorInput = null;
	private Dispatcher dispatcher = null;
	private Bundle bundle = null;

	public Fetcher(Dispatcher dispatcher,CommunicatorOutput communicatorOutput, CommunicatorInput communicatorInput, Bundle bundle) {
		this.communicatorOutput = communicatorOutput;
		this.communicatorInput = communicatorInput;
		this.dispatcher = dispatcher;
		this.bundle = bundle;
	}

	@Override
	public void run() {
		this.communicatorOutput.query("bundle load " + this.bundle.getTimestamp() + " " + this.bundle.getBlockNumber() + " " + this.bundle.getSource());
		while(this.dispatcher.getState() != State.BDL_LOADED);
		this.communicatorOutput.query("bundle info");
		while(this.dispatcher.getState() != State.INFO_BUFFERED);
		String buffer = this.communicatorInput.getBuffer();
		
		System.out.print(">>>>>>>" + buffer + "<<<<<<");

	}

}
