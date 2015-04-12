/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

import java.util.Observable;
import java.util.Observer;

/**
 * @author auzias
 *
 */
public class Dispatcher implements Observer {
	private FifoQueue receivedBundles = new FifoQueue();
	private FifoQueue toSendBundles = null;

	public Dispatcher(FifoQueue toSendBundles, BpApplication application) {
		this.toSendBundles = toSendBundles;
		this.receivedBundles.addObserver(application);
	}

	@Override
	public void update(Observable toSendBundles, Object o) {
		System.out.println("notified");
		System.out.println("Bundle to send:" + ((FifoQueue)toSendBundles).dequeue());
	}

	public FifoQueue getReceivedBundles() {
		return receivedBundles;
	}

	public void simulateBundle(Bundle bundle) {
		this.receivedBundles.enqueue(bundle);
	}
}
