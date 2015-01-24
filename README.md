# GeoNetworking stack

A basic implementation of the [ETSI](http://en.wikipedia.org/wiki/ETSI) GeoNetworking stack based on 'Geographical addressing and forwarding for point-to-point and point-to-multipoint communications' ([ETSI EN 302 636-4-1](http://webapp.etsi.org/wprogram/Report_WorkItem.asp?WKI_ID=38232)) and 'Transport Protocols; Sub-part 1: Basic Transport Protocol' ([ETSI EN 302 636-5-1](http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=38233)).


### Scope

Only Single Hop Broadcast is supported at the moment, and GeoBroadcast is planned. There are currently no plans to implement GeoAnycast or GeoUnicast, and no plans to implement security either.


### Implementation

API here is work-in-progress, and is expected to change when usage patterns emerge. Feedback is welcome.

The implementation assumes that there is a separate entity (e.g. on the same or on a separate machine) taking care of [Link Layer](http://en.wikipedia.org/wiki/Link_layer) communication. Communication between that entity and GeoNetworking stack is implemented via UDP at the moment.

Implementation targets Java 7 at the moment, with some use of backported features from Java 8.


### Building, Testing and Running

[SBT](http://www.scala-sbt.org/) is used as a build tool. Later it should be changed to something else. 

Running tests: 
```
sbt test
```

Compile and run command-line client:
```
sbt assembly
echo 4001 127.0.0.1:4000 | java -cp target/scala-2.10/geonetworking-assembly-0.1.0-SNAPSHOT.jar net.gcdc.DuplicatorUdpServer &
java -cp target/scala-2.10/geonetworking-assembly-0.1.0-SNAPSHOT.jar net.gcdc.StdinClient 4000 127.0.0.1:4001
```

### Other GeoNetworking implementations

This implementation was inspired by [DriveITS](https://github.com/Dimme/driveits). Another open-source implementation, now outdated, is [CarGeo6](http://www.cargeo6.org/).


### License

Apache License Version 2.0.
