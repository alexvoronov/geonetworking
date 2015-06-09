package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;

class BooleanCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        return obj instanceof Boolean;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
        UperEncoder.logger.debug("BOOLEAN {}", obj);
        bitbuffer.put((Boolean) obj);
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return Boolean.class.isAssignableFrom(classOfT)
                || boolean.class.isAssignableFrom(classOfT);
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        UperEncoder.logger.debug("BOOL");
        return (T) new Boolean(bitbuffer.get());
    }
}