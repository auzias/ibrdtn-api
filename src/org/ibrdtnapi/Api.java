package org.ibrdtnapi;

import java.io.FileWriter;

public class Api {
	public static final int DEFAULT_PORT = 4550;
	public static final String DEFAULT_SCHEME = "dtn";
	public static final String NOT_SINGLETON = "://";	//It is assumed that if the endpoint of the Application contains "://" it is a group and not a singleton.
	public static final String LOG_FILE_PATH = "./ibrdtn-api.log";
	public static final boolean APPEND = true;
	public static final int DEFAULT_LIFETIME = 1800;
	public static FileWriter logFile = null;
	public static Object lockFile = new Object();
	public static Object lockFetcher = new Object();
	
	//Flags:
	public static final int FRAGMENT = (1 << 0x00);
	public static final int APPDATA_IS_ADMRECORD = (1 << 0x01);
	public static final int DONT_FRAGMENT = (1 << 0x02);
	public static final int CUSTODY_REQUESTED = (1 << 0x03);
	public static final int DESTINATION_IS_SINGLETON = (1 << 0x04);
	public static final int ACKOFAPP_REQUESTED = (1 << 0x05);
	public static final int CLASS_OF_SERVICE_BULK = (0 << 0x07) + (0 << 0x08);
	public static final int CLASS_OF_SERVICE_NORMAL = (1 << 0x07) + (0 << 0x08);
	public static final int CLASS_OF_SERVICE_EXPEDITED =  (0 << 0x07) + (1 << 0x08);;
	public static final int CLASSOFSERVICE_9 = (1 << 0x09);
	public static final int CLASSOFSERVICE_10 = (1 << 0x0A);
	public static final int CLASSOFSERVICE_11 = (1 << 0x0B);
	public static final int CLASSOFSERVICE_12 = (1 << 0x0C);
	public static final int CLASSOFSERVICE_13 = (1 << 0x0D);
	public static final int REQUEST_REPORT_OF_BUNDLE_RECEPTION = (1 << 0x0E);
	public static final int REQUEST_REPORT_OF_CUSTODY_ACCEPTANCE = (1 << 0x0F);
	public static final int REQUEST_REPORT_OF_BUNDLE_FORWARDING = (1 << 0x10);
	public static final int REQUEST_REPORT_OF_BUNDLE_DELIVERY = (1 << 0x11);
	public static final int REQUEST_REPORT_OF_BUNDLE_DELETION = (1 << 0x12);
	//Sum(foreach(payloadBlocks.weight)) =< MAX_PAYLOAD_WEIGHT (MAX_PAYLOAD_WEIGHT should be the MTU of the Convergence Layer)
	public static final int MAX_PAYLOAD_WEIGHT = 1200;

	public enum ClassOfService {
		BULK(CLASS_OF_SERVICE_BULK),
		NORMAL(CLASS_OF_SERVICE_NORMAL),
		EXPEDITED(CLASS_OF_SERVICE_EXPEDITED);

		private int classOfService = 0;
		ClassOfService(int classOfService) {
			this.classOfService = classOfService;
		}

		public int getValue() {
			return this.classOfService;
		}
	}
	
	public static void sleepWait() {
 		try {
			//Thread.sleep(1);
			Thread.sleep(0, 1);
		} catch (InterruptedException e) {
			// We waited a bit less than 1 ns, now what?
		}
	}
}
