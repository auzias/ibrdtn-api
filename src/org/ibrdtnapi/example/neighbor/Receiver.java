package org.ibrdtnapi.example.neighbor;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ibrdtnapi.BpApplication;
import org.ibrdtnapi.example.PrintingHandler;

public class Receiver {
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void main(String[] args) throws InterruptedException {
		String eid = "rcv";
		BpApplication bpApp = new BpApplication(eid);
		bpApp.setHandler(new PrintingHandler());

		Runnable r = new NeighborListing(bpApp);
		Receiver.scheduler.scheduleAtFixedRate(r, 0, 10, TimeUnit.MILLISECONDS);
	}
}
