package org.ibrdtnapi.dispatcher;

import org.ibrdtnapi.dispatcher.Dispatcher.State;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

public class FetcherLauncher implements Runnable {
	private Bundle bundle = null;
	private FifoBundleQueue toFetchBundles = null;
	private Dispatcher dispatcher = null;

	public static FetcherLauncher getInstance(Bundle bundle, FifoBundleQueue toFetchBundles, Dispatcher dispatcher) {
		while(dispatcher.getState() != State.IDLE);
		return new FetcherLauncher(bundle, toFetchBundles, dispatcher);
		
	}
	
	private FetcherLauncher(Bundle bundle, FifoBundleQueue toFetchBundles, Dispatcher dispatcher) {
		this.bundle = bundle;
		this.toFetchBundles = toFetchBundles;
		this.dispatcher = dispatcher;
	}

	@Override
	public void run() {
		//Wait for the dispatcher to be ready to fetch the next bundle
		this.toFetchBundles.enqueue(this.bundle);
	}

}
