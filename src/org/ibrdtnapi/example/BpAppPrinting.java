/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;

/**
 *
 * An example of MyOwnApp extending {@link BpApplication}
 * to override the method called once a bundle is received.
 * 
 * Process it as you need.
 *
 */
public class BpAppPrinting extends BpApplication {

	public BpAppPrinting(String eid) {
		super(eid);
	}

	@Override
	protected void bundleReceived(Bundle b) {
		System.out.println("Received bundle:" + b.toString());
	}

}
