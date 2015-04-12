###IBR-DTN Java API
This project aims to offer a simplified and easy-to-use java API for IBR-DTN.

The two main classes are Dispatcher and BpApplication. Dispatcher is the class that manage the communication with the daemon. It notifies received bundles to the BpApplication and send bundles through the daemon on behalf of BpApplication.
Both have two FifoQueues. The two FifoQueues are receivedBundles and toSendBundles. Dispatcher observes toSendBundles, while BpApplication observes receivedBundles.
