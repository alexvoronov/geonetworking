package net.gcdc.geonetworking;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BtpPacket {

    private Optional<Short>         sourcePort;
    private short                   destinationPort;
    private Optional<Short>         destinationPortInfo;
    private byte[]                  payload;
    private Destination          destination;
    private Optional<TrafficClass>  trafficClass;
    private Optional<LongPositionVector> senderPosition;

    private byte[] gnPayload = null;

    private BtpPacket(
            Optional<Short>         sourcePort,
            short                   destinationPort,
            Optional<Short>         destinationPortInfo,
            byte[]                  payload,
            Destination          destination,
            Optional<TrafficClass>  trafficClass,
            Optional<LongPositionVector>            senderPosition
        ) {
        this.sourcePort          = sourcePort;
        this.destinationPort     = destinationPort;
        this.destinationPortInfo = destinationPortInfo;
        this.payload             = payload.clone();  // Defensive. Just in case.
        this.destination         = destination;
        this.trafficClass        = trafficClass;
        this.senderPosition      = senderPosition;
    }

//    public static BtpPacket beacon() {
//        return new BtpPacket();
//    }

    public static BtpPacket singleHop(byte[] payload, short destinationPort) {
        Optional<Short>         sourcePort          = Optional.empty();
        Optional<Short>         destinationPortInfo = Optional.empty();
        Optional<TrafficClass>  trafficClass        = Optional.empty();
        Optional<LongPositionVector> senderPosition = Optional.empty();

        return new BtpPacket(
                sourcePort,
                destinationPort,
                destinationPortInfo,
                payload,
                Destination.singleHop(),
                trafficClass,
                senderPosition
                );
    }

    public Optional<Short>         sourcePort()           { return sourcePort; }
    public short                   destinationPort()      { return destinationPort; }
    public Optional<Short>         destinationPortInfo () { return destinationPortInfo; }
    public byte[]                  payload ()             { return payload.clone(); }  // Extra defensive here. Just in case.
    public Destination          destination ()         { return destination; }
    public Optional<TrafficClass>  trafficClass ()        { return trafficClass; }
    public Optional<LongPositionVector>            senderPosition ()      { return senderPosition; }

    public byte[] asBytes() {
        if (gnPayload == null) {
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH + payload.length);
            gnPayload = putHeaderTo(buffer).put(payload).array();
        }
        return gnPayload;
    }

    public static final int HEADER_LENGTH = 4;

    private ByteBuffer putHeaderTo(ByteBuffer buffer) {
        return sourcePort.isPresent() ?
                buffer.putShort(sourcePort.get()).putShort(destinationPort) :
                buffer.putShort(destinationPort).putShort(destinationPortInfo.orElse((short)0x00));
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
                short port1 = buffer.getShort();
                short port2 = buffer.getShort();
                sourcePort          = isA ? Optional.of(port1)  :         emptyPort;
                destinationPort     = isA ?             port2   :             port1;
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
                throw new IllegalArgumentException("Bad BTP protocol: " + data.protocol);
        }
    }
}
