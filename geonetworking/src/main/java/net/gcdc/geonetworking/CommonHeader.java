package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

public final class CommonHeader {
    private final UpperProtocolType nextHeader;
    private final DestinationType   typeAndSubtype;
    private final TrafficClass      trafficClass;
    private final boolean           isMobile;
    private final short             payloadLength;
    private final byte              maximumHopLimit;

    /** Header length in bytes. */
    public static final int LENGTH = 8;

    public UpperProtocolType nextHeader()      { return nextHeader;      }
    public DestinationType   typeAndSubtype()  { return typeAndSubtype;  }
    public TrafficClass      trafficClass()    { return trafficClass;    }
    public boolean           isMobile()        { return isMobile;        }
    public short             payloadLength()   { return payloadLength;   }
    public byte              maximumHopLimit() { return maximumHopLimit; }

    public CommonHeader(
            UpperProtocolType nextHeader,
            DestinationType   typeAndSubtype,
            TrafficClass      trafficClass,
            boolean           isMobile,
            short             payloadLength,
            byte              maximumHopLimit)
    {
        this.nextHeader      = nextHeader;
        this.typeAndSubtype  = typeAndSubtype;
        this.trafficClass    = trafficClass;
        this.isMobile        = isMobile;
        this.payloadLength   = payloadLength;
        this.maximumHopLimit = maximumHopLimit;
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        byte  nextHeaderAndReserved = (byte) (nextHeader.value() << 4);
        byte  flags                 = (byte) ((isMobile? 1 : 0) << 7);  // Bit 0 isMobile, bits 1-7 reserved.
        byte  reserved              = 0x00;
        return buffer
            .put(nextHeaderAndReserved)
            .put(typeAndSubtype.asByte())
            .put(trafficClass.asByte())
            .put(flags)
            .putShort(payloadLength)
            .put(maximumHopLimit)
            .put(reserved);
    }

    public static CommonHeader getFrom(ByteBuffer buffer) {
        byte nextHeaderAndReserved = buffer.get();
        byte typeAndSubtypeByte    = buffer.get();
        byte trafficClassByte      = buffer.get();
        byte flags                 = buffer.get();
        short payloadLength        = buffer.getShort();
        byte maxHopLimit           = buffer.get();
        return new CommonHeader(
                UpperProtocolType.fromCode(nextHeaderAndReserved >> 4),
                DestinationType.fromByte(typeAndSubtypeByte),
                TrafficClass.fromByte(trafficClassByte),
                ((flags & 0xFF)>> 7)==1,  // '& 0xFF' converts signed byte to int.
                payloadLength,
                maxHopLimit);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isMobile ? 1231 : 1237);
        result = prime * result + maximumHopLimit;
        result = prime * result + ((nextHeader == null) ? 0 : nextHeader.hashCode());
        result = prime * result + payloadLength;
        result = prime * result + ((trafficClass == null) ? 0 : trafficClass.hashCode());
        result = prime * result + ((typeAndSubtype == null) ? 0 : typeAndSubtype.hashCode());
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
        CommonHeader other = (CommonHeader) obj;
        if (isMobile != other.isMobile)
            return false;
        if (maximumHopLimit != other.maximumHopLimit)
            return false;
        if (nextHeader != other.nextHeader)
            return false;
        if (payloadLength != other.payloadLength)
            return false;
        if (trafficClass == null) {
            if (other.trafficClass != null)
                return false;
        } else if (!trafficClass.equals(other.trafficClass))
            return false;
        if (typeAndSubtype != other.typeAndSubtype)
            return false;
        return true;
    }

}