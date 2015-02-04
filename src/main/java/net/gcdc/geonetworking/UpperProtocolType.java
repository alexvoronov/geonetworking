package net.gcdc.geonetworking;

public enum UpperProtocolType {
    ANY     (0),
    BTP_A   (1),  // Carries the source and the destination port ("Don't use it" - ETSI Plugtest).
    BTP_B   (2),  // Carries the destination port, but no source port.
    IPv6    (3);
    private final int value;
    private UpperProtocolType(int value) { this.value = value; }
    public  int value()           { return value;       }

    public static UpperProtocolType fromCode(int code) {
        for (UpperProtocolType h: UpperProtocolType.values()) { if (h.value() == code) { return h; } }
        throw new IllegalArgumentException("Can't recognize upper protocol: " + code);
    }
}