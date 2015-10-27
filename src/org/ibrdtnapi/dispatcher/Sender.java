package org.ibrdtnapi.dispatcher;

import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;

/**
 * 
 * This class is used to send bundle.
 * It sends only bundle with a single block.
 * 
 * It is launched by {@link Dispatcher}.
 *
 */
public class Sender implements Runnable {
	private static final Logger log = Logger.getLogger(Sender.class.getName());
	private CommunicatorOutput communicatorOutput = null;
	private Dispatcher dispatcher = null;
	private Bundle bundle = null;

	public Sender(Dispatcher dispatcher, CommunicatorOutput communicatorOutput,	Bundle bundle) {
		this.communicatorOutput = communicatorOutput;
		this.dispatcher = dispatcher;
		this.bundle = bundle;
	}

	@Override
	public void run() {
		final int FirstBlock = 0;
		Sender.log.fine("Sending started with:" + this.bundle);
		//Request the daemon to send a bundle:
		this.communicatorOutput.query("bundle put plain");
		while(this.dispatcher.getState() != State.PUTTING) { Api.sleepWait(); };
		this.communicatorOutput.query("Source: api:me");
		this.communicatorOutput.query("Destination: " + this.bundle.getDestination());
		this.communicatorOutput.query("Processing flags: "+ this.bundle.getFlags());
		this.communicatorOutput.query("Timestamp: " + ((this.bundle.getTimestamp() == 0) ? System.currentTimeMillis() : this.bundle.getTimestamp()));
		String reportto = (this.bundle.getReportto() != null) ? this.bundle.getReportto() : "dtn:none"; 
		this.communicatorOutput.query("Reportto: " + reportto );
		String custodian = (this.bundle.getCustodian() != null) ? this.bundle.getCustodian() : "dtn:none"; 
		this.communicatorOutput.query("Custodian: " + custodian );
		int lifetime = (this.bundle.getLifetime() != 0) ? this.bundle.getLifetime() : Api.DEFAULT_LIFETIME; 
		this.communicatorOutput.query("Lifetime: " + lifetime);
		this.communicatorOutput.query("Sequencenumber: " + this.bundle.getSequencenumber());
		// If you set the number of blocks you want to send, the daemon will wait for all blocks to be appended.
		// This API _put_ a bundle with a single block, *then* adds blocks to it.
		this.communicatorOutput.query("Blocks: 1");// + this.bundle.getNumberOfBlocks());
		this.communicatorOutput.query("");
		this.communicatorOutput.query("Block: 1");
		this.communicatorOutput.query("Flags: LAST_BLOCK");
		this.communicatorOutput.query("Length: " + this.bundle.getLength(FirstBlock));
		this.communicatorOutput.query("");
		this.communicatorOutput.query(this.bundle.getEncoded(FirstBlock));
		this.communicatorOutput.query("");
		this.communicatorOutput.query("");
		this.communicatorOutput.query("");
		this.communicatorOutput.query("");
		while(this.dispatcher.getState() != State.BDL_REGISTERED) { Api.sleepWait(); };

		//Add other payload blocks (if any):
		if(this.bundle.getNumberOfBlocks() > 1) {
			for(int i = 1; i < this.bundle.getNumberOfBlocks(); i++) {
				this.communicatorOutput.query("bundle block add 0");
				while(this.dispatcher.getState() != State.BDL_BLOCK_ADDING) { Api.sleepWait(); };
				this.communicatorOutput.query("Block: 1");
				this.communicatorOutput.query("Length: " + this.bundle.getLength(i));
				this.communicatorOutput.query("");
				this.communicatorOutput.query(this.bundle.getEncoded(i));
				this.communicatorOutput.query("");
				this.communicatorOutput.query("");
				while(this.dispatcher.getState() != State.BLOCK_ADDED) { Api.sleepWait(); };
			}
		}

		this.communicatorOutput.query("bundle send");
		while(this.dispatcher.getState() != State.BDL_SENT) { Api.sleepWait(); };

		Sender.log.fine("Sending finished with:" + this.bundle);
	}
}
