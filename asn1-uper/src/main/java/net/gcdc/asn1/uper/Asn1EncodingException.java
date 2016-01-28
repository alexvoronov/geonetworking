package net.gcdc.asn1.uper;

public class Asn1EncodingException extends Exception {

    private static final long serialVersionUID = 1L;

    public Asn1EncodingException(String message) {
        super(message);
    }

    public Asn1EncodingException(String extraMessage, Asn1EncodingException cause) {
        super(extraMessage + cause.getMessage(), cause);
    }

}
