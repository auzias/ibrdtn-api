/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.entities;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.ibrdtnapi.Api;

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
	private ArrayList<PayloadBlock> payloadBlocks = new ArrayList<PayloadBlock>();
	private int payloadWeigth = 0;

	public Bundle() {

	}

	public Bundle(long timestamp, int blockNumber, String source, String destination) {
		this.timestamp = (timestamp == 0) ? System.currentTimeMillis() : timestamp;
		this.sequenceNumber = blockNumber;
		this.source = source;
		this.destination = destination;
		Bundle.log.finer("Bundle created with long constructor");
		this.setDefaultFlags();
	}

	public Bundle(String destination, byte[] payloadDecoded) {
		this.destination = destination;
		this.addDecoded(payloadDecoded);
		this.setDefaultFlags();
		Bundle.log.finer("Bundle created with destination/decoded-data constructor");
	}

	public Bundle(String destination) {
		this.destination = destination;
		Bundle.log.finer("Bundle created with destination/decoded-data constructor");
		this.setDefaultFlags();
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
		if(bundle.payloadBlocks.size() > 0) //It could be shorter to copy the value instead of base64-calculating it again
			payloadBlocks.addAll(bundle.payloadBlocks);
		Bundle.log.finer("Bundle created with deep-copy constructor");
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

		for (PayloadBlock payloadBlock : payloadBlocks) {
			str.append(payloadBlock.toString() + ",");
		}
		str.append(")");

		return str.toString();
	}

	public void setDestination(String eid) {
		this.destination = eid;
	}

	public int getLength(int block) {
		int length = 0;

		try {
			length = this.payloadBlocks.get(block).getLength();
		} catch (IndexOutOfBoundsException e) {
			return length;
		}

		return length;
	}

	public boolean addEncoded(String encoded) {
		//If the encoded data size added to the current payloadWeigth ..
		// .. is bigger than the Api.MAX_PAYLOAD_WEIGHT then it is NOT added ..
		// .. unless this is the *first* payload block
		if ( (this.payloadBlocks.size() != 0)
				&& (this.payloadWeigth + encoded.length()) > Api.MAX_PAYLOAD_WEIGHT)
			return false;
		else {
		// .. Otherwise it payloadWeigth is updated and the encoded data added. 
			this.payloadWeigth += encoded.length();
			this.payloadBlocks.add(new PayloadBlock(encoded));
			return true;
		}
	}

	public boolean addDecoded(byte[] decoded) {
		if(this.payloadBlocks.size() >= Api.MAX_PAYLOAD_WEIGHT)
			return false;
		else {
			this.payloadBlocks.add(new PayloadBlock(decoded));
			return true;
		}
	}

	public String getEncoded(int block) {
		String ret = null;
		try {
			ret = new String(this.payloadBlocks.get(block).getEncoded());
		} catch (IndexOutOfBoundsException e) {
			ret = new String("");
		}
		return ret;
	}

	public byte[] getDecoded(int block) {
		byte[] src = null;
		byte[] decoded = null;
		try {
			src = this.payloadBlocks.get(block).getDecoded();
			decoded = new byte[src.length];
			System.arraycopy(src, 0, decoded, 0, src.length);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return decoded;
	}

	/**************************************************************************
	**							FLAGS										 ** 
	**************************************************************************/
	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void setSingleFlag(Api.Flags flag) {
		this.flags |= (1 << flag.getPosition());
	}

	public void clearSingleFlag(Api.Flags flag) {
		this.flags &= (0 << flag.getPosition());
	}

	public int getNumberOfBlocks() {
		return this.payloadBlocks.size();
	}

	public void setClassOfService(Api.ClassOfService classOfService) {
		//Clear the ClassOfService:
		this.flags &= (0 << 0x07);
		this.flags &= (0 << 0x08);
		//Then set the ClassOfService by addition:
		this.flags += classOfService.getValue();
	}

	private void setDefaultFlags() {
		this.setClassOfService(Api.ClassOfService.DEFAULT);
		this.setSingleFlag(Api.Flags.DESTINATION_IS_SINGLETON);
	}
}
