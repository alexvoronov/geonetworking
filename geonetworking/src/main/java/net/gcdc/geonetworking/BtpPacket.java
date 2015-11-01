package net.gcdc.geonetworking;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BtpPacket {

    private final Optional<Short>              sourcePort;
    private final short                        destinationPort;
    private final Optional<Short>              destinationPortInfo;
    private final byte[]                       payload;  // Final reference, but mutable content!
    private final Destination                  destination;
    private final Optional<TrafficClass>       trafficClass;
    private final Optional<LongPositionVector> senderPosition;

    private byte[] gnPayload = null;

    private BtpPacket(
            Optional<Short>              sourcePort,
            short                        destinationPort,
            Optional<Short>              destinationPortInfo,
            byte[]                       payload,
            Destination                  destination,
            Optional<TrafficClass>       trafficClass,
            Optional<LongPositionVector> senderPosition
        ) {
        this.sourcePort          = sourcePort;
        this.destinationPort     = destinationPort;
        this.destinationPortInfo = destinationPortInfo;
        this.payload             = payload.clone();  // Defensive. Just in case.
        this.destination         = destination;
        this.trafficClass        = trafficClass;
        this.senderPosition      = senderPosition;
    }

    public static BtpPacket singleHop(byte[] payload, short destinationPort) {
        Optional<Short>              emptySourcePort          = Optional.empty();
        Optional<Short>              emptyDestinationPortInfo = Optional.empty();
        Optional<TrafficClass>       emptyTrafficClass        = Optional.empty();
        Optional<LongPositionVector> emptySenderPosition      = Optional.empty();

        return new BtpPacket(
                emptySourcePort,
                destinationPort,
                emptyDestinationPortInfo,
                payload,
                Destination.singleHop(),
                emptyTrafficClass,
                emptySenderPosition
                );
    }

    public static BtpPacket singleHop(byte[] payload, short destinationPort, double maxLifetimeSec) {
        Optional<Short>              emptySourcePort          = Optional.empty();
        Optional<Short>              emptyDestinationPortInfo = Optional.empty();
        Optional<TrafficClass>       emptyTrafficClass        = Optional.empty();
        Optional<LongPositionVector> emptySenderPosition      = Optional.empty();

        return new BtpPacket(
                emptySourcePort,
                destinationPort,
                emptyDestinationPortInfo,
                payload,
                Destination.singleHop().withMaxLifetimeSeconds(maxLifetimeSec),
                emptyTrafficClass,
                emptySenderPosition
                );
    }
    /** Dummy BTP-A packet for Plugtest. */
    public static BtpPacket singleHopEmptyA(short destinationPort, short sourcePort) {
        Optional<Short>              emptyDestinationPortInfo = Optional.empty();
        Optional<TrafficClass>       emptyTrafficClass        = Optional.empty();
        Optional<LongPositionVector> emptySenderPosition      = Optional.empty();

        return new BtpPacket(
                Optional.of(sourcePort),
                destinationPort,
                emptyDestinationPortInfo,
                new byte[] {},
                Destination.singleHop(),
                emptyTrafficClass,
                emptySenderPosition
                );
    }

    /** Dummy BTP-B packet for Plugtest. */
    public static BtpPacket singleHopEmptyB(short destinationPort, short DestinationPortInfo) {
        Optional<Short>              emptySourcePort          = Optional.empty();
        Optional<TrafficClass>       emptyTrafficClass        = Optional.empty();
        Optional<LongPositionVector> emptySenderPosition      = Optional.empty();

        return new BtpPacket(
                emptySourcePort,
                destinationPort,
                Optional.of(DestinationPortInfo),
                new byte[] {},
                Destination.singleHop(),
                emptyTrafficClass,
                emptySenderPosition
                );
    }

    public static BtpPacket customDestination(byte[] payload, short destinationPort,
            Destination destination) {
        Optional<Short>              emptySourcePort          = Optional.empty();
        Optional<Short>              emptyDestinationPortInfo = Optional.empty();
        Optional<TrafficClass>       emptyTrafficClass        = Optional.empty();
        Optional<LongPositionVector> emptySenderPosition      = Optional.empty();

        return new BtpPacket(
                emptySourcePort,
                destinationPort,
                emptyDestinationPortInfo,
                payload,
                destination,
                emptyTrafficClass,
                emptySenderPosition
                );
    }

    public Optional<Short>              sourcePort()           { return sourcePort;          }
    public short                        destinationPort()      { return destinationPort;     }
    public Optional<Short>              destinationPortInfo () { return destinationPortInfo; }
    public byte[]                       payload ()             { return payload.clone();     }  // Extra defensive here. Just in case.
    public Destination                  destination ()         { return destination;         }
    public Optional<TrafficClass>       trafficClass ()        { return trafficClass;        }
    public Optional<LongPositionVector> senderPosition ()      { return senderPosition;      }

    public byte[] asBytes() {
        if (gnPayload == null) {
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + payload.length);
            gnPayload = putHeaderTo(buffer).put(payload).array();
        }
        return gnPayload;
    }

    public static final int HEADER_LENGTH = 4;

    private ByteBuffer putHeaderTo(ByteBuffer buffer) {
        return buffer.putShort(destinationPort)
                     .putShort(sourcePort.orElse(destinationPortInfo.orElse((short)0x00)));
    }

    public static BtpPacket fromGeonetData(GeonetData data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.payload);
        Optional<Short>         sourcePort;
        short                   destinationPort;
        Optional<Short>         destinationPortInfo;
        switch (data.protocol) {
            case BTP_A:
            case BTP_B: {  // Carries the source and the destination port.
                boolean isA = data.protocol == UpperProtocolType.BTP_A;
                Optional<Short> emptyPort = Optional.empty();
                destinationPort = buffer.getShort();
                short port2 = buffer.getShort();
                sourcePort          = isA ? Optional.of(port2)  :         emptyPort;
                destinationPortInfo = isA ?           emptyPort : Optional.of(port2);
                byte[] btpPayload = Arrays.copyOfRange(data.payload, HEADER_LENGTH,
                        data.payload.length);
                return new BtpPacket(
                        sourcePort,
                        destinationPort,
                        destinationPortInfo,
                        btpPayload,
                        data.destination,
                        data.trafficClass,
                        data.sender
                        );
            }
            case ANY:
            case IPv6:
            default:
                throw new IllegalArgumentException("Unsupported BTP protocol: " + data.protocol);
        }
    }
}
