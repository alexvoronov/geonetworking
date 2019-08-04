package net.gcdc.geonetworking;

import java.util.Arrays;

/**
 * Geonetworking Data to send and receive Geonetworking packets.
 *
 * The class contains both payload and information needed to populate all Geonetworking headers.
 *
 * The class is declared as final, just because there was no need for subclasses yet.
 * If you remove final, make sure to take good care of {@link #equals(Object)} and
 * {@link #hashCode()}, because in one possible implementation idea there was a plan to store
 * GeonetData in a Set.
 */
public final class GeonetData {

    public final UpperProtocolType            protocol;
    public final Destination                  destination;
    public final Optional<TrafficClass>       trafficClass;
    public final Optional<LongPositionVector> sender;
    public final byte[]                       payload;  // Final, but mutable content!

    public GeonetData(
            UpperProtocolType            protocol,
            Destination                  destination,
            Optional<TrafficClass>       trafficClass,
            Optional<LongPositionVector> sender,
            byte[]                       payload
            ) {
        this.protocol     = protocol;
        this.destination  = destination;
        this.trafficClass = trafficClass;
        this.sender       = sender;
        this.payload      = payload;
    }

    public GeonetData withSender(final Optional<LongPositionVector> sender) {
        return new GeonetData(this.protocol, this.destination, this.trafficClass, sender,
                this.payload.clone());
    }

    public GeonetData withDestination(final Destination destination) {
        return new GeonetData(this.protocol, destination, this.trafficClass, this.sender,
                this.payload.clone());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((destination == null) ? 0 : destination.hashCode());
        result = prime * result + Arrays.hashCode(payload);
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + ((trafficClass == null) ? 0 : trafficClass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GeonetData other = (GeonetData) obj;
        if (destination == null) {
            if (other.destination != null)
                return false;
        } else if (!destination.equals(other.destination))
            return false;
        if (!Arrays.equals(payload, other.payload))
            return false;
        if (protocol != other.protocol)
            return false;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        if (trafficClass == null) {
            if (other.trafficClass != null)
                return false;
        } else if (!trafficClass.equals(other.trafficClass))
            return false;
        return true;
    }

}