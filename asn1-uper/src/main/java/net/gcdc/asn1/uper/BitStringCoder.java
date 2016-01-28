package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.gcdc.asn1.datatypes.Asn1VarSizeBitstring;
import net.gcdc.asn1.datatypes.Bitstring;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.SizeRange;
import net.gcdc.asn1.uper.UperEncoder.Asn1ContainerFieldSorter;

class BitStringCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Bitstring.class) != null;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        if (!(obj instanceof Asn1VarSizeBitstring)) {
            if (UperEncoder.hasExtensionMarker(annotations)) { throw new UnsupportedOperationException(
                    "Bitstring with extensions is not implemented yet"); }
            FixedSize size = type.getAnnotation(FixedSize.class);
            int position = bitbuffer.position();
            if (size != null) {
                Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
                if (sorter.ordinaryFields.size() != size.value()) { throw new AssertionError(
                        "Declared size (" + size.value() +
                                ") and number of fields (" + sorter.ordinaryFields.size() +
                                ") do not match!"); }
                for (Field f : sorter.ordinaryFields) {
                    try {
                        bitbuffer.put(f.getBoolean(obj));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalArgumentException("can't encode" + obj, e);
                    }
                }
                UperEncoder.logger.debug("BITSTRING {}, encoded as <{}>", obj.getClass().getName(),
                        bitbuffer.toBooleanStringFromPosition(position));
                return;
            } else {
                throw new UnsupportedOperationException(
                        "Bitstrings of variable size are not implemented yet");
            }
        } else if (obj instanceof Asn1VarSizeBitstring) {
            int position = bitbuffer.position();
            if (UperEncoder.hasExtensionMarker(annotations)) { throw new UnsupportedOperationException(
                    "Bitstring with extensions is not implemented yet"); }
            Asn1VarSizeBitstring bitstring = (Asn1VarSizeBitstring) obj;
            FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
            SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
            if (fixedSize != null) {
                for (int i = 0; i < fixedSize.value(); i++) {
                    bitbuffer.put(bitstring.getBit(i));
                }
                UperEncoder.logger.debug("BITSTRING {}: {}", obj.getClass().getName(),
                        bitbuffer.toBooleanStringFromPosition(position));
                return;
            } else if (sizeRange != null) {
                int position1 = bitbuffer.position();
                UperEncoder.encodeConstrainedInt(bitbuffer, bitstring.size(), sizeRange.minValue(),
                        sizeRange.maxValue());
                int position2 = bitbuffer.position();
                for (int i = 0; i < bitstring.size(); i++) {
                    bitbuffer.put(bitstring.getBit(i));
                }
                UperEncoder.logger.debug("BITSTRING {} size {}: {}", obj.getClass().getName(),
                        bitbuffer.toBooleanString(position1, position2 - position1),
                        bitbuffer.toBooleanStringFromPosition(position2));
                return;
            } else {
                throw new IllegalArgumentException("Both SizeRange and FixedSize are null");
            }
        }
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Bitstring.class) != null;
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        if (!Asn1VarSizeBitstring.class.isAssignableFrom(classOfT)) {
            UperEncoder.logger.debug("Bitlist(fixed-size, all-named)");
            FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
            if (fixedSize == null) { throw new UnsupportedOperationException(
                    "bitstrings of non-fixed size that do not extend Asn1VarSizeBitstring are not supported yet"); }
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);
            if (fixedSize.value() != sorter.ordinaryFields.size()) { throw new IllegalArgumentException(
                    "Fixed size annotation " + fixedSize.value()
                            + " does not match the number of fields "
                            + sorter.ordinaryFields.size() + " in " + classOfT.getName()); }
            if (UperEncoder.hasExtensionMarker(annotations)) {
                boolean extensionPresent = bitbuffer.get();
                if (extensionPresent) { throw new UnsupportedOperationException(
                        "extensions in fixed-size bitlist are not supported yet"); }
            }
            T result = UperEncoder.instantiate(classOfT);
            for (Field f : sorter.ordinaryFields) {
                boolean value = bitbuffer.get();
                UperEncoder.logger.debug("Field {} set to {}", f.getName(), value);
                try {
                    f.set(result, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalArgumentException("can't decode " + classOfT, e);
                }
            }
            return result;
        } else {
            UperEncoder.logger.debug("Bitlist(var-size)");
            FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
            SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
            // We use reflection here to access protected method of Asn1VarSizeBitstring.
            // Alternative would be to mandate BitSet constructors for all subclasses of
            // Asn1VarSizeBitstring.
            Method setBitMethod;
            try {
                setBitMethod = Asn1VarSizeBitstring.class.getDeclaredMethod("setBit", int.class,
                        boolean.class);
                setBitMethod.setAccessible(true);
            } catch (SecurityException | NoSuchMethodException e) {
                throw new AssertionError("Can't find/access setBit " + e);
            }
            long size = (fixedSize != null) ? fixedSize.value() :
                    (sizeRange != null) ? UperEncoder.decodeConstrainedInt(bitbuffer,
                            UperEncoder.intRangeFromSizeRange(sizeRange)) :
                            badSize(classOfT);
            T result = UperEncoder.instantiate(classOfT);
            for (int i = 0; i < size; i++) {
                try {
                    setBitMethod.invoke(result, i, bitbuffer.get());
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalArgumentException("Can't invoke setBit", e);
                }
            }
            return result;
        }
    }

    /** This function only throws an exception, to be used in ternary (a?b:c) expression. */
    static <T> long badSize(Class<T> classOfT) {
        throw new IllegalArgumentException("both size range and fixed size are null for "
                + classOfT.getName());
    }
}