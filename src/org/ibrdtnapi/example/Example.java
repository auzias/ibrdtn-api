/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.entities.Bundle;

public class Example {

	public static void main(String[] args) throws InterruptedException {
        BpAppPrinting bpApp = new BpAppPrinting("azee");
        Bundle bundle = new Bundle("dtn://59/rcp", "That's my payload!  :D\n");
        bpApp.send(bundle);
        
        Thread.sleep(5000);
        
        bundle = new Bundle("dtn://59/rcp", "That's my 2nd payload!  :D\n");
        bpApp.send(bundle);
	}
}