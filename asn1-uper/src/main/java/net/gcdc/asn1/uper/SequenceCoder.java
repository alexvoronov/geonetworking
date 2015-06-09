package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;

import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.uper.UperEncoder.Asn1ContainerFieldSorter;

class SequenceCoder implements Decoder, Encoder {

    @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Sequence.class) != null;
    }

    @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
        Class<?> type = obj.getClass();
        AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("SEQUENCE {}", type.getName());
        Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
        try {
            if (UperEncoder.hasExtensionMarker(annotations)) {
                boolean extensionsPresent = !sorter.extensionFields.isEmpty()
                        && UperEncoder.hasNonNullExtensions(obj, sorter);
                UperEncoder.logger.debug("with extension marker, {} extensions, extensionBit: <{}>",
                        extensionsPresent ? "with" : "without", extensionsPresent);
                bitbuffer.put(extensionsPresent);
            }
            // Bitmask for optional fields.
            for (Field f : sorter.optionalOrdinaryFields) {
                boolean fieldPresent = f.get(obj) != null;
                UperEncoder.logger.debug("with optional field {} {}, presence encoded as bit <{}>",
                        f.getName(), fieldPresent ? "present" : "absent", fieldPresent);
                bitbuffer.put(fieldPresent);  // null means the field is absent.
            }
            // All ordinary fields (fields within extension root).
            for (Field f : sorter.ordinaryFields) {
                if ((UperEncoder.isMandatory(f) || f.get(obj) != null) && !UperEncoder.isTestInstrumentation(f)) {
                    UperEncoder.logger.debug("Field : {}", f.getName());
                    UperEncoder.encode2(bitbuffer, f.get(obj), f.getAnnotations());
                }
            }
            // Extension fields.
            if (UperEncoder.hasExtensionMarker(annotations) && !sorter.extensionFields.isEmpty()
                    && UperEncoder.hasNonNullExtensions(obj, sorter)) {
                // Total extensions count.
                int numExtensions = sorter.extensionFields.size();
                UperEncoder.logger.debug(
                        "continuing sequence : {} extension(s) are present, encoding length determinant for them...",
                        numExtensions);
                UperEncoder.encodeLengthOfBitmask(bitbuffer, numExtensions);
                // Bitmask for present extensions.
                for (Field f : sorter.extensionFields) {
                    boolean fieldIsPresent = f.get(obj) != null;
                    UperEncoder.logger.debug("Extension {} is {}, presence encoded as <{}>", f.getName(),
                            fieldIsPresent ? "present" : "absent", fieldIsPresent ? "1" : "0");
                    bitbuffer.put(fieldIsPresent);
                }
                // Values of extensions themselves.
                for (Field f : sorter.extensionFields) {
                    if (f.get(obj) != null) {
                        UperEncoder.logger.debug("Encoding extension field {}", f.getName());
                        UperEncoder.encodeAsOpenType(bitbuffer, f.get(obj), f.getAnnotations());
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalArgumentException("can't encode " + obj, e);
        }
        sorter.revertAccess();
    }

    @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        return annotations.getAnnotation(Sequence.class) != null;
    }

    @Override public <T> T decode(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                extraAnnotations);
        UperEncoder.logger.debug("SEQUENCE");
        T result = UperEncoder.instantiate(classOfT);
        Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);
        boolean extensionPresent = false;
        if (UperEncoder.hasExtensionMarker(annotations)) {
            extensionPresent = bitbuffer.get();
            UperEncoder.logger.debug("with extension marker, extension {}", extensionPresent ? "present!"
                    : "absent");
        }
        // Bitmask for optional fields.
        Deque<Boolean> optionalFieldsMask = new ArrayDeque<>(
                sorter.optionalOrdinaryFields.size());
        for (Field f : sorter.optionalOrdinaryFields) {
            optionalFieldsMask.add(bitbuffer.get());
            UperEncoder.logger.debug("with optional field {} {}", f.getName(),
                    optionalFieldsMask.getLast() ? "present" : "absent");
        }
        // All ordinary fields (fields within extension root).
        for (Field f : sorter.ordinaryFields) {
            if (!UperEncoder.isTestInstrumentation(f)
                    && (UperEncoder.isMandatory(f) || (UperEncoder.isOptional(f) && optionalFieldsMask.pop()))) {
                UperEncoder.logger.debug("Field : {}", f.getName());
                try {
                    f.set(result, UperEncoder.decode2(bitbuffer, f.getType(), f.getAnnotations()));
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("can't access 'set method' for field " + f + " of class " + classOfT + " " + e, e);
                }
            }
        }
        // Extension fields.
        if (UperEncoder.hasExtensionMarker(annotations) && extensionPresent) {
            // Number of extensions.
            int numExtensions = (int) UperEncoder.decodeLengthOfBitmask(bitbuffer);
            UperEncoder.logger.debug("sequence has {} extension(s)", numExtensions);
            // Bitmask for extensions.
            boolean[] bitmaskValueIsPresent = new boolean[numExtensions];
            for (int i = 0; i < numExtensions; i++) {
                bitmaskValueIsPresent[i] = bitbuffer.get();
                UperEncoder.logger.debug("extension {} is {}", i, bitmaskValueIsPresent[i] ? "present"
                        : "absent");
            }
            // Values.
            UperEncoder.logger.debug("decoding extensions values...");
            for (int i = 0; i < numExtensions; i++) {
                UperEncoder.logger.debug("sequence extension {} {}", i,
                        bitmaskValueIsPresent[i] ? "present" : "absent");
                if (bitmaskValueIsPresent[i]) {
                    UperEncoder.logger.debug("decoding extension {}...", i);
                    Field field = sorter.extensionFields.size() > i ? sorter.extensionFields
                            .get(i) : null;
                    Class<?> classOfElement = field != null ? field.getType() : null;
                    try {
                        Object decodedValue = UperEncoder.decodeAsOpenType(bitbuffer, classOfElement,
                                field.getAnnotations());
                        if (field != null) {
                            field.set(result, decodedValue);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalArgumentException("can't decode " + classOfT, e);
                    }
                }
            }
        }
        sorter.revertAccess();
        return result;        }
}