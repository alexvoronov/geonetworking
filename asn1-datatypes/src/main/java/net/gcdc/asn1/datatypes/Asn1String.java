package net.gcdc.asn1.datatypes;

public class Asn1String {

    private String value;

    public Asn1String() { this(""); }

    public Asn1String(String value) {
        this.value = value;
    }

    @Override public String toString() { return value; }

    public String value() { return value; }

}
