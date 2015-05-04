package org.ibrdtnapi;

import java.io.FileWriter;

public class Api {
	public static final int DEFAULT_PORT = 4550;
	public static final String DEFAULT_SCHEME = "dtn";
	public static final String NOT_SINGLETON = "://";	//It is assumed that if the endpoint of the Application contains "://" it is a group and not a singleton.
	public static final String LOG_FILE_PATH = "./ibrdtn-api.log";
	public static final boolean APPEND = true;
	public static final int THREAD_POOL = 3;
	public static final int DEFAULT_LIFETIME = 1800;
	public static FileWriter logFile = null;
	public static Object lockFile = new Object();
	public static Object lockFetcher = new Object();
	public static Object lockSender = new Object();
}
