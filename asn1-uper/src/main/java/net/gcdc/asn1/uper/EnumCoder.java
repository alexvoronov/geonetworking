package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gcdc.asn1.datatypes.IsExtension;

class EnumCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        return type.isEnum();
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("ENUM");
        int position = bitbuffer.position();
        if (!UperEncoder.hasExtensionMarker(annotations)) {
            List<?> values = Arrays.asList(type.getEnumConstants());
            int index = values.indexOf(obj);
            UperEncoder.logger.debug("enum without ext, index {}, encoding index...", index);
            UperEncoder.encodeConstrainedInt(bitbuffer, index, 0, values.size() - 1);
            return;
        } else {
            List<Object> valuesWithinExtensionRoot = new ArrayList<>();
            List<Object> valuesOutsideExtensionRoot = new ArrayList<>();
            for (Object c : type.getEnumConstants()) {
                if (c.getClass().getAnnotation(IsExtension.class) == null) {  // double-check
                                                                             // getClass
                    valuesWithinExtensionRoot.add(c);
                } else {
                    valuesOutsideExtensionRoot.add(c);
                }
            }
            if (valuesWithinExtensionRoot.contains(obj)) {
                bitbuffer.put(false);
                int index = valuesWithinExtensionRoot.indexOf(obj);
                UperEncoder.encodeConstrainedInt(bitbuffer, index, 0, valuesWithinExtensionRoot.size() - 1);
                UperEncoder.logger.debug("ENUM w/ext (index {}), encoded as <{}>", index,
                        bitbuffer.toBooleanStringFromPosition(position));
                return;
            } else {
                throw new UnsupportedOperationException("Enum extensions are not supported yet");
            }
        }
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        return classOfT.isEnum();
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("ENUM");
        if (UperEncoder.hasExtensionMarker(annotations)) {
            boolean extensionPresent = bitbuffer.get();
            UperEncoder.logger.debug("with extension marker, {}", extensionPresent ? "present" : "absent");
            if (extensionPresent) {
                throw new UnsupportedOperationException(
                        "choice extension is not implemented yet");
            } else {
                // We already consumed the bit, keep processing as if there were no extension.
            }
        }
        T[] enumValues = classOfT.getEnumConstants();
        int index = (int) UperEncoder.decodeConstrainedInt(bitbuffer,
                UperEncoder.newRange(0, enumValues.length - 1, false));
        if (index > enumValues.length - 1) { throw new IllegalArgumentException(
                "decoded enum index " + index + " is larger then number of elements (0.."
                        + enumValues.length + ") in " + classOfT.getName()); }
        T value = enumValues[index];
        return value;        }

}