package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

public class BasicHeader {

    public static final int LENGTH = 4;  // Header length in bytes.

    private final byte       version;           // octet 0, bits 0-3
    private final NextHeader nextHeader;        // octet 0, bits 4-7
                                                // octet 1 (reserved)
    private final Lifetime   lifetime;          // octet 2
    private final byte       remainingHopLimit; // octet 3

    public byte       version()           { return version;           }
    public NextHeader nextHeader()        { return nextHeader;        }
    public Lifetime   lifetime()          { return lifetime;          }
    public byte       remainingHopLimit() { return remainingHopLimit; }

    public static enum NextHeader {
        ANY            (0),
        COMMON_HEADER  (1),
        SECURED_PACKET (2);
        private final int value;
        private NextHeader(int value) { this.value = value; }
        public  int value()           { return value;       }
        public static NextHeader fromValue(int value) {
            switch (value) {
                case  0: return ANY;
                case  1: return COMMON_HEADER;
                case  2: return SECURED_PACKET;
                default: throw new IllegalArgumentException("Bad next header for Basic Header: " +
                                                            value);
            }
            // "Universal" version:
            // for (NextHeader h : NextHeader.values()) { if (h.value() == value) { return h; } }
            // throw new IllegalArgumentException("Bad next header for Basic Header: " + value);
        }

    }

    /**
     * The Lifetime indicates the maximum tolerable time a packet can be buffered
     * until it reaches its destination.
     *
     *   NOTE1: This parameter is relevant for safety and traffic efficiency information that
     *          do not have strict real-time requirements. In sparse network scenarios, this
     *          lifetime can also be used to avoid re-transmission and forwarding of outdated
     *          information.
     *
     *   NOTE2: When a GeoNetworking packet is buffered,the value of the Lifetime field is
     *          reduced by the queuing time in the packet buffer.
     *
     * The following method for encoding of the LT field uses a non-linear encoding, which
     * provides a high resolution for low numbers and progressively lower resolution for
     * higher numbers.
     *
     * The LT field shall be comprised of two sub-fields: a LT_Multiplier sub-field
     * (Multiplier) and a LT_Base sub-field (Base) and shall be encoded as follows:
     *
     *       Lifetime_decoded = LT_Multiplier × LT_Base
     *
     * The LT_Base sub-field represents a two bit unsigned selector that chooses one out of
     * four predefined values as specified in enum LifetimeBases.
     *
     * The LT_Multiplier is a 6 bit unsigned integer, which represents a multiplier range from
     * 0 to 2^6 - 1 = 63.
     */
    public static class Lifetime {

        public static enum Base {
            X50MS  (  0.050, 0),  // Up to 3.15 seconds.
            X1S    (  1.000, 1),  // From 4 up to 63 seconds.
            X10S   ( 10.000, 2),  // From 64 (rounded to 60) up to  630 seconds.
            X100S  (100.000, 3);  // From 631 (rounded to 600) to 6300 seconds or 105 minutes.
            private final double seconds;
            private final int code;
            private Base(double value, int code) { this.seconds = value; this.code = code; }
            public  double asSeconds()           { return seconds;                         }
            public  int    code()                { return code;                            }

            public  static Base fromCode(int code) {
                switch (code) {
                    case  0: return X50MS;
                    case  1: return X1S;
                    case  2: return X10S;
                    case  3: return X100S;
                    default: throw new IllegalArgumentException("Bad lifetime base: " + code);
                }
            }
        }

        final static double MAX_MULTIPLIER = 63;  // Max unsigned 6-bit integer.

        public final static Lifetime MAX_VALUE = new Lifetime(Base.X100S, (byte)MAX_MULTIPLIER);

        private final byte multiplier;  // bits 0-5
        private final Base base;        // bits 6-7

        private Lifetime(Base base, byte multiplier) {
            this.base       = base;
            this.multiplier = multiplier;
        }

        public static Lifetime fromSeconds(double seconds) {
            Base currentBase = Base.values()[0];       // Start with the smallest base.
            for (Lifetime.Base b : Base.values()) {    // Increase base one by one...
                currentBase = b;                       // ...until base is big enough:
                if (currentBase.asSeconds() * MAX_MULTIPLIER > seconds)  { break; }
            }
            byte multiplier = (byte) (seconds / currentBase.asSeconds());
            return new Lifetime(currentBase, multiplier);
        }

        public static Lifetime fromByte(byte b) {
            byte multiplier = (byte) (b >> 2);           // Drop bits 6-7, keep only bits 0-5.
            Base base = Base.fromCode(b & 0b0000_0011);  // Keep only bits 6-7.
            return new Lifetime(base, multiplier);
        }

        public double asSeconds() {
            return base.asSeconds() * multiplier;
        }

        public byte asByte() {
            return (byte) (multiplier << 2 | base.code());
        }

    }

    public BasicHeader(
            byte version,
            NextHeader nextHeader,
            Lifetime lifetime,
            byte remainingHopLimit
            ) {
        this.version              = version;
        this.nextHeader           = nextHeader;
        this.lifetime             = lifetime;
        this.remainingHopLimit    = remainingHopLimit;
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        byte versionAndNextHeader = (byte) (version << 4 | nextHeader.value());
        byte reserved = 0x00;
        return buffer
                .put(versionAndNextHeader)     // octet 0
                .put(reserved)                 // octet 1
                .put(lifetime.asByte())        // octet 2
                .put(remainingHopLimit);       // octet 3
    }

    public static BasicHeader getFrom(ByteBuffer buffer) {
        byte versionAndNextHeader = buffer.get();
        byte reserved             = buffer.get();
        byte lifetimeByte         = buffer.get();
        byte remainingHopLimit    = buffer.get();
        byte version = (byte) (versionAndNextHeader >> 4);
        NextHeader nextHeader = NextHeader.fromValue(versionAndNextHeader & 0b0000_1111);
        Lifetime lifetime = Lifetime.fromByte(lifetimeByte);
        return new BasicHeader(version, nextHeader, lifetime, remainingHopLimit);
    }
}