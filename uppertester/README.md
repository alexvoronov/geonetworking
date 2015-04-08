# Upper Tester Application for ITS CMS4 Plugtest

This is an Upper Tester Application for [geonetworking](https://github.com/alexvoronov/geonetworking/geonetworking) library to participate in ETSI Plugtest on Intelligent Transportation Systems - Cooperative Mobility Services Event 4 ([ITS CMS4](http://www.etsi.org/news-events/events/846-plugtests-2015-itscms4)). Message set for this Upper Tester Application is defined in [ETSI TR 103 099](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=42425) "Architecture of conformance validation framework".

### Status

This Upper Tester Application was successfully used to do Interoperability Testing and Conformance Testing of the geonetworking library at the Plugtest. There are no plans to develop it any further, except maybe for participation in another Plugtest, or for automated Conformance Testing after each build in spirit of Unit/Integration testing (this would require TTCN-3).

### Usage

Geonetworking library requires a separate Link Layer entity, you can use e.g. [utoepy](https://github.com/alexvoronov/utoepy) or `udp2eth`.

First, start `utoepy`:

```
sudo python udp2eth.py 4000 en0

sudo python eth2udp.py 127.0.0.1:4001 en0
```

Then start Upper Tester Application. Example here uses geographical coordinates (57,13) and listens for Upper Tester Messages on UDP port 1600:

```
mvn exec:java -Dexec.mainClass="net.gcdc.uppertester.ItsStation" -Dexec.args="--lat 57 --lon 13 --localPortForUdpLinkLayer 4001 --remoteAddressForUdpLinkLayer 127.0.0.1:4000 --upperTesterUdpPort 1600"
```

Here's another example:

```
mvn clean compile exec:java -Dexec.mainClass="net.gcdc.uppertester.ItsStation" -Dexec.args="--localPortForUdpLinkLayer 1237 --remoteAddressForUdpLinkLayer 192.168.159.102:1235 --upperTesterUdpPort 1600 --hasEthernetHeader --gpsdServerAddress 10.200.0.3:1944"
```

### Acknowledgements
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).


### License

This Upper Tester Application source code is released under the business-friendly Apache 2.0 license.
