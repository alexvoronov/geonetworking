package net.gcdc.geonetworking;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MacAddress {
    private final long address;

    public MacAddress(long address) {
        if ((address & 0xffff_0000_0000_0000L) != 0) {
            throw new IllegalArgumentException("address is longer than 6 bytes: " + address);
        }
        this.address = address;
    }

    public long value() { return address; }

    /** Returns 6-bytes array representing this address. */
    public byte[] asBytes() {
        return Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(address).array(), 2, 8);
    }

    public static MacAddress fromBytes(byte[] bytes) {
        if (bytes.length > 6) {
            throw new IllegalArgumentException("Address is too long: " + bytes.length + "bytes");
        }
        return new MacAddress(new BigInteger(+1, bytes).longValue());
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (address ^ (address >>> 32));
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MacAddress other = (MacAddress) obj;
        if (address != other.address) return false;
        return true;
    }
}
