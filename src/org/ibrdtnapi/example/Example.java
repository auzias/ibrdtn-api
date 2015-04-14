/**
 * Created by auzias (Apr 12, 2015)
 *
 */
package org.ibrdtnapi.example;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Example {

	public static void main(String[] args) {
		Logger logger = Logger.getGlobal();
		logger.setLevel(Level.FINEST);
        ConsoleHandler handler = new ConsoleHandler();
        // PUBLISH this level
        handler.setLevel(Level.FINEST);
        logger.addHandler(handler);
        BpAppPrinting bpApp = new BpAppPrinting("azee");
	}
}