package org.ibrdtnapi.test;

import static org.junit.Assert.*;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.entities.Bundle;
import org.junit.Test;

public class BpAppTest {
	private class BpAppPrintingTest extends BpApplication {
		public BpAppPrintingTest(String eid) {
			super(eid);
		}

		@Override
		protected void bundleReceived(Bundle b) {
			System.out.println("[BpAppPrintingTest::bundleReceived]" +
							   "Received bundle:" + b.toString());
		}
	}

	@Test
	public void testSend() {
		BpAppPrintingTest app = new BpAppPrintingTest("testing");
		boolean send = app.send(null);
		if(send) {
			fail("null-bundle shoud not be send, and the method should return *false*");
		} else {
			send = app.send(new Bundle());
			assertTrue("Bundle not-null sent", send);
		}
		
	}

	@Test
	public void testGetDispatcher() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testBundleReceived() {
		fail("Not yet implemented");
	}

}
