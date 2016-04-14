package org.ibrdtnapi.dispatcher;

import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;

/**
 * 
 * This class is used to fetch (download) bundles
 * from the daemon. {@link Dispatcher} launch it
 * each time the {@link CommunicatorInput} notifies
 * a new bundle.
 * 
 * Note that all bundle received are required to have
 * only one single block. Otherwise the behavior may
 * differ (or crash depending on your luck).
 *
 */
public class Fetcher implements Runnable {
	private static final Logger log = Logger.getLogger(Fetcher.class.getName());
	private CommunicatorOutput communicatorOutput = null;
	private CommunicatorInput communicatorInput = null;
	private Dispatcher dispatcher = null;
	private Bundle bundle = null;

	public Fetcher(Dispatcher dispatcher,CommunicatorOutput communicatorOutput, CommunicatorInput communicatorInput, Bundle bundle) {
		synchronized(Api.lockFetcher) {
			this.communicatorOutput = communicatorOutput;
			this.communicatorInput = communicatorInput;
			this.dispatcher = dispatcher;
			this.bundle = bundle;
		}
	}

	@Override
	public void run() {
		synchronized(Api.lockFetcher) {
			Fetcher.log.fine("Fetching started with:" + this.bundle);
			//Request to load the bundle into the register:
			this.communicatorOutput.query("bundle load " + this.bundle.getTimestamp() + " " + this.bundle.getSequencenumber() + " " + this.bundle.getSource());
			while(this.dispatcher.getState() != State.BDL_LOADED) { Api.sleepWait(); };
			//Request to load the meta-data
			this.communicatorOutput.query("bundle info");
			//Wait for the INFO to be sent:
			while(this.dispatcher.getState() != State.INFO_BUFFERED) { Api.sleepWait(); };
			//Parse the meta-data
			String buffer = new String(this.communicatorInput.getBuffer());
			//Fetcher.log.finest("Buffer\n::::::::::::::::::\n" + buffer + "\n::::::::::::::::::");
			String[] meta = buffer.split("\n");
			int numberOfBlocks = 1;
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
				else if(s.startsWith("Blocks:"))
					numberOfBlocks = Integer.parseInt(s.split(" ")[1]);
			}
			for(int payloadBlock = 0; payloadBlock < numberOfBlocks; payloadBlock++) {
				//Request to load the payload (base64 encoded)
				this.communicatorOutput.query("payload " + payloadBlock +" get 0 0");
				while(this.dispatcher.getState() != State.PLD_BUFFERED) { Api.sleepWait(); };
				//Set the encoded payload to the bundle
				buffer = new String(this.communicatorInput.getBuffer());
				String[] payloadBuffer = buffer.split("\n");

				StringBuilder encoded =  new StringBuilder();
				final int payloadStartingLine = 2;
				for (int line = payloadStartingLine; line < payloadBuffer.length; line++) {
					String s = payloadBuffer[line].trim();
					if (!s.isEmpty()) {
						encoded.append(s);
					}
				}

				this.bundle.addEncoded(encoded.toString());
				this.dispatcher.setState(State.PLD_CONSUMED);
			}

			//Set the bundle to the dispatcher, so the dispatcher can add it in the Fifo for the app
			this.dispatcher.setFetchingBundle(this.bundle);
			this.dispatcher.setState(State.BDL_READY);
			//Request to mark the bundle as delivered so the CommunicatorInput will set the dispatcher's state to BDL_READY
			this.communicatorOutput.query("bundle delivered " + this.bundle.getTimestamp() + " " + this.bundle.getSequencenumber() + " " + this.bundle.getSource());
		}
	}
}
