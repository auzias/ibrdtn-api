package org.ibrdtnapi;

import java.util.LinkedList;

public class FifoQueue<E> extends LinkedList<E> {
	private static final long serialVersionUID = 4902517869289867995L;

	public FifoQueue() {
		
	}

	public boolean enqueue(E e) {
		return this.add(e);
	}

	public E dequeue() {
		return this.removeFirst();
	}
	
}
