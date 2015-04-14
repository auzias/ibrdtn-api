package org.ibrdtnapi.dispatcher;

import java.util.logging.Logger;

import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;

public class Fetcher implements Runnable {
	private static final Logger log = Logger.getLogger(Fetcher.class.getName());
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
		Fetcher.log.fine("Fetching started with:" + this.bundle);
		this.communicatorOutput.query("bundle load " + this.bundle.getTimestamp() + " " + this.bundle.getBlockNumber() + " " + this.bundle.getSource());
		while(this.dispatcher.getState() != State.BDL_LOADED);
		this.communicatorOutput.query("bundle info");
		while(this.dispatcher.getState() != State.INFO_BUFFERED);
		String buffer = this.communicatorInput.getBuffer();
		String[] meta = buffer.split("\n");
		for(String s : meta) {
			if(s.startsWith("Processing flags:"))
				this.bundle.setFlags(Integer.parseInt(s.split(" ")[2]));
			else if(s.startsWith("Timestamp:"))
				this.bundle.setTimestamp(Long.parseLong(s.split(" ")[1]));
			else if(s.startsWith("Sequencenumber:"))
				this.bundle.setSequencenumber(Integer.parseInt(s.split(" ")[1]));
			else if(s.startsWith("Source:"))
				this.bundle.setSource(s.split(" ")[1]);
			else if(s.startsWith("Destination:"))
				this.bundle.setDestination(s.split(" ")[1]);
			else if(s.startsWith("Reportto:"))
				this.bundle.setReportto(s.split(" ")[1]);
			else if(s.startsWith("Custodian:"))
				this.bundle.setCustodian(s.split(" ")[1]);
			else if(s.startsWith("Lifetime:"))
				this.bundle.setLifetime(Integer.parseInt(s.split(" ")[1]));
			else if(s.startsWith("Length:"))
				this.bundle.setLength(Integer.parseInt(s.split(" ")[1]));
		}
		this.communicatorOutput.query("payload 0 get 0 " + this.bundle.getLength());
		while(this.dispatcher.getState() != State.PLD_BUFFERED);
		
		buffer = this.communicatorInput.getBuffer();
		this.bundle.setEncoded(buffer.split("\n")[4]);

		this.dispatcher.setFetchingBundle(this.bundle);
		this.dispatcher.setState(State.BDL_READY);
	}

}
