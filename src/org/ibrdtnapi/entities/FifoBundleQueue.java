/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.dispatcher.Dispatcher;

/**
 * 
 * The Fifo is used to communicate between the
 * {@link Dispatcher} and the {@link BpApplication}
 * to send bundles to each other. One way for the
 * received bundles, the other way for the bundle to send.
 *
 */
public class FifoBundleQueue extends Observable {
	private ConcurrentLinkedQueue<Bundle> bundles = new ConcurrentLinkedQueue<Bundle>();

	public FifoBundleQueue() {
	}

	public synchronized boolean enqueue(Bundle bundle) {
		if(bundle == null) return false;
		boolean ret = this.bundles.add(bundle);
		if(ret) {
			setChanged();
			notifyObservers();
		}
		return ret;
	}

	public synchronized Bundle dequeue() {
		return this.bundles.poll();
	}

	public synchronized boolean isEmpty() {
		return this.bundles.isEmpty();
	}
}
