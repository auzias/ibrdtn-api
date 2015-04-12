/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

import java.util.Observable;
import java.util.Observer;

public abstract class BpApplication implements Observer {
	private FifoQueue receivedBundles = null;
	private FifoQueue toSendBundles = new FifoQueue();
	private Dispatcher dispatcher = null;

	public BpApplication() {
		this.dispatcher = new Dispatcher(toSendBundles, this);
		this.toSendBundles.addObserver(this.dispatcher);
		this.receivedBundles = this.dispatcher.getReceivedBundles();
	}

	public boolean send(Bundle b) {
		return this.toSendBundles.enqueue(b);
	}

	public Dispatcher getDispatcher(){
		return this.dispatcher;
	}
	
	@Override
	public void update(Observable receivedBundles, Object o) {
		if(this.receivedBundles == receivedBundles && !this.receivedBundles.isEmpty()) {
			this.bundleReceived(((FifoQueue)receivedBundles).dequeue());
		}
	}

	protected abstract void bundleReceived(Bundle b);
}
