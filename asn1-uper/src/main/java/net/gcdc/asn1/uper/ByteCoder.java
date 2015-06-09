package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;

class ByteCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        return obj instanceof Byte;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
        UperEncoder.encodeConstrainedInt(bitbuffer, ((Byte) obj).byteValue() & 0xff, 0, 255);
        UperEncoder.logger.debug("BYTE {}", ((Byte) obj).byteValue());
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return Byte.class.isAssignableFrom(classOfT) || byte.class.isAssignableFrom(classOfT);
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        UperEncoder.logger.debug("BYTE");
        return (T) new Byte((byte) UperEncoder.decodeConstrainedInt(bitbuffer, UperEncoder.newRange(0, 255, false)));
    }

}