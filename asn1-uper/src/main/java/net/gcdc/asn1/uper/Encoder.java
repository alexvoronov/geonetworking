package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;

public interface Encoder {
    <T> boolean canEncode(T obj, Annotation[] extraAnnotations);
    <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException;
}
