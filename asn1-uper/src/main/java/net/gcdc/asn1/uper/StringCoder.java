package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.DefaultAlphabet;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.SizeRange;

class StringCoder implements Decoder, Encoder {

    private static final String  CHAR_DECORATION = "decoded more than one char (";

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        return obj instanceof String || obj instanceof Asn1String;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("STRING {} of type {}", obj, obj.getClass().getName());
        String string = (obj instanceof String) ? ((String) obj) : ((Asn1String) obj).value();
        RestrictedString restrictionAnnotation = annotations
                .getAnnotation(RestrictedString.class);
        if (restrictionAnnotation == null) { throw new UnsupportedOperationException(
                "Unrestricted character strings are not supported yet. All annotations: "
                        + Arrays.asList(type.getAnnotations())); }
        FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
        SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
        if (fixedSize != null && fixedSize.value() != string.length()) { throw new IllegalArgumentException(
                "Bad string length, expected " + fixedSize.value() + ", got " + string.length()); }
        if (sizeRange != null
                && !sizeRange.hasExtensionMarker()
                && (string.length() < sizeRange.minValue() || sizeRange.maxValue() < string
                        .length())) { throw new IllegalArgumentException(
                "Bad string length, expected " + sizeRange.minValue() + ".."
                        + sizeRange.maxValue() + ", got " + string.length()); }
        if (restrictionAnnotation.value() == CharacterRestriction.UTF8String) {  // UTF8 length
                                                                                BitBuffer stringbuffer = ByteBitBuffer.createInfinite();
            for (char c : string.toCharArray()) {
                encodeChar(stringbuffer, c, restrictionAnnotation);
            }
            stringbuffer.flip();
            if (stringbuffer.limit() % 8 != 0) { throw new AssertionError(
                    "utf8 encoding resulted not in multiple of 8 bits"); }
            int numOctets = (stringbuffer.limit() + 7) / 8;  // Actually +7 is not needed here,
                                                            // since we already checked with %8.
            int position1 = bitbuffer.position();
            UperEncoder.encodeLengthDeterminant(bitbuffer, numOctets);
            UperEncoder.logger.debug("UTF8String {},  length {} octets, encoded as {}", string, numOctets,
                    bitbuffer.toBooleanStringFromPosition(position1));
            int position2 = bitbuffer.position();
            for (int i = 0; i < stringbuffer.limit(); i++) {
                bitbuffer.put(stringbuffer.get());
            }
            UperEncoder.logger.debug("UTF8String {}, encoded length {} octets, value bits: {}", string,
                    numOctets, bitbuffer.toBooleanStringFromPosition(position2));
            return;
        } else if (fixedSize != null) {
            if (fixedSize.value() != string.length()) { throw new IllegalArgumentException(
                    "String length does not match constraints"); }
            int position = bitbuffer.position();
            for (int i = 0; i < fixedSize.value(); i++) {
                encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
            }
            UperEncoder.logger.debug("string encoded as <{}>",
                    bitbuffer.toBooleanStringFromPosition(position));
            return;
        } else if (sizeRange != null) {
            UperEncoder.logger.debug("string length");
            UperEncoder.encodeConstrainedInt(bitbuffer, string.length(), sizeRange.minValue(),
                    sizeRange.maxValue(), sizeRange.hasExtensionMarker());
            UperEncoder.logger.debug("string content");
            for (int i = 0; i < string.length(); i++) {
                encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
            }
            // logger.debug("string of type {} size {}: {}", obj.getClass().getName(),
            // binaryStringFromCollection(lengthBits), binaryStringFromCollection(valuebits));
            return;
        } else {
            int position1 = bitbuffer.position();
            UperEncoder.encodeLengthDeterminant(bitbuffer, string.length());
            int position2 = bitbuffer.position();
            for (int i = 0; i < string.length(); i++) {
                encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
            }
            UperEncoder.logger.debug("STRING {} size {}: {}", obj.getClass().getName(),
                    bitbuffer.toBooleanString(position1, position2 - position1),
                    bitbuffer.toBooleanStringFromPosition(position2));
            return;
        }
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return String.class.isAssignableFrom(classOfT)
                || Asn1String.class.isAssignableFrom(classOfT);
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("String");
        RestrictedString restrictionAnnotation = annotations
                .getAnnotation(RestrictedString.class);
        if (restrictionAnnotation == null) { throw new UnsupportedOperationException(
                "Unrestricted character strings are not supported yet. All annotations: "
                        + Arrays.asList(classOfT.getAnnotations())); }
        if (restrictionAnnotation.value() == CharacterRestriction.UTF8String) {
            long numOctets = UperEncoder.decodeLengthDeterminant(bitbuffer);
            List<Boolean> content = new ArrayList<Boolean>();
            for (int i = 0; i < numOctets * 8; i++) {
                content.add(bitbuffer.get());
            }
            byte[] contentBytes = UperEncoder.bytesFromCollection(content);
            String resultStr = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(contentBytes))
                    .toString();
            T result = UperEncoder.instantiate(classOfT, resultStr);
            return result;
        } else {
            FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
            SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
            long numChars = (fixedSize != null) ? fixedSize.value() :
                    (sizeRange != null) ? UperEncoder.decodeConstrainedInt(bitbuffer,
                            UperEncoder.intRangeFromSizeRange(sizeRange)) :
                            UperEncoder.decodeLengthDeterminant(bitbuffer);
            UperEncoder.logger.debug("known-multiplier string, numchars: {}", numChars);
            StringBuilder stringBuilder = new StringBuilder((int) numChars);
            for (int c = 0; c < numChars; c++) {
                stringBuilder.append(decodeRestrictedChar(bitbuffer, restrictionAnnotation));
            }
            String resultStr = stringBuilder.toString();
            UperEncoder.logger.debug("Decoded as {}", resultStr);
            T result = UperEncoder.instantiate(classOfT, resultStr);
            return result;
        }
    }

    private static void encodeChar(BitBuffer bitbuffer, char c, RestrictedString restriction) throws Asn1EncodingException {
        UperEncoder.logger.debug("char {}", c);
        switch (restriction.value()) {
            case IA5String:
                if (restriction.alphabet() != DefaultAlphabet.class) { throw new UnsupportedOperationException(
                        "alphabet for IA5String is not supported yet."); }
                UperEncoder.encodeConstrainedInt(
                        bitbuffer,
                        StandardCharsets.US_ASCII.encode(CharBuffer.wrap(new char[] { c })).get() & 0xff,
                        0,
                        127);
                return;
            case UTF8String:
                if (restriction.alphabet() != DefaultAlphabet.class) { throw new UnsupportedOperationException(
                        "alphabet for UTF8 is not supported yet."); }
                ByteBuffer buffer = StandardCharsets.UTF_8
                        .encode(CharBuffer.wrap(new char[] { c }));
                for (int i = 0; i < buffer.limit(); i++) {
                    UperEncoder.encodeConstrainedInt(bitbuffer, buffer.get() & 0xff, 0, 255);
                }
                return;
            case VisibleString:
            case ISO646String:
                if (restriction.alphabet() != DefaultAlphabet.class) {
                    char[] chars;
                    try {
                        chars = UperEncoder.instantiate(restriction.alphabet()).chars().toCharArray();
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Uninstantinatable alphabet"
                                + restriction.alphabet().getName());
                    }
                    if (BigInteger.valueOf(chars.length - 1).bitLength() < BigInteger.valueOf(126)
                            .bitLength()) {
                        Arrays.sort(chars);
                        String strAlphabet = new String(chars);
                        int index = strAlphabet.indexOf(c);
                        if (index < 0) { throw new IllegalArgumentException("can't find character "
                                + c + " in alphabet " + strAlphabet); }
                        UperEncoder.encodeConstrainedInt(
                                bitbuffer,
                                index,
                                0,
                                chars.length - 1);
                        return;
                    } else {
                        UperEncoder.encodeConstrainedInt(
                                bitbuffer,
                                StandardCharsets.US_ASCII.encode(CharBuffer.wrap(new char[] { c }))
                                        .get() & 0xff,
                                0,
                                126);
                        return;
                    }
                } else {
                    UperEncoder.encodeConstrainedInt(
                            bitbuffer,
                            StandardCharsets.US_ASCII.encode(CharBuffer.wrap(new char[] { c }))
                                    .get() & 0xff,
                            0,
                            126);
                    return;
                }
            default:
                throw new UnsupportedOperationException("String type " + restriction
                        + " is not supported yet");
        }
    }

    private static String decodeRestrictedChar(BitBuffer bitqueue,
            RestrictedString restrictionAnnotation) {
        switch (restrictionAnnotation.value()) {
            case IA5String: {
                if (restrictionAnnotation.alphabet() != DefaultAlphabet.class) { throw new UnsupportedOperationException(
                        "alphabet for IA5String is not supported yet."); }
                byte charByte = (byte) UperEncoder.decodeConstrainedInt(bitqueue, UperEncoder.newRange(0, 127, false));
                byte[] bytes = new byte[] { charByte };
                String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes)).toString();
                if (result.length() != 1) { throw new AssertionError(CHAR_DECORATION
                        + result + ")"); }
                return result;
            }
            case VisibleString:
            case ISO646String: {
                if (restrictionAnnotation.alphabet() != DefaultAlphabet.class) {
                    char[] chars;
                    try {
                        chars = UperEncoder.instantiate(restrictionAnnotation.alphabet()).chars().toCharArray();
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Uninstantinatable alphabet"
                                + restrictionAnnotation.alphabet().getName());
                    }
                    if (BigInteger.valueOf(chars.length - 1).bitLength() < BigInteger.valueOf(126)
                            .bitLength()) {
                        Arrays.sort(chars);
                        int index = (byte) UperEncoder.decodeConstrainedInt(bitqueue,
                                UperEncoder.newRange(0, chars.length - 1, false));
                        String strAlphabet = new String(chars);
                        char c = strAlphabet.charAt(index);
                        String result = new String("" + c);
                        return result;
                    } else {  // Encode normally
                        byte charByte = (byte) UperEncoder.decodeConstrainedInt(bitqueue,
                                UperEncoder.newRange(0, 126, false));
                        byte[] bytes = new byte[] { charByte };
                        String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes))
                                .toString();
                        if (result.length() != 1) { throw new AssertionError(
                                CHAR_DECORATION + result + ")"); }
                        return result;
                    }
                } else {  // Encode normally
                    byte charByte = (byte) UperEncoder.decodeConstrainedInt(bitqueue, UperEncoder.newRange(0, 126, false));
                    byte[] bytes = new byte[] { charByte };
                    String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes))
                            .toString();
                    if (result.length() != 1) { throw new AssertionError(
                            CHAR_DECORATION + result + ")"); }
                    return result;
                }
            }
            default:
                throw new UnsupportedOperationException("String type " + restrictionAnnotation
                        + " is not supported yet");

        }
    }

}