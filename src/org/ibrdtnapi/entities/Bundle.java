/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

import java.io.IOException;
import java.util.logging.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * The class Bundle represents bundle with ONLY ONE SINGLE BLOCK.  
 *
 */
public class Bundle {
	private static final Logger log = Logger.getLogger(Bundle.class.getName());
	private long timestamp;
	private int sequenceNumber;
	private String source = null;
	private String destination = null;
	private int flags = 0;
	private String reportto = null;
	private String custodian = null;
	private int lifetime = 0;
	private String encoded = null;
	private byte[] decoded = null;

	public Bundle() {

	}

	public Bundle(long timestamp, int blockNumber, String source, String destination) {
		this.timestamp = (timestamp == 0) ? System.currentTimeMillis() : timestamp;
		this.sequenceNumber = blockNumber;
		this.source = source;
		this.destination = destination;
	}

	public Bundle(String destination, byte[] payloadDecoded) {
		this.destination = destination;
		this.setDecoded(payloadDecoded);
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

	public String getSource() {
		return source;
	}

	public String getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("" + ((this.source == null) ? "source:none" : this.source));
		str.append(" -> " + ((this.destination == null) ? "destnt:none" : this.destination));
		str.append(" @" + this.timestamp);
		str.append(", data");
		str.append("(" + ((this.getLength() == 0) ? "X" : this.getLength()) + "):");
		if(this.encoded != null)
			str.append("" + this.encoded);
		if(this.decoded != null)
			str.append("#" + new String(this.decoded) + "#");

		return str.toString();
	}

	public void setDestination(String eid) {
		this.destination = eid;
	}

	public int getLength() {
		return (this.decoded != null) ? this.decoded.length : 0;
	}

	public void setEncoded(String encoded) {
		this.encoded  = new String(encoded);
		try {
			this.decoded = new BASE64Decoder().decodeBuffer(this.encoded);
		} catch (IOException e) {
			Bundle.log.warning("Could not base64::decode() the payload of the bundle:" + this + ". " + e.getMessage());
		}
	}

	public void setDecoded(byte[] decoded) {
		this.decoded  = decoded;
		this.encoded = new String(new BASE64Encoder().encodeBuffer(this.decoded)).trim();
	}

	public int getDataLength() {
		return this.decoded.length;
	}

	public String getEncoded() {
		return this.encoded;
	}

	public byte[] getDecoded() {
		return this.decoded;
	}
}
