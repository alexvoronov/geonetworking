package net.gcdc.asn1.datatypes;

import java.math.BigInteger;

public abstract class Asn1BigInteger {
    @Override public String toString() {
        return "" + value;
    }

    private final BigInteger value;

    public BigInteger value() { return value; }

    public Asn1BigInteger(final BigInteger value) {
        this.value = value;
    }


}
