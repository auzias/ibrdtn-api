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
public class Sender {

	public static void main(String[] args) throws InterruptedException {
		String eid = "test";
        BpApplication bpApp = new BpApplication(eid);
        bpApp.setHandler(new PrintingHandler());

        int numberOfBundlesToSend = 2;
        String destination = "dtn://59/rcp";
        System.out.println("Hi! I'm " + bpApp.getURI() + " and I will send " + numberOfBundlesToSend + " bundles to " + destination);

        for (int i = 0; i < numberOfBundlesToSend; i++) {
        	Bundle bundle = new Bundle(destination, new String("" + i + "\n").getBytes());
        	bpApp.send(bundle);
        }

	}
}
