/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.BundleHandler;
import org.ibrdtnapi.entities.Bundle;

/**
 *
 * An example of MyOwnApp implementing {@link BundleHandler}
 * to override the method called once a bundle is received.
 * 
 * Process it as you need.
 *
 */
public class PrintingHandler implements BundleHandler {

	@Override
	public void onReceive(Bundle bundle) {
		System.out.println("Received bundle:" + bundle.toString());
	}
}
