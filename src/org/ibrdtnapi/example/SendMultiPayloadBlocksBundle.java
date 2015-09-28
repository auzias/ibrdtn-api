package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;

public class SendMultiPayloadBlocksBundle {

	public static void main(String[] args) {
		String eid = "SendMultiPayloadBlocksBundle";
        BpApplication bpApp = new BpApplication(eid);
        bpApp.setHandler(new PrintingHandler());

        int numberOfBundlesToSend = 1;
        String destination = "dtn://59/rcp";
        System.out.println("Hi! I'm " + bpApp.getURI() + " and I will send " + numberOfBundlesToSend + " bundles to " + destination);

        for (int i = 0; i < numberOfBundlesToSend; i++) {
        	Bundle bundle = new Bundle(destination, new String("" + i + "\n").getBytes());
        	bundle.addEncoded("SGkhIE15IG5hbWUgaXMgTWFlbCBBdXppYXMK");//Hi!
        	bundle.addEncoded("TXkgZW1haWwgaXMgaGlAYXV6aWFzLm5ldAo=");//My email
        	bpApp.send(bundle);
        	System.out.println("#" + i + " has been sent:");
        	System.out.println(bundle);
        }
	}
}
