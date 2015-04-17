package net.gcdc.asn1.datatypes;

public abstract class Asn1Integer {
    @Override public String toString() {
        return "" + value;
    }

    public long value;

    public long value() { return value; }

    public Asn1Integer() {}

    public Asn1Integer(long value) {
        this.value = value;
    }


}
