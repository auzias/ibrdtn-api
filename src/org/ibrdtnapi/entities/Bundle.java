/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

import java.io.IOException;
import java.util.logging.Logger;
import sun.misc.BASE64Decoder;

/**
 * The class Bundle represent bundle with ONLY ONE SINGLE BLOCK.  
 *
 */
public class Bundle {
	private static final Logger log = Logger.getLogger(Bundle.class.getName());
	private long timestamp;
	private int sequenceNumber;
	private String source = null;
	private String destination = null;
	private int length;
	private int flags = 0;
	private String reportto = null;
	private String custodian = null;
	private int lifetime = 0;
	private String encoded = null;
	private String decoded = null;

	public Bundle() {

	}

	public Bundle(long timestamp, int blockNumber, String source, String destination) {
		this.timestamp = (timestamp == 0) ? System.currentTimeMillis() : timestamp;
		this.sequenceNumber = blockNumber;
		this.source = source;
		this.destination = destination;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getSequencenumber() {
		return this.sequenceNumber;
	}

	public void setSequencenumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getReportto() {
		return reportto;
	}

	public void setReportto(String reportto) {
		this.reportto = reportto;
	}

	public String getCustodian() {
		return custodian;
	}

	public void setCustodian(String custodian) {
		this.custodian = custodian;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setBlockNumber(int blockNumber) {
		this.sequenceNumber = blockNumber;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setLength(int length) {
		this.length = length;
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
		str.append("From " + ((this.source == null) ? "source:none" : this.source));
		str.append(", to " + ((this.destination == null) ? "destnt:none" : this.destination));
		str.append(" @ " + this.timestamp);
		str.append(", length:" + this.length);
		if(this.encoded != null)
			str.append(", encoded:" + this.encoded);
		if(this.decoded != null)
			str.append(", decoded:#" + this.decoded + "#");

		return str.toString();
	}

	public void setDestination(String eid) {
		this.destination = eid;
	}

	public int getLength() {
		return this.length;
	}

	public void setEncoded(String encoded) {
		this.encoded  = new String(encoded);
		try {
			this.decoded = new String(new BASE64Decoder().decodeBuffer(this.encoded));
		} catch (IOException e) {
			Bundle.log.warning("Could not base64::decode() the payload of the bundle:" + this + ". " + e.getMessage());
		}
	}
}
