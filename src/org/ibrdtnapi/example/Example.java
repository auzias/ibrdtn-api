/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import org.ibrdtnapi.Bundle;

public class Example {

	public static void main(String[] args) {
		BpAppPrinting bpApp = new BpAppPrinting();
		System.out.println("It's " + System.currentTimeMillis());
		boolean sending = bpApp.send(new Bundle());
		System.out.println("sending:" + sending);	

		bpApp.getDispatcher().simulateBundle(new Bundle());
	}
}
