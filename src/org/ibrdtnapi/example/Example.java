/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;

/**
 * 
 * This example show how to receive and send bundle using IBR-DTN.
 * You first have to create your own class that extends {@link BpApplication}
 * to override the method bundleReceived(Bundle b) called at each
 * received bundle.
 *
 */
public class Example {

	public static void main(String[] args) throws InterruptedException {
        BpAppPrinting bpApp = new BpAppPrinting("azee");
        Bundle bundle = new Bundle("dtn://59/rcp", "That's my payload.\n");
        bpApp.send(bundle);
             
        bundle = new Bundle("dtn://59/rcp", "That's my 2nd payload!--\n");
        bpApp.send(bundle);
	}
}