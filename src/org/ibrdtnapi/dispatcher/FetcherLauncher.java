package org.ibrdtnapi.dispatcher;

import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

/**
 * 
 * {@link CommunicatorInput} HAVE TO always listen the socket.
 * That is why a thread is launched for each notified bundle,
 * so it does not block the {@link CommunicatorInput}.
 * 
 * The FetcherLauncher actually notifies {@link Dispatcher},
 * which then launches {@link Fetcher} to download the bundle.
 *
 */
public class FetcherLauncher implements Runnable {
	private Bundle bundle = null;
	private FifoBundleQueue toFetchBundles = null;

	public FetcherLauncher(Bundle bundle, FifoBundleQueue toFetchBundles) {
		this.bundle = bundle;
		this.toFetchBundles = toFetchBundles;
	}

	@Override
	public void run() {
		//Wait for the dispatcher to be ready to fetch the next bundle
		this.toFetchBundles.enqueue(this.bundle);
	}

}
