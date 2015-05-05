![IRISA](https://www.irisa.fr/sites/all/themes/irisa_theme/logo.png)
![UBS](http://www.univ-ubs.fr/images/logoUBS120x110px.jpg)
[CASA research team](http://www-casa.irisa.fr/)

IBR-DTN Java API
================

What does it do?
----------------
This project aims to offer a simplified and easy-to-use java API for the [IBR-DTN (BP implementation)](http://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/). It communicates with the daemon to send and receive bundles while focusing on the ease of its usage.

How to use it?
--------------
The first thing you have to do is extend the abstract class [BpApplication](src/org/ibrdtnapi/BpApplication.java) in order to override the `bundleReceived(Bundle b)` method to process, as need be, the received bundles:
```java
public class MyBpApp extends BpApplication {

public BpAppPrinting(String eid) {
                super(eid);
        }

        @Override
        protected void bundleReceived(Bundle b) {
                System.out.println("Received bundle:" + b.toString());
        }
}
```
You can then create your `MyBpApp` instance by setting the EID in the constructor. [IBR-DTN](http://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/) supports both group and singleton EID. If your local node name is *dtn://localname* and you want to receive bundles sent to the URI *dtn://localname/MyEID* then call the constructor with the path-part of the URI, this way: `MyBpApp application = new MyBpApp(MyEID);`. However, if you want to receive bundles sent to the URI *dtn://global/advertise* call the constructor with the full URI. If the `eid` contains "://" it is assumed that the application is **not** a singleton.

To send a bundle, just call the method `send(Bundle bundle)` of [BpApplication](src/org/ibrdtnapi/BpApplication.java).

Here is a full example:
```java
        public static void main(String[] args) throws InterruptedException {
          //My local node name is "dtn://zulu"
          BpAppPrinting bpApp = new BpAppPrinting("log");//Bundles sent to "dtn://zulu/log" will be received
          Bundle bundle = new Bundle("dtn://panthers/X", "Payload\n".getBytes());
          bpApp.send(bundle);//This will send the bundle from dtn://zulu/log to dtn://panthers/X, with the payload "Payload\n".
        }
```

How does it work ?
------------------
The two main classes are [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) and [BpApplication](src/org/ibrdtnapi/BpApplication.java). [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) is the class that manages the communication with the daemon through the two communicators (input and output). It notifies received bundles to the [BpApplication](src/org/ibrdtnapi/BpApplication.java) and sends bundles to the daemon on behalf of [BpApplication](src/org/ibrdtnapi/BpApplication.java). The [CommunicatorInput](src/org/ibrdtnapi/dispatcher/CommunicatorInput.java), among other classes, sets the state of the [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java). States are used so the [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) does not send a bundle while fetching a received one.
[Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) and [BpApplication](src/org/ibrdtnapi/BpApplication.java) communicate using observable [FifoBundleQueue](src/org/ibrdtnapi/entities/FifoBundleQueue.java), one for received bundles, the other one for the bundle to be sent.

See the [architecture.svg](imgs/architecture.svg) to get a visual overview.

License
-------
Apache License - Version 2.0, just like [IBR-DTN](http://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/wiki/license).

To-do
-----
 - [ ] Add executor (Thread poll) for outgoing bundles.
 - [x] Add executor (Thread poll) for incomming bundles.
