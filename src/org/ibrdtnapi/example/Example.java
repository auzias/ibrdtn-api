/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.entities.Bundle;

public class Example {

	public static void main(String[] args) {
        BpAppPrinting bpApp = new BpAppPrinting("azee");
        Bundle bundle = new Bundle("dtn://59/rcp", "That's my payload!");
        //bpApp.send(bundle);
	}
}