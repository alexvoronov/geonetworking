package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.math.BigInteger;

import net.gcdc.asn1.datatypes.Asn1BigInteger;
import net.gcdc.asn1.datatypes.IntRange;

class BigIntCoder implements Encoder, Decoder {

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return Asn1BigInteger.class.isAssignableFrom(classOfT);
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("BIG INT");
        IntRange intRange = annotations.getAnnotation(IntRange.class);
        if (intRange != null) { throw new UnsupportedOperationException(
                "Big int with range is not supported yet"); }
        int lengthInOctets = (int) UperEncoder.decodeLengthDeterminant(bitbuffer);
        BitBuffer valueBits = ByteBitBuffer.allocate(lengthInOctets * 8);
        for (int i = 0; i < lengthInOctets * 8; i++) {
            valueBits.put(bitbuffer.get());
        }
        valueBits.flip();
        BigInteger resultValue = new BigInteger(+1, valueBits.array());
        UperEncoder.logger.debug("big int Decoded as {}", resultValue);
        return UperEncoder.instantiate(classOfT, resultValue);
    }

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        return obj instanceof Asn1BigInteger;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        IntRange range = annotations.getAnnotation(IntRange.class);
        if (range != null) { throw new UnsupportedOperationException(
                "Asn1 BigInteger with range is not supported yet"); }
        byte[] array = ((Asn1BigInteger) obj).value().toByteArray();
        int lengthInOctets = array.length;
        int position1 = bitbuffer.position();
        try {
            UperEncoder.encodeLengthDeterminant(bitbuffer, lengthInOctets);
        } catch (Asn1EncodingException e) {
            throw new Asn1EncodingException(" length determinant of " + type.getName(), e);
        }
        int position2 = bitbuffer.position();
        for (byte b : array) {
            bitbuffer.putByte(b);
        }
        UperEncoder.logger.debug("Big Int({}): len {}, val {}", obj,
                bitbuffer.toBooleanString(position1, position2 - position1),
                bitbuffer.toBooleanStringFromPosition(position2));
        return;
    }

}