/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

import java.util.Observable;
import java.util.Observer;

import org.ibrdtnapi.dispatcher.Dispatcher;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

public abstract class BpApplication implements Observer {
	private FifoBundleQueue receivedBundles = null;
	private FifoBundleQueue toSendBundles = new FifoBundleQueue();

	public BpApplication(String eid) {
		if(eid == null || eid.contains(" ")) throw new ApiException("The endpoint must be not null and not contain any space.");
		Dispatcher dispatcher = new Dispatcher(toSendBundles, this, eid);
		this.toSendBundles.addObserver(dispatcher);
		this.receivedBundles = dispatcher.getReceivedBundles();
	}

	public boolean send(Bundle b) {
		return this.toSendBundles.enqueue(b);
	}
	
	@Override
	public void update(Observable receivedBundles, Object o) {
		if(this.receivedBundles == receivedBundles && !this.receivedBundles.isEmpty())
			this.bundleReceived(((FifoBundleQueue)receivedBundles).dequeue());
	}

	protected abstract void bundleReceived(Bundle b);
}
