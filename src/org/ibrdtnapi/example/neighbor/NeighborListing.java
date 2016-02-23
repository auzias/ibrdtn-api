package org.ibrdtnapi.example.neighbor;

import java.util.ArrayList;
import java.util.List;

import org.ibrdtnapi.BpApplication;

public class NeighborListing implements Runnable {
	private BpApplication bpApp = null;

	public NeighborListing(BpApplication bpApp) {
		this.bpApp = bpApp;
	}

	@Override
	public void run() {
		System.out.print(".");
		List<String> list = new ArrayList<String>(this.bpApp.getNeighborList());
		System.out.print("=========================== >>>>>>> List(" + list.size() + "): ");
		for (String name : list) {
			name = name.trim();
			System.out.print(name + ", ");
		}
		System.out.println();
	}
}
