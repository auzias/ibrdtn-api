package org.ibrdtnapi.dispatcher;

import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

public class FetcherLauncher implements Runnable {
	private Bundle bundle = null;
	private FifoBundleQueue toFetchBundles = null;

	
	public FetcherLauncher(Bundle bundle, FifoBundleQueue toFetchBundles) {
		this.bundle = bundle;
		this.toFetchBundles = toFetchBundles;
	}

	@Override
	public void run() {
		this.toFetchBundles.enqueue(this.bundle);
	}

}
