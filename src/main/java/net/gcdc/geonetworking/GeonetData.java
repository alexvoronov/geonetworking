package net.gcdc.geonetworking;

public class GeonetData {

    public final UpperProtocolType      protocol;
    public final Destination            destination;
    public final Optional<TrafficClass> trafficClass;
    public final Optional<LongPositionVector>     sender;
    public final byte[]                 payload;  // Final, but mutable content!

    public GeonetData(
            UpperProtocolType      protocol,
            Destination            destination,
            Optional<TrafficClass> trafficClass,
            Optional<LongPositionVector>     sender,
            byte[]                 payload
            ) {
        this.protocol     = protocol;
        this.destination  = destination;
        this.trafficClass = trafficClass;
        this.sender       = sender;
        this.payload      = payload;
    }
}