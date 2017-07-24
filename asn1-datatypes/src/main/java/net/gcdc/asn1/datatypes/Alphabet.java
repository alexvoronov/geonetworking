package net.gcdc.asn1.datatypes;

/**
 * Alphabet class for Restricted Strings.
 *
 * Use {@link AlphabetBuilder} for convenient construction of restriction alphabets.
 */
public abstract class Alphabet {

    private final String chars;

    protected Alphabet(String chars) {
        this.chars = chars;
    }

    public final String chars() {
        return chars;
    }

}
