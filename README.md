# ETSI ITS G5 GeoNetworking Stack [![Build Status](https://travis-ci.org/alexvoronov/geonetworking.svg?branch=master)](https://travis-ci.org/alexvoronov/geonetworking) [![Coverage Status](https://coveralls.io/repos/alexvoronov/geonetworking/badge.svg)](https://coveralls.io/r/alexvoronov/geonetworking) [![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.55650.svg)](http://dx.doi.org/10.5281/zenodo.55650)

A part of [ETSI](http://en.wikipedia.org/wiki/ETSI) ITS G5 stack including: 

* Layer 7 Application: [CAM-DENM](https://github.com/alexvoronov/camdenm)
* Layer 6 Presentation: [ASN.1 UPER](https://github.com/alexvoronov/gcdc-asn1) 
* Layer 5/4/3 Transport and Network: [BTP / GeoNetworking](https://github.com/alexvoronov/geonetworking) (this library)
* Layer 2 Data link: [udp2eth](https://github.com/jandejongh/udp2eth) and [utoepy](https://github.com/alexvoronov/utoepy)
* Layer 1 Physical: see [Hardware and Drivers](https://github.com/alexvoronov/geonetworking/blob/master/HARDWARE.md)

To test/simulate together with vehicle control models, use e.g. [ITT](https://github.com/alexvoronov/itt-gt). For a C++ ITS-G5, see [vanetza](https://github.com/riebl/vanetza).

## GeoNetworking stack

A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) GeoNetworking stack based on 'Geographical addressing and forwarding for point-to-point and point-to-multipoint communications' ([ETSI EN 302 636-4-1](http://webapp.etsi.org/wprogram/Report_WorkItem.asp?WKI_ID=38232)) and 'Transport Protocols; Sub-part 1: Basic Transport Protocol' ([ETSI EN 302 636-5-1](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=38233)).


### Status

Sending and receiving Single Hop Broadcast and GeoBroadcast is supported. Forwarding of GeoBroadcast packets is on the wishlist. There are currently no plans for GeoUnicast or Security (for Security, try [FITSec](https://github.com/fillabs/FITSec) or [vanetza](https://github.com/riebl/vanetza)).

The code was tested during ETSI ITS CMS 4 PlugTest in March 2015 and during Grand Cooperative Driving Challenge in May 2016.

One way to improve the quality is to set up an automated conformance testing using open-source TTCN-3 [Eclipse Titan](https://projects.eclipse.org/projects/tools.titan) and [ETSI ITS library](http://www.ttcn-3.org/index.php/development/devlibraries/devlib-libits).


### Implementation

The implementation assumes that there is a separate entity, e.g. [udp2eth](https://github.com/jandejongh/udp2eth) or [utoepy](https://github.com/alexvoronov/utoepy) (which might be running on the same or on a separate machine) taking care of [Data Link Layer](http://en.wikipedia.org/wiki/Data_link_layer) communication. Communication between that entity and GeoNetworking stack is implemented via UDP at the moment (see [LinkLayerUdpToEthernet](https://github.com/alexvoronov/geonetworking/blob/master/geonetworking/src/main/java/net/gcdc/geonetworking/LinkLayerUdpToEthernet.java)). It is easy to change Link Layer entities by implementing a [LinkLayer](https://github.com/alexvoronov/geonetworking/blob/master/geonetworking/src/main/java/net/gcdc/geonetworking/LinkLayer.java), one option could be to use Java bindings for `libpcap` to sniff and inject frames.

Implementation targets Java 7 at the moment, with some use of backported features from Java 8.


### Building, Testing and Running

[Maven](http://maven.apache.org/) is used as a build tool. There is also some support for [SBT](http://www.scala-sbt.org/).

Most IDEs work with Maven projects directly, e.g. Eclipse supports Maven through [M2Eclipse](http://www.eclipse.org/m2e/). If you use SBT, you can generate Eclipse project files with ```sbt eclipse```.

Running tests: 

```
sbt test
```

or

```
mvn test
```

#### Using as a library
Compile and install into the local maven repository:

```
mvn install
```
Examples of using `geonetworking` as a library, as well as a mechanism to send CAM messages, are in the [uppertester](https://github.com/alexvoronov/gn-uppertester). Another example is in the [Rendits Router](https://github.com/rendits/router). 

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


#### Run with UdpBtpClient

Assuming that link layer is already started (see previous example), here's how to start UDP BTP client. This client receives the payload of the BTP packet on a UDP port. The destination port for BTP prefixes the BTP payload in the UDP packet. To send CAM to BTP port 2001 (0x07D1), send the following UDP packet to the client: ```07.D1.XX.YY.ZZ```, where ```XXYYZZ``` is the BTP Payload (binary CAM message). Note that there is no way to specify *destination port info* for BTP, but it is usually not used anyway.

```
mvn package

java -cp target/geonetworking-0.0.1-SNAPSHOT-jar-with-dependencies.jar net.gcdc.BtpUdpClient --position 13.000,50.000 --local-udp2eth-port 4000 --remote-udp2eth-address 127.0.0.1:4001 --local-data-port 5003 --remote-data-address 192.168.1.23:1236 --mac-address 00:00:00:00:01:11
```

### Related implementations

This implementation was inspired by [DriveITS](https://github.com/Dimme/driveits). A good C++ LGPL implementation is [vanetza](https://github.com/riebl/vanetza). Another open-source implementation, now outdated, is [CarGeo6](http://www.cargeo6.org/).


### Acknowledgements
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).


### License

This GeoNetworking source code is released under Apache 2.0 license.
