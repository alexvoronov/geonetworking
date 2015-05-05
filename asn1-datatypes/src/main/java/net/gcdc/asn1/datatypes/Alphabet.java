package net.gcdc.asn1.datatypes;

public abstract class Alphabet {

    private final String chars;

    public final String chars() {
        return chars;
    }

    protected Alphabet(String chars) {
        this.chars = chars;
    }
}
