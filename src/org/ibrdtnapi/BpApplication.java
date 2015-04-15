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


/**
 * 
 * To create an BpApp you will have to
 * extend this class and override "bundleReceived()".
 * 
 * To create a BpApp you need to choose an endpoint (name).
 * If your local node is name "dtn://localnode" and you want
 * the application to have an EID of "dtn://localnode/MyAppEID"
 * create the BpApp as it:
 * <code>
 * 		MyApp myApp = new MyApp("MyAppEID");
 * </code>
 * where MyApp is a class extending BpApplication.
 *
 */
public abstract class BpApplication implements Observer {
	private FifoBundleQueue receivedBundles = null;
	private FifoBundleQueue toSendBundles = new FifoBundleQueue();

	public BpApplication(String eid) {
		if(eid == null || eid.contains(" ")) throw new ApiException("The endpoint must be not null and not contain any space.");
		Dispatcher dispatcher = new Dispatcher(toSendBundles, this, eid);
		this.toSendBundles.addObserver(dispatcher);
		this.receivedBundles = dispatcher.getReceivedBundles();
	}

	public boolean send(Bundle bundle) {
		return this.toSendBundles.enqueue(bundle);
	}
	
	@Override
	public void update(Observable receivedBundles, Object o) {
		if(this.receivedBundles == receivedBundles && !this.receivedBundles.isEmpty())
			this.bundleReceived(((FifoBundleQueue)receivedBundles).dequeue());
	}

	protected abstract void bundleReceived(Bundle b);
}
