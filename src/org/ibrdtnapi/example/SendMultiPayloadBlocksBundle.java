package org.ibrdtnapi.example;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;

public class SendMultiPayloadBlocksBundle {

	public static void main(String[] args) {
		String eid = "SendMultiPayloadBlocksBundle";
        BpApplication bpApp = new BpApplication(eid);
        bpApp.setHandler(new PrintingHandler());

        int numberOfBundlesToSend = 100;
        String destination = "dtn://59/Sender";
        System.out.println("Hi! I'm " + bpApp.getURI() + " and I will send " + numberOfBundlesToSend + " bundles to " + destination);

        for (int i = 0; i < numberOfBundlesToSend; i++) {
        	Bundle bundle = new Bundle(destination, new String("" + i + "\n").getBytes());
        	boolean ok = true;
        	ok &= bundle.addEncoded("SGkhIE15IG5hbWUgaXMgTWFlbCBBdXppYXMK");//Hi!
        	ok &= bundle.addEncoded("TXkgZW1haWwgaXMgaGlAYXV6aWFzLm5ldAo=");//My email
        	if(ok) {
	        	bpApp.send(bundle);
	        	System.out.println("#" + i + " has been sent:" + bundle);
        	} else {
	        	System.out.println("#" + i + " has *NOT* been sent:" + bundle);
        	}
        }
	}
}
