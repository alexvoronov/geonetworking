package net.gcdc.geonetworking;

public enum DestinationType {
    ANY                      (0,0),
    BEACON                   (1,0),
    GEOUNICAST               (2,0),
    GEOANYCAST_CIRCLE        (3,0),
    GEOANYCAST_RECTANGLE     (3,1),
    GEOANYCAST_ELLIPSE       (3,2),
    GEOBROADCAST_CIRCLE      (4,0),
    GEOBROADCAST_RECTANGLE   (4,1),
    GEOBROADCAST_ELLIPSE     (4,2),
    SINGLE_HOP               (5,0),
    MULTI_HOP                (5,1),
    LOCATION_SERVICE_REQUEST (6,0),
    LOCATION_SERVICE_REPLY   (6,1);

    private final int headerType;
    private final int headerSubtype;

    private DestinationType(int type, int subtype) {
        this.headerType     =  type;
        this.headerSubtype  =  subtype;
    }

    public int  type()    { return headerType; }
    public int  subtype() { return headerSubtype; }
    public byte asByte()  { return (byte) (headerType << 4 | headerSubtype); }

    public static DestinationType fromByte(byte b) {
        for (DestinationType hts: DestinationType.values()) {
            if (hts.asByte() == b) { return hts; }
        }
        throw new IllegalArgumentException("Can't recognize packet type and subtype: " + b);
    }
}