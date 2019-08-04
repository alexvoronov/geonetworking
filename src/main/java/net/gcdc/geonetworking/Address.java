package net.gcdc.geonetworking;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * The network address for the GeoAdhoc router entity in the ITS-S.
 *
 * 64-bit address encoded as 8-octets octet string with network byte order.
 *
 * From ASN1:  DISPLAY-HINT "2x:2x:2x:2x"
 *
 * <pre>
 *  Octets:         1               2               3
 *  0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1
 *  Bits:               10                  20
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5
 *  Bits backwards:           50                  40
 *  3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8 7 6 5 4 3 2 1 0 9 8
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |M|   Type  | Country Code      | MAC Address 48 bit
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *  1-1 2 3 4 5-1 2 3 4 5 6 7 8 9 0-1 2 3 4 5 6 ....
 * </pre>
 *
 * The class is declared as final, just because there was no need for subclasses yet.
 * If you remove final, make sure to take good care of {@link #equals(Object)} and
 * {@link #hashCode()}.
 */
public final class Address {


    public static final int LENGTH = 8;

    private final long value;

    public Address(long value) {
        this.value = value;
    }

    public Address(boolean isManual, StationType stationType, int countryCode, long lowLevelAddress) {
        this(
                ((isManual ? 1L : 0L)                            << 63) |  // bit  63
                ((stationType.value() & 0b0001_1111L)            << 58) |  // bits 62-58
                ((countryCode & 0b0011_1111_1111L)               << 48) |  // bits 57-48
                ((lowLevelAddress & 0x00_00_ff_ff_ff_ff_ff_ffL))          // bits 47-0
        );
        if (countryCode > 0b0011_1111_1111) {
            throw new IllegalArgumentException("Country code is outside of the range 0-" +
                    0b0011_1111_1111);
        }
    }

    /** For MANAGED address configuration, returns true if address is manually configured by
     * geonet Management, and false if the address from geonet Management.
     * For AUTO address configuration (station takes a random number) - undefined in standard? */
    public boolean isManual() {
        return (value >>> 63) != 0;
    }
    public StationType stationType() {
        return StationType.fromValue((int)((value >>> 58) & 0b0001_1111));
    }
    /** Returns country code.
     *
     * Country codes are according to Annex to ITU Operational Bulletin No. 741 - 1.VI.2001:
     * "Complement to Recommendation ITU-T E.212 (11/98)".
     * See http://www.itu.int/dms_pub/itu-t/opb/sp/T-SP-OB.741-2001-PDF-E.pdf
     */
    public int countryCode() {
        return (int)((value >>> 48) & 0b0011_1111_1111);
    }
    public long lowLevelAddress() {
        return value & 0x00_00_ff_ff_ff_ff_ff_ffL;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        // TODO: make sure that the length is correct!
        return new StringBuilder(Long.toHexString(value).toUpperCase()).toString();
//            .insert(12, ':')
//            .insert( 8, ':')
//            .insert( 4, ':')
//            .toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (value ^ (value >>> 32));
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
        Address other = (Address) obj;
        if (value != other.value)
            return false;
        return true;
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        return buffer.putLong(value);
    }

    public static Address getFrom(ByteBuffer buffer) {
        return new Address(buffer.getLong());
    }

    /**
     *
     * @param isManual Address is manually configured, e.g. after DAD, and is not an initial random.
     * @param stationType
     * @param countryCode
     * @return
     */
    public static Address random(boolean isManual, StationType stationType, int countryCode) {
        return new Address(isManual, stationType, countryCode,
                new Random().nextLong() & 0x00_00_ff_ff_ff_ff_ff_ffL);
    }
}
