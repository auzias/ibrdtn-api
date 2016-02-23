/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.ibrdtnapi.dispatcher.Dispatcher;
import org.ibrdtnapi.entities.Bundle;
import org.ibrdtnapi.entities.FifoBundleQueue;

/**
 * 
 * To create an BpApp you will have to extend this class and override
 * "bundleReceived()".
 * 
 * To create a BpApp you need to choose an endpoint (name). If your local node
 * is name "dtn://localnode" and you want the application to have an EID of
 * "dtn://localnode/MyAppEID" create the BpApp as it: <code>
 * 		MyApp myApp = new MyApp("MyAppEID");
 * </code> where MyApp is a class extending BpApplication.
 *
 */
public class BpApplication implements Observer {
	private FifoBundleQueue receivedBundles = null;
	private FifoBundleQueue toSendBundles = new FifoBundleQueue();
	private Dispatcher dispatcher = null;
	private BundleHandler handler = null;
	private ArrayList<Bundle> pendingBundle = new ArrayList<Bundle>();

	public BpApplication() {

	}

	public void setEid(String eid) {
		if (eid == null || eid.contains(" "))
			throw new ApiException(
					"The endpoint must be not null and not contain any space.");
		this.dispatcher = new Dispatcher(toSendBundles, this, eid);
		this.toSendBundles.addObserver(dispatcher);
		this.receivedBundles = dispatcher.getReceivedBundles();
	}

	public void setHandler(BundleHandler handler) {
		this.handler = handler;
		for (Bundle bundle : pendingBundle)
			handler.onReceive(bundle);
	}

	public BpApplication(String eid) {
		this.setEid(eid);
	}

	public boolean send(Bundle bundle) {
		if (this.dispatcher == null || bundle == null)
			return false;
		// else the bundle is copied and added to the queue.
		Bundle bdl = new Bundle(bundle);
		return this.toSendBundles.enqueue(bdl);
	}

	@Override
	public void update(Observable receivedBundles, Object o) {
		if (this.receivedBundles == receivedBundles
				&& !this.receivedBundles.isEmpty())
			this.bundleReceived(((FifoBundleQueue) receivedBundles).dequeue());
	}

	public void stop() {
		while (this.dispatcher.getState() != Dispatcher.State.IDLE) {
			Api.sleepWait();
		};
		this.dispatcher.stop();
	}

	public List<String> getNeighborList() {
		//The list, as the buffer is cleared, is copied.
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(this.dispatcher.getNeighborList());
		return list;
	}

	public String getEid() {
		return this.dispatcher.getEid();
	}

	public String getNodeName() {
		return this.dispatcher.getNodeName();
	}

	public String getURI() {
		return this.dispatcher.getNodeName() + "/" + this.dispatcher.getEid();
	}

	public void bundleReceived(Bundle b) {
		if (this.handler != null)
			this.handler.onReceive(b);
		else
			this.pendingBundle.add(b);
	}
}
