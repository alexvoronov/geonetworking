package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import net.gcdc.asn1.datatypes.Choice;
import net.gcdc.asn1.uper.UperEncoder.Asn1ContainerFieldSorter;

class ChoiceCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Choice.class) != null;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) throws Asn1EncodingException {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("CHOICE");
        int nonNullIndex = 0;
        Field nonNullField = null;
        Object nonNullFieldValue = null;
        int currentIndex = 0;
        Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
        try {
            for (Field f : sorter.ordinaryFields) {
                if (f.get(obj) != null) {
                    nonNullIndex = currentIndex;
                    nonNullFieldValue = f.get(obj);
                    nonNullField = f;
                    break;
                }
                currentIndex++;
            }
            if (nonNullFieldValue != null) {
                if (UperEncoder.hasExtensionMarker(annotations)) {
                    boolean extensionBit = false;
                    UperEncoder.logger.debug("with extension marker, set to {}", extensionBit);
                    bitbuffer.put(extensionBit);
                }
                if (sorter.ordinaryFields.size() > 1) {  // Encode index only if more than one.
                    UperEncoder.logger.debug("with chosen element indexed {}", nonNullIndex);
                    UperEncoder.encodeConstrainedInt(bitbuffer, nonNullIndex, 0,
                            sorter.ordinaryFields.size() - 1);
                }
                UperEncoder.encode2(bitbuffer, nonNullFieldValue, nonNullField.getAnnotations());
                return;
            } else if (UperEncoder.hasExtensionMarker(annotations)) {
                currentIndex = 0;
                for (Field f : sorter.extensionFields) {
                    if (f.get(obj) != null) {
                        nonNullIndex = currentIndex;
                        nonNullFieldValue = f.get(obj);
                        nonNullField = f;
                        break;
                    }
                    currentIndex++;
                }
                if (nonNullField == null) { throw new IllegalArgumentException(
                        "All fields of Choice are null"); }
                boolean extensionBit = true;
                UperEncoder.logger.debug("with extension marker, set to <{}>", extensionBit);
                bitbuffer.put(extensionBit);
                throw new UnsupportedOperationException(
                        "Choice extension is not implemented yet");
            } else {
                throw new IllegalArgumentException(
                        "Not Extension and All ordinary fields of Choice are null");
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException("can't encode " + obj, e);
        } catch (Asn1EncodingException e) {
            throw new Asn1EncodingException("." + type.getName(), e);
        }
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Choice.class) != null;
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("CHOICE");
        T result = UperEncoder.instantiate(classOfT);
        Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);

        // Reset all fields, since default constructor initializes one.
        for (Field f : sorter.allFields) {
            try {
                f.set(result, null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalArgumentException("can't decode " + classOfT, e);
            }
        }
        if (UperEncoder.hasExtensionMarker(annotations)) {
            UperEncoder.logger.debug("with extension marker");
            boolean extensionPresent = bitbuffer.get();
            if (extensionPresent) {
                throw new UnsupportedOperationException(
                        "choice extension is not implemented yet");
            } else {
                // We already consumed the bit, keep processing as if there were no extension.
            }
        }
        int index = (int) UperEncoder.decodeConstrainedInt(bitbuffer,
                UperEncoder.newRange(0, sorter.ordinaryFields.size() - 1, false));
        Field f = sorter.ordinaryFields.get(index);
        Object fieldValue = UperEncoder.decode2(bitbuffer, f.getType(), f.getAnnotations());
        try {
            f.set(result, fieldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException("can't decode " + classOfT, e);
        }
        return result;
    }

}