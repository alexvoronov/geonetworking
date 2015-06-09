package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;

public interface Decoder {
    <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations);
    <T> T decode(BitBuffer bitbuffer, Class<T> classOfT, Annotation[] extraAnnotations);
}
