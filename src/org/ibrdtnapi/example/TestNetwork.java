package org.ibrdtnapi.example;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;

public class TestNetwork {

	public static void main(String[] args) {/*
		if (args.length != 5) {
			System.err.println ("Usage is: " + args[0] + "destination_EID  number_of_payload_blocks  char_number_in_each_payload(4,9,49,98)  number_of_bundle_to_send");
			System.err.println ("{number_of_bundle_to_send} will be sent to {destination_EID} with {number_of_payload_blocks} payload blocks " +
								"containing {char_number_in_each_payload} characters* in each bundle.");
			System.err.println ("Note that N characters of data, will be base64 encoded into M characters");
			System.exit(-1);
		}*/
        final String _destination = "dtn://59/rcp";
        final int _numberOfBlocks = 2;
        final int _numberOfChar = 4;
        final int _numberOfBundlesToSend = 1;

        
        String eid = "TestNetwork";
        BpApplication bpApp = new BpApplication(eid);
        bpApp.setHandler(new PrintingHandler());

        System.out.println ("Hi! I'm " + bpApp.getURI() + " and I will send " + _numberOfBundlesToSend + " bundles with " + _numberOfBlocks
        					+ " blocks with " + _numberOfChar + " bytes of encoded data to " + _destination);

        String payloadEncoded = null;
        switch (_numberOfChar) {
		case 4:
			payloadEncoded = new String("YWE=");
			break;
		case 9:
			payloadEncoded = new String("MTIzNDU2");
			break;
		case 49:
			payloadEncoded = new String("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1");
			break;
		case 98:
			payloadEncoded = new String("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEzNDU2ODkwMTI1");
			break;
		default:
			System.exit(-2);
			break;
		}
        
        boolean ok = true;
        Bundle bundle = new Bundle(_destination);
        bundle.clearSingleFlag(Api.Flags.DESTINATION_IS_SINGLETON);
        for(int payloadBlock = 0; payloadBlock < _numberOfBlocks; payloadBlock++) {
        	ok &= bundle.addEncoded(payloadEncoded);
        }

        long start = System.currentTimeMillis();

        if(ok) {
	        for (int bundletoSend = 0; bundletoSend < _numberOfBundlesToSend; bundletoSend++) {
	        	bpApp.send(bundle);
	        }
        } else {
        	System.err.println("An error occured.");
        }
        
        System.out.println("I'm done (work done in " + (System.currentTimeMillis() - start) + " ms)");
	}
}
