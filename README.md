# ETSI ITS stack [![Build Status](https://travis-ci.org/alexvoronov/geonetworking.svg?branch=master)](https://travis-ci.org/alexvoronov/geonetworking) [![Coverage Status](https://coveralls.io/repos/alexvoronov/geonetworking/badge.svg)](https://coveralls.io/r/alexvoronov/geonetworking)


A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) ITS stack including: [CAM-DENM](https://github.com/alexvoronov/geonetworking/tree/master/camdenm) / [ASN.1](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper) / [BTP / GeoNetworking](https://github.com/alexvoronov/geonetworking/tree/master/geonetworking). For lower layer (DataLinkLayer/AccessTechnologies) see [udp2eth](https://github.com/jandejongh/udp2eth) and [utoepy](https://github.com/alexvoronov/utoepy). For even lower layers, see section Hardware and drivers below.


### Status

Sending CAM via Single Hop Broadcast and DENM via GeoBroadcast is supported. Forwarding of GeoBroadcast packets is on the wishlist. There are currently no plans for GeoUnicast or Security.

Basic functionality was tested at an ETSI Plugtest in March 2015, and many things already worked. However, this is not a production-ready code. Even API is expected to change when usage patterns emerge, feedback is welcome! 

One way to improve the quality is to set up an automated conformance testing using open-source TTCN-3 [Eclipse Titan](https://projects.eclipse.org/projects/tools.titan) and [ETSI ITS library](http://www.ttcn-3.org/index.php/development/devlibraries/devlib-libits). 


### Hardware and drivers

For 802.11p Linux drivers, see, for example, modified [ath5k](https://wireless.wiki.kernel.org/en/users/drivers/ath5k) and [ath9k](https://wireless.wiki.kernel.org/en/users/drivers/ath9k) drivers by [CTU-IIG](https://github.com/CTU-IIG) ([kernel](https://github.com/CTU-IIG/802.11p-linux), [iw](https://github.com/CTU-IIG/802.11p-iw), [regdb](https://github.com/CTU-IIG/802.11p-wireless-regdb), [crda](https://github.com/CTU-IIG/802.11p-crda)) or by [Componentality](https://github.com/Componentality). FreeBSD wiki has a [summary of how 11p is different from 11a](https://wiki.freebsd.org/802.11p), and even stackoverflow has an [answer with a summary of needed driver changes](http://stackoverflow.com/a/10076012). For hardware for ath5k, see cards based on Atheros AR5414 chipset, like [Mikrotik R52H](http://routerboard.com/R52H) (for more hardware see wiki lists on [kernel.org](https://wireless.wiki.kernel.org/en/users/drivers/ath5k#supported_devices) or [debian.org](https://wiki.debian.org/ath5k#Supported_Devices)). For hardware for ath9k, see Atheros AR92xx-based cards, like [Compex WLE200NX](http://www.pcengines.ch/wle200nx.htm). [Mikrotik wiki](http://wiki.mikrotik.com/wiki/Manual:Wireless_Advanced_Channels) says since 2011 that only AR92xx chips support the needed 10 MHz channels, but maybe it has changed since then? Any updates welcome! For example, whether or not [Mikrotik R11e-5HnD](http://routerboard.com/R11e-5HnD) based on AR9580 works. For more ath9k devices see wiki lists on [kernel.org](https://wireless.wiki.kernel.org/en/users/drivers/ath9k/products) or [debian.org](https://wiki.debian.org/ath9k#Supported_Devices). Examples of cheap small computers to install the wifi cards are [Alix board](http://www.pcengines.ch/alix.htm) and it's successor [apu platform](http://www.pcengines.ch/apu.htm); another small computer with Mini PCI Express is [Hummingboard](https://www.solid-run.com/products/hummingboard/). As a Linux distro, try [Voyage Linux](http://linux.voyage.hk/). Note that the Linux machine with the wifi card don't have to be the same as the one running this GeoNetworking stack (it can be two separate machines talking UDP to each other). Any other information about hardware and drivers is greatly appreciated!



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
