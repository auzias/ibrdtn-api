/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi;

public class Bundle {
	private String timestamp;

	public Bundle() {
		timestamp = "" + System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return timestamp;
	}
}
