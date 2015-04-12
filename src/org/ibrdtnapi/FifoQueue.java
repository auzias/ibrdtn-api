/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FifoQueue extends Observable {
	private ConcurrentLinkedQueue<Bundle> bundles = new ConcurrentLinkedQueue<Bundle>();

	public FifoQueue() {
	}

	public boolean enqueue(Bundle bundle) {
		boolean ret = this.bundles.add(bundle);
		if(ret) {
			setChanged();
			notifyObservers();
		}
		return ret;
	}

	public Bundle dequeue() {
		return this.bundles.poll();
	}

	public boolean isEmpty() {
		return this.bundles.isEmpty();
	}
}
