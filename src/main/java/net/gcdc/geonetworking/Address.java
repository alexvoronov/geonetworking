package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

/**
 * The network address for the GeoAdhoc router entity in the ITS-S.
 *
 * 64-bit address encoded as 8-octets octet string with network byte order.
 *
 * From ASN1:  DISPLAY-HINT "2x:2x:2x:2x"
 */
public class Address {

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

    public static final int LENGTH = 8;

    private long value;

    public Address(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new StringBuilder(Long.toHexString(value).toUpperCase())
            .insert(12, ':')
            .insert( 8, ':')
            .insert( 4, ':')
            .toString();
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        return buffer.putLong(value);
    }

    public static Address getFrom(ByteBuffer buffer) {
        return new Address(buffer.getLong());
    }
}
