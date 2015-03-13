# GeoNetworking stack

A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) GeoNetworking stack based on 'Geographical addressing and forwarding for point-to-point and point-to-multipoint communications' ([ETSI EN 302 636-4-1](http://webapp.etsi.org/wprogram/Report_WorkItem.asp?WKI_ID=38232)) and 'Transport Protocols; Sub-part 1: Basic Transport Protocol' ([ETSI EN 302 636-5-1](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=38233)).


### Status

Sending and receiving Single Hop Broadcast and GeoBroadcast is supported. Forwarding of GeoBroadcast packets is on the wishlist. There are currently no plans for GeoUnicast or Security.

Code here is very much work in progress, it was not thoroughly tested or verified. Even API is expected to change when usage patterns emerge, feedback is welcome.


### Implementation

The implementation assumes that there is a separate entity, e.g. [udp2eth](https://github.com/jandejongh/udp2eth) or [utoepy](https://github.com/alexvoronov/utoepy) (which might be running on the same or on a separate machine) taking care of [Data Link Layer](http://en.wikipedia.org/wiki/Data_link_layer) communication. Communication between that entity and GeoNetworking stack is implemented via UDP at the moment (see [LinkLayerUdpToEthernet](https://github.com/alexvoronov/geonetworking/blob/master/src/main/java/net/gcdc/geonetworking/LinkLayerUdpToEthernet.java)). It is easy to change Link Layer entities by implementing a [LinkLayer](https://github.com/alexvoronov/geonetworking/blob/master/src/main/java/net/gcdc/geonetworking/LinkLayer.java), one option could be to use Java bindings for `libpcap` to sniff and inject frames.

Implementation targets Java 7 at the moment, with some use of backported features from Java 8.


### Building, Testing and Running

[SBT](http://www.scala-sbt.org/) and [Maven](http://maven.apache.org/) are supported as a build tools. Since they are independent from each other, they might be out of sync sometime (please open an issue then). 

Most IDEs work with Maven projects directly, e.g. Eclipse supports Maven through [M2Eclipse](http://www.eclipse.org/m2e/). If you use SBT, you can generate Eclipse project files with ```sbt eclipse```.

Running tests: 

```
sbt test
```

or

```
mvn test
```

#### Run with echo link layer
Compile and run command-line client:

```
sbt assembly
echo 4001 127.0.0.1:4000 | java -cp target/scala-2.10/geonetworking-assembly-0.1.0-SNAPSHOT.jar net.gcdc.UdpDuplicatorRunner
java -cp target/scala-2.10/geonetworking-assembly-0.1.0-SNAPSHOT.jar net.gcdc.BtpStdinClient --local-port 4000 --remote-address 127.0.0.1:4001 --no-ethernet-header --position 13.000,50.000 --btp-destination-port 2001
```
The first line compiles the source and assembles everything into one big "fat" jar. 

The second line starts a fake UDP Link Layer entity that echoes packets back. Echo server listens on UDP port 4001, and forwards all packets to UDP port 4000.

The third line starts a command line client. The client assumes that Link Layer can be reached by sending payload to UDP port 4001, and expects that the payload from Link Layer can be received on UDP port 4000. The client gets BTP payload from stdin, and prints received BTP payload to stdout.

#### Run with utoepy as link layer

Run command-line client on top of [utoepy](https://github.com/alexvoronov/utoepy) bound to [tap0](http://en.wikipedia.org/wiki/TUN/TAP):

```
sudo python udp2eth.py 4001 tap0
sudo python eth2udp.py 127.0.0.1:4000 tap0 --keep-own-frames
java -cp target/scala-2.10/geonetworking-assembly-0.1.0-SNAPSHOT.jar net.gcdc.BtpStdinClient --local-port 4000 --remote-address 127.0.0.1:4001 --no-ethernet-header --position 13.000,50.000 --btp-destination-port 2001
```

With `--keep-own-frames`, `eth2udp` will pick even the frames sent by `udp2eth`, so the sender will hear its own packets. TAP interfaces of multiple computers can be connected with OpenVPN, here is a diagram for that case:

```
           +--------+                    +---------+    +----------+   +-------+   +----------+    +---------+
           | BTP/GN |     127.0.0.1:4001 | udp2eth |    | Ethernet |   |OpenVPN|   | Ethernet |    | udp2eth |
 Stdin --->|        |------------------->|4001     |--->|  (tap0)  |   |Bridge |   |  (tap0)  |<---|     4001|...
           |        |                    +---------+    |          |   |       |   |          |    +---------+
           |        |                                   |          |<->|       |<->|          |               
           |        |                    +---------+    |          |   |       |   |          |    +---------+
           |        | 127.0.0.1:4000     | eth2udp |    |          |   |       |   |          |    | eth2udp |
Stdout <---|    4000|<-------------------|         |<---|          |   |       |   |          |--->|         |...
           |        |                    +---------+    +----------+   +-------+   +----------+    +---------+
           +--------+
```


### Other GeoNetworking implementations

This implementation was inspired by [DriveITS](https://github.com/Dimme/driveits). Another open-source implementation, now outdated, is [CarGeo6](http://www.cargeo6.org/).


### Acknowledgements
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).


### License

This GeoNetworking source code is released under the business-friendly Apache 2.0 license.
