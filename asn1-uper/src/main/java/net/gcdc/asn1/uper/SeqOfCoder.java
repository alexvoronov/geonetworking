package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.SizeRange;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

class SeqOfCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        return obj instanceof List<?>;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("SEQUENCE OF");
        List<?> list = (List<?>) obj;
        final FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
        SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
        if (fixedSize != null)
            sizeRange = new SizeRange() {
                @Override public Class<? extends Annotation> annotationType() { return SizeRange.class; }
                @Override public int minValue() { return fixedSize.value(); }
                @Override public int maxValue() { return fixedSize.value(); }
                @Override public boolean hasExtensionMarker() { return false; }
            };
        if (sizeRange == null) {
            int position1 = bitbuffer.position();
            try {
                UperEncoder.encodeLengthDeterminant(bitbuffer, list.size());
            } catch (Asn1EncodingException e) {
                throw new Asn1EncodingException(" number of elements ", e);
            }
            UperEncoder.logger.debug("unbound size {}, encoded as {}", list.size(),
                    bitbuffer.toBooleanStringFromPosition(position1));
            UperEncoder.logger.debug("  all elems of Seq Of: {}", list);
            for (Object elem : list) {
                try {
                    UperEncoder.encode2(bitbuffer, elem, new Annotation[] {});
                } catch (Asn1EncodingException e) {
                    throw new Asn1EncodingException(" element " + elem.toString(), e);
                }
            }
            return;
        }
        boolean outsideOfRange = list.size() < sizeRange.minValue()
                || sizeRange.maxValue() < list.size();
        if (outsideOfRange && !sizeRange.hasExtensionMarker()) { throw new IllegalArgumentException(
                "Out-of-range size for " + obj.getClass() + ", expected " +
                        sizeRange.minValue() + ".." + sizeRange.maxValue() + ", got "
                        + list.size()); }
        if (sizeRange.hasExtensionMarker()) {
            bitbuffer.put(outsideOfRange);
            UperEncoder.logger.debug("With Extension Marker, {} of range ({} <= {} <= {})",
                    (outsideOfRange ? "outside" : "inside"), sizeRange.minValue(), list.size(),
                    sizeRange.maxValue());
            if (outsideOfRange) { throw new UnsupportedOperationException(
                    "Sequence-of size range extensions are not implemented yet, range " +
                            sizeRange.minValue() + ".." + sizeRange.maxValue()
                            + ", requested size " + list.size()); }
        }
        UperEncoder.logger.debug("seq-of of constrained size {}, encoding size...", list.size());
        UperEncoder.encodeConstrainedInt(bitbuffer, list.size(), sizeRange.minValue(), sizeRange.maxValue());
        UperEncoder.logger.debug("  all elems of Seq Of: {}", list);
        for (Object elem : list) {
            UperEncoder.encode2(bitbuffer, elem, new Annotation[] {});
        }
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return List.class.isAssignableFrom(classOfT);
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("SEQUENCE OF for {}", classOfT);
        FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
        SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
        long size =
          (fixedSize != null) ? fixedSize.value() :
              (sizeRange != null) ? UperEncoder.decodeConstrainedInt(bitbuffer, UperEncoder.intRangeFromSizeRange(sizeRange)) :
                  UperEncoder.decodeLengthDeterminant(bitbuffer);
        Collection<Object> coll = new ArrayList<Object>((int) size);
        for (int i = 0; i < size; i++) {
            Class<?>[] typeArgs = TypeResolver.resolveRawArguments(List.class, classOfT);
            Class<?> classOfElements = typeArgs[0];
            if (classOfElements == Unknown.class) { throw new IllegalArgumentException(
                    "Can't resolve type of elements for " + classOfT.getName()); }
            coll.add(UperEncoder.decode2(bitbuffer, classOfElements, new Annotation[] {}));
        }
        T result = UperEncoder.instantiate(classOfT, coll);
        return result;        }

}