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
		String eid = "app";
        BpApplication bpApp = new BpApplication(eid);
        bpApp.setHandler(new PrintingHandler());
        
        System.out.println(bpApp.getURI());
        
        Bundle bundle = new Bundle("dtn://59/rcp", "That's my payload.\n".getBytes());
        bpApp.send(bundle);
/*
        bundle = new Bundle("dtn://59/rcp", "That's my 2nd payload!--\n".getBytes());
        bpApp.send(bundle);

        System.out.println("5 sec to go");
        Thread.sleep(5000);
        System.out.println("Killing the endpoint");
        bpApp.stop();
        System.out.println("Restart in 2 sec");
        Thread.sleep(2000);
        bpApp = new BpApplication(eid);
*/
	}
}