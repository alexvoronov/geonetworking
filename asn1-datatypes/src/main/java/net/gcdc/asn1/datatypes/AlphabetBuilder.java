package net.gcdc.asn1.datatypes;


public class AlphabetBuilder {

    private final StringBuilder sb = new StringBuilder();

    public AlphabetBuilder() {}

    public String chars() {
        return sb.toString();
    }

    public AlphabetBuilder withRange(char from, char to) {
        for (char c = from; c <= to; c++) {
            sb.append(c);
        }
        return this;
    }

    public AlphabetBuilder withChars(String str) {
        sb.append(str);
        return this;
    }

    public AlphabetBuilder withChars(Character... chars) {
        for (char c : chars) {
            sb.append(c);
        }
        return this;
    }
}
