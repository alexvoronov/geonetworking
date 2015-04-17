package net.gcdc.asn1.datatypes;

public class Asn1String {

    @Override public String toString() { return value; }

    private String value;

    public String value() { return value; }

    public Asn1String(String value) {
        this.value = value;
    }

    public Asn1String() { this(""); }
}
