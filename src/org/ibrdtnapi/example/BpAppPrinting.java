/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.Bundle;

public class BpAppPrinting extends BpApplication {

	@Override
	protected void bundleReceived(Bundle b) {
		System.out.println("Received bundle:" + b.toString());
	}

}
