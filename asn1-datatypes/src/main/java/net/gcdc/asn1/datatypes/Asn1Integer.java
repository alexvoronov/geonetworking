package net.gcdc.asn1.datatypes;

public abstract class Asn1Integer {

    public long value;

    public Asn1Integer() {}
    public Asn1Integer(long value) {
        this.value = value;
    }

    public long value() { return value; }

    @Override public String toString() {
        return "" + value;
    }








}
