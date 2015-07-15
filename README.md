# ETSI ITS stack [![Build Status](https://travis-ci.org/alexvoronov/geonetworking.svg?branch=master)](https://travis-ci.org/alexvoronov/geonetworking) [![Coverage Status](https://coveralls.io/repos/alexvoronov/geonetworking/badge.svg)](https://coveralls.io/r/alexvoronov/geonetworking)


A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) ITS stack including: [CAM-DENM](https://github.com/alexvoronov/geonetworking/tree/master/camdenm) / [ASN.1](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper) / [BTP / GeoNetworking](https://github.com/alexvoronov/geonetworking/tree/master/geonetworking). For lower layer (DataLinkLayer/AccessTechnologies) see [udp2eth](https://github.com/jandejongh/udp2eth) and [utoepy](https://github.com/alexvoronov/utoepy). For even lower layers, see [Hardware and Drivers](https://github.com/alexvoronov/geonetworking/blob/master/HARDWARE.md). To test/simulate together with vehicle control models, use e.g. [ITT](https://github.com/alexvoronov/itt-gt).


### Status

Sending CAM via Single Hop Broadcast and DENM via GeoBroadcast is supported. Forwarding of GeoBroadcast packets is on the wishlist. There are currently no plans for GeoUnicast or Security (for Security, try [fitsec](https://www.assembla.com/code/fitsec/subversion/nodes)).

Basic functionality was tested at an ETSI Plugtest in March 2015, and many things already worked. However, this is not a production-ready code. Even API is expected to change when usage patterns emerge, feedback is welcome! 

One way to improve the quality is to set up an automated conformance testing using open-source TTCN-3 [Eclipse Titan](https://projects.eclipse.org/projects/tools.titan) and [ETSI ITS library](http://www.ttcn-3.org/index.php/development/devlibraries/devlib-libits). 



### Building, Testing and Running 

This project uses [Maven](http://maven.apache.org/) as a build tool.

Most IDEs work with Maven projects directly, e.g. Eclipse supports Maven through [M2Eclipse](http://www.eclipse.org/m2e/). 

Since this is a multi-module setup, Maven flags for multiple projects become useful (`--projects`, `--also-make`, `--also-make-dependents`). See [Maven docs](https://maven.apache.org/guides/mini/guide-multiple-modules.html) for manual, and a [Sonatype blog post](http://blog.sonatype.com/2009/10/maven-tips-and-tricks-advanced-reactor-options/) for a nice tutorial introduction.

Running: 

```
mvn clean install

mvn --projects uppertester exec:java -Dexec.mainClass="net.gcdc.uppertester.ItsStation" -Dexec.args="--localPortForUdpLinkLayer 1237 --remoteAddressForUdpLinkLayer 192.168.159.102:1235 --upperTesterUdpPort 1600 --hasEthernetHeader"
```


### Acknowledgements
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).


### License

This code is released under the business-friendly Apache 2.0 license.
