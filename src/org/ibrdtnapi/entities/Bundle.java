/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

public class Bundle {
	private long timestamp;
	private int blockNumber;
	private String source = null;
	private String destination = null;
	private Bundle.State state = null;

	public Bundle() {

	}

	public Bundle(long timestamp, int blockNumber, String source, String destination, State state) {
		this.timestamp = (timestamp == 0) ? System.currentTimeMillis() : timestamp;
		this.blockNumber = blockNumber;
		this.source = source;
		this.destination = destination;
		this.state = state;
	}

	public Bundle.State getState() {
		return state;
	}

	public void setState(Bundle.State state) {
		this.state = state;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public int getBlockNumber() {
		return blockNumber;
	}

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("" + this.timestamp);
		str.append(", " + this.blockNumber);
		str.append(", from " + ((this.source == null) ? "source:none" : this.source));
		str.append(", to " + ((this.destination == null) ? "destnt:none" : this.destination));
		return str.toString();
	}

	public enum State {
		TO_FETCH(0),	//
		TO_SEND(1),		//
		SENT(2),		//
		RECEIVED(3),	//
		PROCESSED(4);	//
		public final int value;

		State(int value) {
			this.value = value;
		}
	}

	public void setDestination(String eid) {
		this.destination = eid;
	}
}
