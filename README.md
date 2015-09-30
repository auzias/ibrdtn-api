![IRISA](https://www.irisa.fr/sites/all/themes/irisa_theme/logo.png)
![UBS](http://www.univ-ubs.fr/images/logoUBS120x110px.jpg)
[CASA research team](http://www-casa.irisa.fr/)

API being programmed during my Ph.D. within the [CASA research team](http://www-casa.irisa.fr/) at the [UBS](http://www.univ-ubs.fr/) with the laboratory [IRISA](https://www.irisa.fr/).

IBR-DTN Java API
================

What does it do?
----------------
This project aims to offer a simplified and easy-to-use java API for the [IBR-DTN (BP implementation)](http://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/). It communicates with the daemon to send and receive bundles while focusing on the ease of its usage.

How to use it?
--------------
The first thing you have to do is to create your own handler implementing the interface [BundleHandler](src/org/ibrdtnapi/BundleHandler.java) in order to override the `onReceive(Bundle bundle)` method to process, as need be, the received bundles:
```java
public class PrintingHandler implements BundleHandler {
	@Override
	public void onReceive(Bundle bundle) {
		System.out.println("Received bundle:" + bundle.toString());
	}
}
```
You can then create your `MyBpApplication` instance and set the EID. [IBR-DTN](http://trac.ibr.cs.tu-bs.de/project-cm-2012-ibrdtn/) supports both group and singleton EID. If your local node name is *dtn://localname* and you want to receive bundles sent to the URI *dtn://localname/MyApp* then set the eid with the **path-part** of the URI, this way: `bpApp.setEid("MyApp");`. However, if you want to receive bundles sent to the URI *dtn://global/all* set the EID with the **full** URI, not the path-part only. If the `eid` contains "://" it is assumed that the application is **not** a singleton.

To send a bundle, just call the method `send(Bundle bundle)` of [BpApplication](src/org/ibrdtnapi/BpApplication.java).

Here is a full example:
```java
    public static void main(String[] args) throws InterruptedException {
        //My local node name is "dtn://actuator"
        String eid = "app";
        BpApplication bpApp = new BpApplication();
        //You can also call the constructor as follow: new BpApplication(eid);
        //and get rid of the next line  
        bpApp.setEid(eid); //Bundle sent to "dtn://actuator/app" will be received..
        //Set your Handler
        bpApp.setHandler(new PrintingHandler());//.. and processed by this handler.

        Bundle bundle = new Bundle("dtn://logger/X", "Payload\n".getBytes());
	//To add a payload block into the bundle use: boolean bundle.[addDecoded(byte[] data)|addEncoded(String data)]
        //This will send the bundle from dtn://actuator/app to dtn://logger/X, with the payload "Payload\n".
        bpApp.send(bundle);
        }
```

How does it work ?
------------------
The two main classes are [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) and [BpApplication](src/org/ibrdtnapi/BpApplication.java). [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) is the class that manages the communication with the daemon through the two communicators (input and output). It notifies received bundles to the [BpApplication](src/org/ibrdtnapi/BpApplication.java) and sends bundles to the daemon on behalf of [BpApplication](src/org/ibrdtnapi/BpApplication.java). The [CommunicatorInput](src/org/ibrdtnapi/dispatcher/CommunicatorInput.java), among other classes, sets the state of the [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java). States are used so the [Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) does not send a bundle while fetching a received one.
[Dispatcher](src/org/ibrdtnapi/dispatcher/Dispatcher.java) and [BpApplication](src/org/ibrdtnapi/BpApplication.java) communicate using observable [FifoBundleQueue](src/org/ibrdtnapi/entities/FifoBundleQueue.java), one for received bundles, the other one for the bundle to be sent.

See the [architecture.svg](imgs/architecture.svg) to get a visual overview.

##### Multi-payload blocks considerations
Bundles can have multiple payload blocks. However if a bundle gathers too much data in its different blocks and is bigger than the MTU of the Convergence Layer fragmentation will happen. We may want to avoid this (for many reasons) and a payload block is added **only** if it is the first payload block and if the new payload block weight summed to the current payload blocks weight is less than `Api.MAX_PAYLOAD_WEIGHT`.

Performance
-----------
Tests performed on a localhost with a i5-3570 CPU @ 3.40GHz, running Debian 3.2.68-1+deb7u2, IBR-DTN daemon 0.12.1 (build 7c220eb) and Java 1.7 (OpenJDK Runtime Environment (IcedTea 2.5.5) (7u79-2.5.5-1~deb7u1)).
### Reception
With the code of [2562d61](https://github.com/auzias/ibrdtn-api/commit/2562d61bb35ae34c9f2d0cf1444ef93f69be1f20), 2000 bundles were sent before the  [app](src/org/ibrdtnapi/BpApplication.java) registered itself. The [BundleHandler](src/org/ibrdtnapi/BundleHandler.java) code didn't process bundles but just counted them and checked if 2000 were received to finally print `System.currentTimeMillis()` once the last was received.
The average time of reception for the 2000 is 5030 ms (about  2.5 ms/each).
Note that this measurement include the delay of registration.
### Registration
The average delay for an [app](src/org/ibrdtnapi/BpApplication.java) to create the Java object and successfully register to the daemon is about 27 ms.
### CPU usage
This code was first a POC of an easy-to-use IBR-DTN Java interface. The code has been made in a **quick-and-dirty** way. This include many lines with `while(this.dispatcher.getState() != State.SOME_STATE);`. These active wait are CPU eater, even though two `while();` _at most_ can run at the same time.
A *quick-and-dirty* (again, but well... A PhD Thesis is already consuming, isn't it?) solution would be to add a `Thread.Sleep(5);`. This would (partially) solve the CPU usage.
A *clean* solution to handle this would be to develop a state automate by using Java Future.
Here is the output of `time` to receive 200 bundle using the `dtnrecv` command-line tool, and the Java interface:
````bash
dtnrecv --count 200 --name test 0.02s user 0.01s system 44%  cpu 0.054 total
#With the CPU hungry version:
java -jar ibr-dtn_java-api.jar  1.11s user 0.20s system 140% cpu 0.934 total
#With the *quick-and-dirty* solution (and a 1 ns sleep):
java -jar t-while_wait-1ns.jar  0.89s user 0.18s system 62% cpu 1.728 total
#With 10k bundle to receive:
time dtnrecv --count 10000 --name test             0.59s user 0.15s system 24% cpu 3.097 total
time java -jar 10000-exit_test-while_wait-1ns.jar  9.08s user 6.66s system 23% cpu 1:07.43 total
````
Using the Java interface is far slower, but the CPU usage is far more friendly than before. Again, this is *not* the best solution!

To-do
-----
 - [x] Add executor (Thread poll) for outgoing bundles. *Actually having a Threads for each bundles is useless ..*
 - [x] Add executor (Thread poll) for incomming bundles. * .. as the work has to be done sequentially. The `ScheduledThreadPoolExecutor` works with only one thread.*
 - [x] Set, clear and test single flags of bundle.
 - [x] Remove the `ScheduledThreadPoolExecutor` and create a `Executors.newSingleThreadExecutor` instead.
 - [x] Test reception performance.
 - [x] Test registration performance.
 - [x] Test emission performance.
 - [x] Multi-payload blocks bundle (Api.MAX_PAYLOAD_BLOCK is the limit of the payload block number).
 - [ ] :bug: Solve the bug when an application is stopped, and started back.
 - [ ] CPU usage friendly.
