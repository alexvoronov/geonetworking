# ETSI ITS G5 GeoNetworking Stack [![Build Status](https://travis-ci.org/alexvoronov/geonetworking.svg?branch=master)](https://travis-ci.org/alexvoronov/geonetworking) [![Coverage Status](https://coveralls.io/repos/alexvoronov/geonetworking/badge.svg)](https://coveralls.io/r/alexvoronov/geonetworking) [![DOI](https://zenodo.org/badge/doi/10.5281/zenodo.55650.svg)](http://dx.doi.org/10.5281/zenodo.55650)



A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) ITS G5 stack including: [CAM-DENM](https://github.com/alexvoronov/geonetworking/tree/master/camdenm) / [ASN.1](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper) / [BTP / GeoNetworking](https://github.com/alexvoronov/geonetworking/tree/master/geonetworking). For lower layer (DataLinkLayer/AccessTechnologies) see [udp2eth](https://github.com/jandejongh/udp2eth) and [utoepy](https://github.com/alexvoronov/utoepy). For even lower layers, see [Hardware and Drivers](https://github.com/alexvoronov/geonetworking/blob/master/HARDWARE.md). To test/simulate together with vehicle control models, use e.g. [ITT](https://github.com/alexvoronov/itt-gt). For a C++ ITS-G5, see [vanetza](https://github.com/riebl/vanetza).

![Stack](https://rawgit.com/alexvoronov/geonetworking/master/doc/img/stack.svg)

### Status

Sending CAM via Single Hop Broadcast and DENM via GeoBroadcast is supported. Forwarding of GeoBroadcast packets is on the wishlist. There are currently no plans for GeoUnicast or Security (for Security, try [FITSec](https://github.com/fillabs/FITSec) or [vanetza](https://github.com/riebl/vanetza)).

Basic functionality was tested at an ETSI Plugtest in March 2015 and at Grand Cooperative Driving Challenge in May 2016.

One way to improve the quality is to set up an automated conformance testing using open-source TTCN-3 [Eclipse Titan](https://projects.eclipse.org/projects/tools.titan) and [ETSI ITS library](http://www.ttcn-3.org/index.php/development/devlibraries/devlib-libits).



### Building and Testing
This project uses [Maven](http://maven.apache.org/) as a build tool.

Most IDEs work with Maven projects directly, e.g. Eclipse supports Maven through [M2Eclipse](http://www.eclipse.org/m2e/).

Since this is a multi-module setup, Maven flags for multiple projects become useful (`--projects`, `--also-make`, `--also-make-dependents`). See [Maven docs](https://maven.apache.org/guides/mini/guide-multiple-modules.html) for manual, and a [Sonatype blog post](http://blog.sonatype.com/2009/10/maven-tips-and-tricks-advanced-reactor-options/) for a nice tutorial introduction.

### Running
Since this project is a library and not a standalone program, you need some main program. There are two examples of such main programs: one is [Upper Tester](https://github.com/alexvoronov/geonetworking/tree/master/uppertester) (used during the ETSI Plugtest), and another is the [Rendits Vehicle-to-Anything Router](https://github.com/rendits/router). The router replaces Vehicle Adapter that was used during GCDC. For more details see their respective documentation and the documentation of [GeoNetworking](https://github.com/alexvoronov/geonetworking/tree/master/geonetworking).

Here is an example of running the Upper Tester (assuming that udp2eth is already started):
```
mvn clean install

mvn --projects uppertester exec:java -Dexec.mainClass="net.gcdc.uppertester.ItsStation" -Dexec.args="--localPortForUdpLinkLayer 1237 --remoteAddressForUdpLinkLayer 192.168.159.102:1235 --upperTesterUdpPort 1600 --hasEthernetHeader"
```

### Citing
If you'd like to cite this GeoNetworking library or ASN.1 UPER encoder in an academic publication, you can use DOI [10.5281/zenodo.55650](http://dx.doi.org/10.5281/zenodo.55650). If you'd like to cite Vehicle Adapter that uses the library, you can use DOI [10.5281/zenodo.51295](http://dx.doi.org/10.5281/zenodo.51295).


### Acknowledgements
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).


### License

This code is released under the business-friendly Apache 2.0 license.
