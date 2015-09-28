/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

import java.io.IOException;
import java.util.ArrayList;
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
	private ArrayList<String> encoded = new ArrayList<String>();
	private ArrayList<byte[]> decoded = new ArrayList<byte[]>();

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
		this.addDecoded(payloadDecoded);
	}

	//Deep copy constructor:
	public Bundle(Bundle bundle) {
		this.timestamp = bundle.timestamp;
		this.sequenceNumber = bundle.sequenceNumber;
		if(bundle.source != null)
			this.source = new String(bundle.source);
		if(bundle.destination != null)
			this.destination = new String(bundle.destination);
		this.flags = bundle.flags;
		if(bundle.reportto != null)
			this.reportto = new String(bundle.reportto);
		if(bundle.custodian != null)
			this.custodian = new String(bundle.custodian);
		this.lifetime = bundle.lifetime;
		if(bundle.encoded != null) //It could be shorter to copy the value instead of base64-calculating it again
			this.setEncoded(bundle.encoded);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getFlags() {
		return flags;
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
		str.append(", data(");
		
		if(this.encoded.size() != 0) {
			for(int i = 0; i < this.encoded.size(); i++) {	
				str.append(((this.getLength(i) == 0) ? "X" : this.getLength(i)) + "):");
				str.append("" + this.encoded.get(i));
				str.append("#" + new String(this.decoded.get(i)) + "#");
			}
		}

		str.append(")");

		return str.toString();
	}

	public void setDestination(String eid) {
		this.destination = eid;
	}

	public int getLength(int block) {
		return (this.decoded.get(block) != null) ? this.decoded.get(block).length : 0;
	}

	public void setEncoded(ArrayList<String> encoded) {
		for(String encodedBlock : encoded) {
			this.encoded.add(encodedBlock);

			try {
				this.decoded.add(new BASE64Decoder().decodeBuffer(encodedBlock));
			} catch (IOException e) {
				Bundle.log.warning("Could not base64::decode() the payload of the bundle:" + this + ". " + e.getMessage());
			}
		}
	}

	public void addDecoded(byte[] decoded) {
		this.decoded.add(decoded);
		this.encoded.add(new String(new BASE64Encoder().encodeBuffer(decoded)).trim());
	}

	public int getDataLength(int block) {
		return this.decoded.get(block).length;
	}

	public String getEncoded(int block) {
		return this.encoded.get(block);
	}

	public byte[] getDecoded(int block) {
		return this.decoded.get(block);
	}

	/**************************************************************************
	**							FLAGS										 ** 
	**************************************************************************/
	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setSingleFlag(int flag) {
		this.flags += flag;
	}

	public void clearSingleFlag(int flag) {
		this.flags -= flag;
	}

	public boolean isSingleFlag(int flag) {
		return ((this.flags & flag) == flag);
	}
}
