package org.ibrdtnapi;

import java.io.FileWriter;

public class Api {
	public static final int DEFAULT_PORT = 4550;
	public static final String DEFAULT_SCHEME = "dtn";
	public static final String NOT_SINGLETON = "://";
	public static final String LOG_FILE_PATH = "./ibrdtn-api.log";
	public static final boolean APPEND = true;
	public static FileWriter logFile = null;
	public static Object lockFile = new Object();
}
