package org.ibrdtnapi.dispatcher;

import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.ibrdtnapi.Api;
import org.ibrdtnapi.ApiException;
import org.ibrdtnapi.entities.Bundle;

/**
 * 
 * This class is used to send commands to the daemon.
 * 
 * See the comment for {@link CommunicatorInput}
 * to learn about how the log are done, it's the same.
 * 
 */
public class CommunicatorOutput {
	private static final Logger log = Logger.getLogger(CommunicatorOutput.class.getName());
	private DataOutputStream dos = null;

	public CommunicatorOutput(DataOutputStream dos) {
		this.dos = dos;
	}

	public void query(String str) {
		str = str.trim();
		this.log(str);
		str += "\n";
		try {
			this.dos.write(str.getBytes());
			this.dos.flush();
		} catch (IOException e) {
			throw new ApiException("Could not write to the scoket. " + e.getMessage());
		}
	}

	public int sendBundle(Bundle bundle) {
		this.query("bundle");
		return 0;
	}

	public void log(String str) {
		try {
			synchronized(Api.lockFile) {
				Api.logFile = new FileWriter(Api.LOG_FILE_PATH, Api.APPEND);
				Api.logFile.append(str + "\n");
				Api.logFile.flush();
				Api.logFile.close();
			}
		} catch (IOException e) {
			CommunicatorOutput.log.info("Could not open the file '" + Api.LOG_FILE_PATH + "' to write it. Log will be display at finest level.");
			CommunicatorOutput.log.finest(str);
		}
	}
}
