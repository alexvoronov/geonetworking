package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gcdc.asn1.datatypes.Asn1BigInteger;
import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.Asn1VarSizeBitstring;
import net.gcdc.asn1.datatypes.Bitstring;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.Choice;
import net.gcdc.asn1.datatypes.DefaultAlphabet;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.HasExtensionMarker;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.IsExtension;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A "quick-and-dirty" implementation of ASN.1 encoder for UPER (Unaligned Packed Encoding Rules).
 *
 * @see ITU-T Recommendation <a
 *      href="http://www.itu.int/ITU-T/recommendations/rec.aspx?rec=x.691">X.691</a>
 *
 *      TODO: refactoring, refactoring, refactoring. Clean up the mess, unify converters, replace
 *      all
 *      Collection<Boolean> with byte-array-based BitBuffer. Also cover the rest of unsupported
 *      cases,
 *      and write unit tests for them. */
public class UperEncoder {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    private final static int NUM_16K = 16384;
    @SuppressWarnings("unused")
    private final static int NUM_32K = 32768;
    @SuppressWarnings("unused")
    private final static int NUM_48K = 49152;
    @SuppressWarnings("unused")
    private final static int NUM_64K = 65536;

    public static <T> byte[] encode(T obj)
            throws IllegalArgumentException, UnsupportedOperationException {
        try {
            BitBuffer bitbuffer = ByteBitBuffer.createInfinite();
            encode2(bitbuffer, obj, new Annotation[] {});
            bitbuffer.flip();
            byte[] result = Arrays.copyOf(bitbuffer.array(), (bitbuffer.limit() + 7) / 8);
            return result;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Can't encode " + obj.getClass().getName() + ": "
                    + e, e);
        }
    }

    public static <T> T decode(byte[] bytes, Class<T> classOfT) throws IllegalArgumentException,
            UnsupportedOperationException {
        BitBuffer bitQueue = bitBufferFromBinaryString(binaryStringFromBytes(bytes));
        T result = decode2(bitQueue, classOfT, new Annotation[] {});
        if (bitQueue.remaining() > 7) { throw new IllegalArgumentException("Can't fully decode "
                + classOfT.getName() + ", got (" + result.getClass().getName() + "): " + result
                + "; remaining " + bitQueue.remaining() + "  bits: " + bitQueue); }
        return result;
    }


    private static <T> void encode2(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
        for (Encoder e : encoders) {
            if (e.canEncode(obj, extraAnnotations)) {
                e.encode(bitbuffer, obj, extraAnnotations);
                return;
            }
        }
        throw new IllegalArgumentException("Can't find encoder for " + obj.getClass().getName()
                + " with extra annotations " + Arrays.asList(extraAnnotations));
    }

    private static <T> T decode2(BitBuffer bitbuffer, Class<T> classOfT, Annotation[] extraAnnotations) {
        logger.debug("Decoding classOfT : {}", classOfT);
        for (Decoder e : decoders) {
            if (e.canDecode(classOfT, extraAnnotations)) {
                return e.decode(bitbuffer, classOfT, extraAnnotations);
            }
        }
        throw new IllegalArgumentException("Can't find decoder for " + classOfT.getName()
                + " with extra annotations " + Arrays.asList(extraAnnotations));
    }

    private static IntRange newRange(
            final long minValue,
            final long maxValue,
            final boolean hasExtensionMarker) {
        return new IntRange() {
            @Override public Class<? extends Annotation> annotationType() { return IntRange.class; }
            @Override public long minValue() { return minValue; }
            @Override public long maxValue() { return maxValue; }
            @Override public boolean hasExtensionMarker() { return hasExtensionMarker; }
        };
    }

    private static IntRange intRangeFromSizeRange(SizeRange sizeRange) {
        return newRange(sizeRange.minValue(), sizeRange.maxValue(), sizeRange.hasExtensionMarker());
    }

    private static class AnnotationStore {

        private Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

        public AnnotationStore(Annotation[] classAnnot, Annotation[] fieldAnnot) {
            for (Annotation a : classAnnot) {
                annotations.put(a.annotationType(), a);
            }
            for (Annotation a : fieldAnnot) {
                annotations.put(a.annotationType(), a);
            }
        }

        public <T extends Annotation> T getAnnotation(Class<T> classOfT) {
            @SuppressWarnings("unchecked")
            // Annotations were added with value T for key classOfT.
            T result = (T) annotations.get(classOfT);
            return result;
        }

        public Collection<Annotation> getAnnotations() {
            return annotations.values();
        }
    }


    private static List<Encoder> encoders = new ArrayList<>();
    private static List<Decoder> decoders = new ArrayList<>();

    static {
        encoders.add(new IntCoder());
        encoders.add(new BigIntCoder());
        encoders.add(new ByteCoder());
        encoders.add(new BooleanCoder());
        encoders.add(new SequenceCoder());
        encoders.add(new ChoiceCoder());
        encoders.add(new EnumCoder());
        encoders.add(new BitStringCoder());
        encoders.add(new SeqOfCoder());
        encoders.add(new StringCoder());

        decoders.add(new IntCoder());
        decoders.add(new BigIntCoder());
        decoders.add(new ByteCoder());
        decoders.add(new BooleanCoder());
        decoders.add(new SequenceCoder());
        decoders.add(new ChoiceCoder());
        decoders.add(new EnumCoder());
        decoders.add(new BitStringCoder());
        decoders.add(new SeqOfCoder());
        decoders.add(new StringCoder());

    }


    private static class IntCoder implements Encoder, Decoder {

        private static final Map<Class<?>, IntRange> DEFAULT_RANGE;

        static {
            DEFAULT_RANGE = new HashMap<>();
            DEFAULT_RANGE.put(short.class, newRange(Short.MIN_VALUE, Short.MAX_VALUE, false));
            DEFAULT_RANGE.put(Short.class, newRange(Short.MIN_VALUE, Short.MAX_VALUE, false));
            DEFAULT_RANGE.put(int.class, newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
            DEFAULT_RANGE.put(Integer.class, newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
            DEFAULT_RANGE.put(long.class, newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
            DEFAULT_RANGE.put(Long.class, newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
            // Byte is not part of this, since we treat byte as unsigned byte, while the rest we treat
            // as it is.

            // Asn1Integer have max range of Long. Bigger ranges require Asn1BigInteger.
            DEFAULT_RANGE.put(Asn1Integer.class, newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
        }

        @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
            return Asn1Integer.class.isAssignableFrom(classOfT) |
                    Long.class.isAssignableFrom(classOfT) |
                    long.class.isAssignableFrom(classOfT) |
                    Integer.class.isAssignableFrom(classOfT) |
                    int.class.isAssignableFrom(classOfT) |
                    Short.class.isAssignableFrom(classOfT) |
                    short.class.isAssignableFrom(classOfT);
        }

        @Override public <T> T decode(BitBuffer bitbuffer,
                Class<T> classOfT,
                Annotation[] extraAnnotations) {
            AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                    extraAnnotations);
            logger.debug("INTEGER");
            IntRange intRange = annotations.getAnnotation(IntRange.class);
            if (intRange == null) {
                intRange = DEFAULT_RANGE.get(classOfT);
            }
            logger.debug("Integer, range {}..{}", intRange.minValue(), intRange.maxValue());
            long value = decodeConstrainedInt(bitbuffer, intRange);
            logger.debug("decoded as {}", value);
            Class<?>[] numericTypes = new Class<?>[] { long.class, int.class, short.class };
            Constructor<T> constructor = null;
            for (Class<?> t : numericTypes) {
                try {
                    constructor = classOfT.getConstructor(t);
                } catch (NoSuchMethodException e) {
                    // ignore and try next
                } catch (SecurityException e) {
                    throw new IllegalArgumentException("can't access constructor of "
                            + classOfT.getName() + ": " + e);
                }
            }
            if (constructor == null) { throw new IllegalArgumentException(
                    "can't find any numeric constructor for " + classOfT.getName()
                            + ", all constructors: " + Arrays.asList(classOfT.getConstructors())); }
            try {
                Class<?> typeOfConstructorArgument = constructor.getParameterTypes()[0];
                if (typeOfConstructorArgument.isAssignableFrom(long.class)) {
                    return constructor.newInstance(value);
                } else if (typeOfConstructorArgument.isAssignableFrom(int.class)) {
                    return constructor.newInstance((int) value);
                } else if (typeOfConstructorArgument.isAssignableFrom(short.class)) {
                    return constructor.newInstance((short) value);
                } else {
                    throw new IllegalArgumentException("unrecognized constructor argument "
                            + typeOfConstructorArgument.getName());
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | InstantiationException e1) {
                throw new IllegalArgumentException("failed to invoke constructor of "
                        + classOfT.getName() + ": " + e1);
            }
        }

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof Asn1Integer ||
                    obj instanceof Long ||
                    obj instanceof Integer ||
                    obj instanceof Short;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            IntRange range = annotations.getAnnotation(IntRange.class);
            if (range == null) {
                range = DEFAULT_RANGE.get(obj);
            }
            int position = bitbuffer.position();
            encodeConstrainedInt(bitbuffer, ((Asn1Integer) obj).value(), range.minValue(),
                    range.maxValue(), range.hasExtensionMarker());
            logger.debug("INT({}): {}", obj, bitbuffer.toBooleanStringFromPosition(position));
            return;
        }

    }

    private static class BigIntCoder implements Encoder, Decoder {

        @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
            return Asn1BigInteger.class.isAssignableFrom(classOfT);
        }

        @Override public <T> T decode(BitBuffer bitbuffer,
                Class<T> classOfT,
                Annotation[] extraAnnotations) {
            AnnotationStore annotations = new AnnotationStore(classOfT.getAnnotations(),
                    extraAnnotations);
            logger.debug("BIG INT");
            IntRange intRange = annotations.getAnnotation(IntRange.class);
            if (intRange != null) { throw new UnsupportedOperationException(
                    "Big int with range is not supported yet"); }
            int lengthInOctets = (int) decodeLengthDeterminant(bitbuffer);
            List<Boolean> valueBits = new ArrayList<Boolean>(lengthInOctets * 8);
            for (int i = 0; i < lengthInOctets * 8; i++) {
                valueBits.add(bitbuffer.get());
            }
            BigInteger resultValue = new BigInteger(binaryStringFromCollection(valueBits), 2);
            logger.debug("big int Decoded as {}", resultValue);
            return instantiate(classOfT, resultValue);
        }

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof Asn1BigInteger;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            IntRange range = annotations.getAnnotation(IntRange.class);
            if (range != null) { throw new UnsupportedOperationException(
                    "Asn1 BigInteger with range is not supported yet"); }
            byte[] array = ((Asn1BigInteger) obj).value().toByteArray();
            int lengthInOctets = array.length;
            int position1 = bitbuffer.position();
            encodeLengthDeterminant(bitbuffer, lengthInOctets);
            int position2 = bitbuffer.position();
            for (byte b : array) {
                bitbuffer.putByte(b);
            }
            logger.debug("Big Int({}): len {}, val {}", obj,
                    bitbuffer.toBooleanString(position1, position2 - position1),
                    bitbuffer.toBooleanStringFromPosition(position2));
            return;
        }

    }

    private static class ByteCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof Byte;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            encodeConstrainedInt(bitbuffer, ((Byte) obj).byteValue() & 0xff, 0, 255);
            logger.debug("BYTE {}", ((Byte) obj).byteValue());
        }

        @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
            return Byte.class.isAssignableFrom(classOfT) || byte.class.isAssignableFrom(classOfT);
        }

        @Override public <T> T decode(BitBuffer bitbuffer,
                Class<T> classOfT,
                Annotation[] extraAnnotations) {
            logger.debug("BYTE");
            return (T) new Byte((byte) decodeConstrainedInt(bitbuffer, newRange(0, 255, false)));
        }

    }

    private static class BooleanCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof Boolean;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            logger.debug("BOOLEAN {}", obj);
            bitbuffer.put((Boolean) obj);
        }

        @Override public <T> boolean canDecode(Class<T> classOfT, Annotation[] extraAnnotations) {
            return Boolean.class.isAssignableFrom(classOfT)
                    || boolean.class.isAssignableFrom(classOfT);
        }

        @Override public <T> T decode(BitBuffer bitbuffer,
                Class<T> classOfT,
                Annotation[] extraAnnotations) {
            logger.debug("BOOL");
            return (T) new Boolean(bitbuffer.get());
        }
    }

    private static class SequenceCoder implements Decoder, Encoder {

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
            logger.debug("SEQUENCE {}", type.getName());
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
            try {
                if (hasExtensionMarker(annotations)) {
                    boolean extensionsPresent = !sorter.extensionFields.isEmpty()
                            && hasNonNullExtensions(obj, sorter);
                    logger.debug("with extension marker, {} extensions, extensionBit: <{}>",
                            extensionsPresent ? "with" : "without", extensionsPresent);
                    bitbuffer.put(extensionsPresent);
                }
                // Bitmask for optional fields.
                for (Field f : sorter.optionalOrdinaryFields) {
                    boolean fieldPresent = f.get(obj) != null;
                    logger.debug("with optional field {} {}, presence encoded as bit <{}>",
                            f.getName(), fieldPresent ? "present" : "absent", fieldPresent);
                    bitbuffer.put(fieldPresent);  // null means the field is absent.
                }
                // All ordinary fields (fields within extension root).
                for (Field f : sorter.ordinaryFields) {
                    if ((isMandatory(f) || f.get(obj) != null) && !isTestInstrumentation(f)) {
                        logger.debug("Field : {}", f.getName());
                        encode2(bitbuffer, f.get(obj), f.getAnnotations());
                    }
                }
                // Extension fields.
                if (hasExtensionMarker(annotations) && !sorter.extensionFields.isEmpty()
                        && hasNonNullExtensions(obj, sorter)) {
                    // Total extensions count.
                    int numExtensions = sorter.extensionFields.size();
                    logger.debug(
                            "continuing sequence : {} extension(s) are present, encoding length determinant for them...",
                            numExtensions);
                    encodeLengthDeterminant(bitbuffer, numExtensions, true);
                    // Bitmask for present extensions.
                    for (Field f : sorter.extensionFields) {
                        boolean fieldIsPresent = f.get(obj) != null;
                        logger.debug("Extension {} is {}, presence encoded as <{}>", f.getName(),
                                fieldIsPresent ? "present" : "absent", fieldIsPresent ? "1" : "0");
                        bitbuffer.put(fieldIsPresent);
                    }
                    // Values of extensions themselves.
                    for (Field f : sorter.extensionFields) {
                        if (f.get(obj) != null) {
                            logger.debug("Encoding extension field {}", f.getName());
                            encodeAsOpenType(bitbuffer, f.get(obj), f.getAnnotations());
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
            logger.debug("SEQUENCE");
            T result = instantiate(classOfT);
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);
            boolean extensionPresent = false;
            if (hasExtensionMarker(annotations)) {
                extensionPresent = bitbuffer.get();
                logger.debug("with extension marker, extension {}", extensionPresent ? "present!"
                        : "absent");
            }
            // Bitmask for optional fields.
            Deque<Boolean> optionalFieldsMask = new ArrayDeque<>(
                    sorter.optionalOrdinaryFields.size());
            for (Field f : sorter.optionalOrdinaryFields) {
                optionalFieldsMask.add(bitbuffer.get());
                logger.debug("with optional field {} {}", f.getName(),
                        optionalFieldsMask.getLast() ? "present" : "absent");
            }
            // All ordinary fields (fields within extension root).
            for (Field f : sorter.ordinaryFields) {
                if (!isTestInstrumentation(f)
                        && (isMandatory(f) || (isOptional(f) && optionalFieldsMask.pop()))) {
                    logger.debug("Field : {}", f.getName());
                    try {
                        f.set(result, decode2(bitbuffer, f.getType(), f.getAnnotations()));
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("can't access 'set method' for field " + f + " of class " + classOfT + " " + e, e);
                    }
                }
            }
            // Extension fields.
            if (hasExtensionMarker(annotations) && extensionPresent) {
                // Number of extensions.
                int numExtensions = (int) decodeLengthDeterminant(bitbuffer, true);
                logger.debug("sequence has {} extension(s)", numExtensions);
                // Bitmask for extensions.
                boolean[] bitmaskValueIsPresent = new boolean[numExtensions];
                for (int i = 0; i < numExtensions; i++) {
                    bitmaskValueIsPresent[i] = bitbuffer.get();
                    logger.debug("extension {} is {}", i, bitmaskValueIsPresent[i] ? "present"
                            : "absent");
                }
                // Values.
                logger.debug("decoding extensions values...");
                for (int i = 0; i < numExtensions; i++) {
                    logger.debug("sequence extension {} {}", i,
                            bitmaskValueIsPresent[i] ? "present" : "absent");
                    if (bitmaskValueIsPresent[i]) {
                        logger.debug("decoding extension {}...", i);
                        Field field = sorter.extensionFields.size() > i ? sorter.extensionFields
                                .get(i) : null;
                        Class<?> classOfElement = field != null ? field.getType() : null;
                        try {
                            Object decodedValue = decodeAsOpenType(bitbuffer, classOfElement,
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

    private static class ChoiceCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            return annotations.getAnnotation(Choice.class) != null;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            logger.debug("CHOICE");
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
                    if (hasExtensionMarker(annotations)) {
                        boolean extensionBit = false;
                        logger.debug("with extension marker, set to {}", extensionBit);
                        bitbuffer.put(extensionBit);
                    }
                    if (sorter.ordinaryFields.size() > 1) {  // Encode index only if more than one.
                        logger.debug("with chosen element indexed {}", nonNullIndex);
                        encodeConstrainedInt(bitbuffer, nonNullIndex, 0,
                                sorter.ordinaryFields.size() - 1);
                    }
                    encode2(bitbuffer, nonNullFieldValue, nonNullField.getAnnotations());
                    return;
                } else if (hasExtensionMarker(annotations)) {
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
                    logger.debug("with extension marker, set to <{}>", extensionBit);
                    bitbuffer.put(extensionBit);
                    throw new UnsupportedOperationException(
                            "Choice extension is not implemented yet");
                } else {
                    throw new IllegalArgumentException(
                            "Not Extension and All ordinary fields of Choice are null");
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalArgumentException("can't encode " + obj, e);
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
            logger.debug("CHOICE");
            T result = instantiate(classOfT);
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);

            // Reset all fields, since default constructor initializes one.
            for (Field f : sorter.allFields) {
                try {
                    f.set(result, null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalArgumentException("can't decode " + classOfT, e);
                }
            }
            if (hasExtensionMarker(annotations)) {
                logger.debug("with extension marker");
                boolean extensionPresent = bitbuffer.get();
                if (extensionPresent) {
                    throw new UnsupportedOperationException(
                            "choice extension is not implemented yet");
                } else {
                    // We already consumed the bit, keep processing as if there were no extension.
                }
            }
            int index = (int) decodeConstrainedInt(bitbuffer,
                    newRange(0, sorter.ordinaryFields.size() - 1, false));
            Field f = sorter.ordinaryFields.get(index);
            Object fieldValue = decode2(bitbuffer, f.getType(), f.getAnnotations());
            try {
                f.set(result, fieldValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalArgumentException("can't decode " + classOfT, e);
            }
            return result;
        }

    }

    private static class EnumCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            return type.isEnum();
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            logger.debug("ENUM");
            int position = bitbuffer.position();
            if (!hasExtensionMarker(annotations)) {
                List<?> values = Arrays.asList(type.getEnumConstants());
                int index = values.indexOf(obj);
                logger.debug("enum without ext, index {}, encoding index...", index);
                encodeConstrainedInt(bitbuffer, index, 0, values.size() - 1);
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
                    encodeConstrainedInt(bitbuffer, index, 0, valuesWithinExtensionRoot.size() - 1);
                    logger.debug("ENUM w/ext (index {}), encoded as <{}>", index,
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
            logger.debug("ENUM");
            if (hasExtensionMarker(annotations)) {
                boolean extensionPresent = bitbuffer.get();
                logger.debug("with extension marker, {}", extensionPresent ? "present" : "absent");
                if (extensionPresent) {
                    throw new UnsupportedOperationException(
                            "choice extension is not implemented yet");
                } else {
                    // We already consumed the bit, keep processing as if there were no extension.
                }
            }
            T[] enumValues = classOfT.getEnumConstants();
            int index = (int) decodeConstrainedInt(bitbuffer,
                    newRange(0, enumValues.length - 1, false));
            if (index > enumValues.length - 1) { throw new IllegalArgumentException(
                    "decoded enum index " + index + " is larger then number of elements (0.."
                            + enumValues.length + ") in " + classOfT.getName()); }
            T value = enumValues[index];
            return value;        }

    }

    private static class BitStringCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            return annotations.getAnnotation(Bitstring.class) != null;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            if (!(obj instanceof Asn1VarSizeBitstring)) {
                if (hasExtensionMarker(annotations)) { throw new UnsupportedOperationException(
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
                    logger.debug("BITSTRING {}, encoded as <{}>", obj.getClass().getName(),
                            bitbuffer.toBooleanStringFromPosition(position));
                    return;
                } else {
                    throw new UnsupportedOperationException(
                            "Bitstrings of variable size are not implemented yet");
                }
            } else if (obj instanceof Asn1VarSizeBitstring) {
                int position = bitbuffer.position();
                if (hasExtensionMarker(annotations)) { throw new UnsupportedOperationException(
                        "Bitstring with extensions is not implemented yet"); }
                Asn1VarSizeBitstring bitstring = (Asn1VarSizeBitstring) obj;
                FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
                SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
                if (fixedSize != null) {
                    for (int i = 0; i < fixedSize.value(); i++) {
                        bitbuffer.put(bitstring.getBit(i));
                    }
                    logger.debug("BITSTRING {}: {}", obj.getClass().getName(),
                            bitbuffer.toBooleanStringFromPosition(position));
                    return;
                } else if (sizeRange != null) {
                    int position1 = bitbuffer.position();
                    encodeConstrainedInt(bitbuffer, bitstring.size(), sizeRange.minValue(),
                            sizeRange.maxValue());
                    int position2 = bitbuffer.position();
                    for (int i = 0; i < bitstring.size(); i++) {
                        bitbuffer.put(bitstring.getBit(i));
                    }
                    logger.debug("BITSTRING {} size {}: {}", obj.getClass().getName(),
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
                logger.debug("Bitlist(fixed-size, all-named)");
                FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
                if (fixedSize == null) { throw new UnsupportedOperationException(
                        "bitstrings of non-fixed size that do not extend Asn1VarSizeBitstring are not supported yet"); }
                Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);
                if (fixedSize.value() != sorter.ordinaryFields.size()) { throw new IllegalArgumentException(
                        "Fixed size annotation " + fixedSize.value()
                                + " does not match the number of fields "
                                + sorter.ordinaryFields.size() + " in " + classOfT.getName()); }
                if (hasExtensionMarker(annotations)) {
                    boolean extensionPresent = bitbuffer.get();
                    if (extensionPresent) { throw new UnsupportedOperationException(
                            "extensions in fixed-size bitlist are not supported yet"); }
                }
                T result = instantiate(classOfT);
                for (Field f : sorter.ordinaryFields) {
                    boolean value = bitbuffer.get();
                    logger.debug("Field {} set to {}", f.getName(), value);
                    try {
                        f.set(result, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalArgumentException("can't decode " + classOfT, e);
                    }
                }
                return result;
            } else {
                logger.debug("Bitlist(var-size)");
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
                        (sizeRange != null) ? decodeConstrainedInt(bitbuffer,
                                intRangeFromSizeRange(sizeRange)) :
                                badSize(classOfT);
                T result = instantiate(classOfT);
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
    }

    private static class SeqOfCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof List<?>;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            logger.debug("SEQUENCE OF");
            List<?> list = (List<?>) obj;
            SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
            if (sizeRange == null) {
                int position1 = bitbuffer.position();
                encodeLengthDeterminant(bitbuffer, list.size());
                logger.debug("unbound size {}, encoded as {}", list.size(),
                        bitbuffer.toBooleanStringFromPosition(position1));
                logger.debug("  all elems of Seq Of: {}", list);
                for (Object elem : list) {
                    encode2(bitbuffer, elem, new Annotation[] {});
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
                logger.debug("With Extension Marker, {} of range ({} <= {} <= {})",
                        (outsideOfRange ? "outside" : "inside"), sizeRange.minValue(), list.size(),
                        sizeRange.maxValue());
                if (outsideOfRange) { throw new UnsupportedOperationException(
                        "Sequence-of size range extensions are not implemented yet, range " +
                                sizeRange.minValue() + ".." + sizeRange.maxValue()
                                + ", requested size " + list.size()); }
            }
            logger.debug("seq-of of constrained size {}, encoding size...", list.size());
            encodeConstrainedInt(bitbuffer, list.size(), sizeRange.minValue(), sizeRange.maxValue());
            logger.debug("  all elems of Seq Of: {}", list);
            for (Object elem : list) {
                encode2(bitbuffer, elem, new Annotation[] {});
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
            logger.debug("SEQUENCE OF for {}", classOfT);
            SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
            long size = (sizeRange != null) ? decodeConstrainedInt(bitbuffer,
                    intRangeFromSizeRange(sizeRange)) :
                    decodeLengthDeterminant(bitbuffer);
            Collection<Object> coll = new ArrayList<Object>((int) size);
            for (int i = 0; i < size; i++) {
                Class<?>[] typeArgs = TypeResolver.resolveRawArguments(List.class, classOfT);
                Class<?> classOfElements = typeArgs[0];
                if (classOfElements == Unknown.class) { throw new IllegalArgumentException(
                        "Can't resolve type of elements for " + classOfT.getName()); }
                coll.add(decode2(bitbuffer, classOfElements, new Annotation[] {}));
            }
            T result = instantiate(classOfT, coll);
            return result;        }

    }


    private static class StringCoder implements Decoder, Encoder {

        @Override public <T> boolean canEncode(T obj, Annotation[] extraAnnotations) {
            return obj instanceof String || obj instanceof Asn1String;
        }

        @Override public <T> void encode(BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations) {
            Class<?> type = obj.getClass();
            AnnotationStore annotations = new AnnotationStore(type.getAnnotations(),
                    extraAnnotations);
            logger.debug("STRING {} of type {}", obj, obj.getClass().getName());
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
                encodeLengthDeterminant(bitbuffer, numOctets);
                logger.debug("UTF8String {},  length {} octets, encoded as {}", string, numOctets,
                        bitbuffer.toBooleanStringFromPosition(position1));
                int position2 = bitbuffer.position();
                for (int i = 0; i < stringbuffer.limit(); i++) {
                    bitbuffer.put(stringbuffer.get());
                }
                logger.debug("UTF8String {}, encoded length {} octets, value bits: {}", string,
                        numOctets, bitbuffer.toBooleanStringFromPosition(position2));
                return;
            } else if (fixedSize != null) {
                if (fixedSize.value() != string.length()) { throw new IllegalArgumentException(
                        "String length does not match constraints"); }
                int position = bitbuffer.position();
                for (int i = 0; i < fixedSize.value(); i++) {
                    encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
                }
                logger.debug("string encoded as <{}>",
                        bitbuffer.toBooleanStringFromPosition(position));
                return;
            } else if (sizeRange != null) {
                logger.debug("string length");
                encodeConstrainedInt(bitbuffer, string.length(), sizeRange.minValue(),
                        sizeRange.maxValue(), sizeRange.hasExtensionMarker());
                logger.debug("string content");
                for (int i = 0; i < string.length(); i++) {
                    encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
                }
                // logger.debug("string of type {} size {}: {}", obj.getClass().getName(),
                // binaryStringFromCollection(lengthBits), binaryStringFromCollection(valuebits));
                return;
            } else {
                int position1 = bitbuffer.position();
                encodeLengthDeterminant(bitbuffer, string.length());
                int position2 = bitbuffer.position();
                for (int i = 0; i < string.length(); i++) {
                    encodeChar(bitbuffer, string.charAt(i), restrictionAnnotation);
                }
                logger.debug("STRING {} size {}: {}", obj.getClass().getName(),
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
            logger.debug("String");
            RestrictedString restrictionAnnotation = annotations
                    .getAnnotation(RestrictedString.class);
            if (restrictionAnnotation == null) { throw new UnsupportedOperationException(
                    "Unrestricted character strings are not supported yet. All annotations: "
                            + Arrays.asList(classOfT.getAnnotations())); }
            if (restrictionAnnotation.value() == CharacterRestriction.UTF8String) {
                long numOctets = decodeLengthDeterminant(bitbuffer);
                List<Boolean> content = new ArrayList<Boolean>();
                for (int i = 0; i < numOctets * 8; i++) {
                    content.add(bitbuffer.get());
                }
                byte[] contentBytes = bytesFromCollection(content);
                String resultStr = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(contentBytes))
                        .toString();
                T result = instantiate(classOfT, resultStr);
                return result;
            } else {
                FixedSize fixedSize = annotations.getAnnotation(FixedSize.class);
                SizeRange sizeRange = annotations.getAnnotation(SizeRange.class);
                long numChars = (fixedSize != null) ? fixedSize.value() :
                        (sizeRange != null) ? decodeConstrainedInt(bitbuffer,
                                intRangeFromSizeRange(sizeRange)) :
                                decodeLengthDeterminant(bitbuffer);
                logger.debug("known-multiplier string, numchars: {}", numChars);
                StringBuilder stringBuilder = new StringBuilder((int) numChars);
                for (int c = 0; c < numChars; c++) {
                    stringBuilder.append(decodeRestrictedChar(bitbuffer, restrictionAnnotation));
                }
                String resultStr = stringBuilder.toString();
                logger.debug("Decoded as {}", resultStr);
                T result = instantiate(classOfT, resultStr);
                return result;
            }
        }

        private static void encodeChar(BitBuffer bitbuffer, char c, RestrictedString restriction) {
            logger.debug("char {}", c);
            switch (restriction.value()) {
                case IA5String:
                    if (restriction.alphabet() != DefaultAlphabet.class) { throw new UnsupportedOperationException(
                            "alphabet for IA5String is not supported yet."); }
                    encodeConstrainedInt(
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
                        encodeConstrainedInt(bitbuffer, buffer.get() & 0xff, 0, 255);
                    }
                    return;
                case VisibleString:
                case ISO646String:
                    if (restriction.alphabet() != DefaultAlphabet.class) {
                        char[] chars;
                        try {
                            chars = instantiate(restriction.alphabet()).chars().toCharArray();
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
                            encodeConstrainedInt(
                                    bitbuffer,
                                    index,
                                    0,
                                    chars.length - 1);
                            return;
                        } else {
                            encodeConstrainedInt(
                                    bitbuffer,
                                    StandardCharsets.US_ASCII.encode(CharBuffer.wrap(new char[] { c }))
                                            .get() & 0xff,
                                    0,
                                    126);
                            return;
                        }
                    } else {
                        encodeConstrainedInt(
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
                    byte charByte = (byte) decodeConstrainedInt(bitqueue, newRange(0, 127, false));
                    byte[] bytes = new byte[] { charByte };
                    String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes)).toString();
                    if (result.length() != 1) { throw new AssertionError("decoded more than one char ("
                            + result + ")"); }
                    return result;
                }
                case VisibleString:
                case ISO646String: {
                    if (restrictionAnnotation.alphabet() != DefaultAlphabet.class) {
                        char[] chars;
                        try {
                            chars = instantiate(restrictionAnnotation.alphabet()).chars().toCharArray();
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Uninstantinatable alphabet"
                                    + restrictionAnnotation.alphabet().getName());
                        }
                        if (BigInteger.valueOf(chars.length - 1).bitLength() < BigInteger.valueOf(126)
                                .bitLength()) {
                            Arrays.sort(chars);
                            int index = (byte) decodeConstrainedInt(bitqueue,
                                    newRange(0, chars.length - 1, false));
                            String strAlphabet = new String(chars);
                            char c = strAlphabet.charAt(index);
                            String result = new String("" + c);
                            return result;
                        } else {  // Encode normally
                            byte charByte = (byte) decodeConstrainedInt(bitqueue,
                                    newRange(0, 126, false));
                            byte[] bytes = new byte[] { charByte };
                            String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes))
                                    .toString();
                            if (result.length() != 1) { throw new AssertionError(
                                    "decoded more than one char (" + result + ")"); }
                            return result;
                        }
                    } else {  // Encode normally
                        byte charByte = (byte) decodeConstrainedInt(bitqueue, newRange(0, 126, false));
                        byte[] bytes = new byte[] { charByte };
                        String result = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(bytes))
                                .toString();
                        if (result.length() != 1) { throw new AssertionError(
                                "decoded more than one char (" + result + ")"); }
                        return result;
                    }
                }
                default:
                    throw new UnsupportedOperationException("String type " + restrictionAnnotation
                            + " is not supported yet");

            }
        }

    }


    private static <T> void encodeAsOpenType(
            BitBuffer bitbuffer, T obj, Annotation[] extraAnnotations)
            throws IllegalArgumentException, IllegalAccessException {
        logger.debug("OPEN TYPE for {}. Encoding preceedes length determinant", obj.getClass()
                .getName());
        BitBuffer tmpbuffer = ByteBitBuffer.createInfinite();
        encode2(tmpbuffer, obj, extraAnnotations);
        int numBytes = (tmpbuffer.position() + 7) / 8;
        logger.debug(
                "Encoding open type length determinant ({}) for {} (will be inserted before the open type content)",
                numBytes, obj.getClass().getName());
        encodeLengthDeterminant(bitbuffer, numBytes);
        tmpbuffer.flip();
        for (int i = 0; i < tmpbuffer.limit(); i++) {
            bitbuffer.put(tmpbuffer.get());
        }
    }

    private static <T> T decodeAsOpenType(BitBuffer bitbuffer,
            Class<T> classOfT,
            Annotation[] extraAnnotations) {
        logger.debug("OPEN TYPE for {}. Encoding preceedes length determinant",
                classOfT != null ? classOfT.getName() : "null");
        long numBytes = decodeLengthDeterminant(bitbuffer);
        BitBuffer openTypeBitBuffer = ByteBitBuffer.allocate((int)numBytes * 8);
        for (int i = 0; i < numBytes * 8; i++) {
            openTypeBitBuffer.put(bitbuffer.get());
        }
        openTypeBitBuffer.flip();
        if (classOfT != null) {
            T result = decode2(openTypeBitBuffer, classOfT, extraAnnotations);
            // Assert that padding bits are all 0.
            logger.debug("open type had {} padding bits");
            for (int i = 0; i < openTypeBitBuffer.remaining(); i++) {
                boolean paddingBit = openTypeBitBuffer.get();
                logger.debug("padding bit {} was <{}>", i, paddingBit ? "1" : "0");
                if (paddingBit) { throw new IllegalArgumentException("non-zero padding bit " + i
                        + " for open type " + classOfT.getName()); }
            }
            return result;
        } else {
            return null;
        }
    }

    private static <T> boolean hasNonNullExtensions(
            T obj, Asn1ContainerFieldSorter sorter)
                    throws IllegalArgumentException, IllegalAccessException {
        for (Field f : sorter.extensionFields) {
            if (f.get(obj) != null) { return true; }
        }
        return false;
    }


    /** This function only throws an exception, to be used in ternary (a?b:c) expression. */
    private static <T> long badSize(Class<T> classOfT) {
        throw new IllegalArgumentException("both size range and fixed size are null for "
                + classOfT.getName());
    }

    private static <T> Constructor<T> findConsturctor(Class<T> classOfT, Object... parameters) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] declaredConstructors = (Constructor<T>[]) classOfT
                .getDeclaredConstructors();
        for (Constructor<T> c : declaredConstructors) {
            Class<?>[] parameterTypes = c.getParameterTypes();
            if (parameterTypes.length == parameters.length) {
                boolean constructorIsOk = true;
                for (int i = 0; i < parameters.length; i++) {
                    if (!parameterTypes[i].isAssignableFrom(parameters[i].getClass())) {
                        constructorIsOk = false;
                        break;
                    }
                }
                if (constructorIsOk) { return c; }
            }
        }
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        throw new IllegalArgumentException("Can't get the " + parameters.length +
                "-argument constructor for parameter(s) "
                + Arrays.asList(parameters) +
                " of type(s) " + Arrays.asList(parameterTypes) + " for class "
                + classOfT.getName() + " (" + classOfT.getClass().getName() + " or " + Arrays.asList(classOfT.getClasses()) + ")" +
                ", all constructors: " + Arrays.asList(classOfT.getDeclaredConstructors()));
    }

    /** Instantiate a given class T using given parameters. */
    private static <T> T instantiate(Class<T> classOfT, Object... parameters) {
        if (classOfT == long.class) {
            if (parameters.length != 1 || !long.class.isAssignableFrom(parameters[0].getClass())) {
                throw new IllegalArgumentException("can't instantiate " + classOfT + " from " + Arrays.asList(parameters));
            } else {
                return (T) parameters[0];
            }
        } else if (classOfT == int.class) {
            if (parameters.length != 1 || !int.class.isAssignableFrom(parameters[0].getClass())) {
                throw new IllegalArgumentException("can't instantiate " + classOfT + " from " + Arrays.asList(parameters));
            } else {
                return (T) parameters[0];
            }
        } else if (classOfT == short.class) {
            if (parameters.length != 1 || !short.class.isAssignableFrom(parameters[0].getClass())) {
                throw new IllegalArgumentException("can't instantiate " + classOfT + " from " + Arrays.asList(parameters));
            } else {
                return (T) parameters[0];
            }
        } else {
            Class<?>[] parameterTypes = new Class<?>[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }
            Constructor<T> constructor = findConsturctor(classOfT, parameters);
            boolean constructorIsAccessible = constructor.isAccessible();
            constructor.setAccessible(true);
            T result;
            try {
                result = constructor.newInstance(parameters);
            } catch (IllegalArgumentException | InvocationTargetException | InstantiationException
                    | IllegalAccessException e) {
                throw new IllegalArgumentException("Can't instantiate " + classOfT.getName(), e);
            }
            constructor.setAccessible(constructorIsAccessible);
            return result;
        }
    }

    private static long decodeConstrainedInt(BitBuffer bitqueue, IntRange intRange) {
        long lowerBound = intRange.minValue();
        long upperBound = intRange.maxValue();
        boolean hasExtensionMarker = intRange.hasExtensionMarker();
        if (upperBound < lowerBound) { throw new IllegalArgumentException("Lower bound "
                + lowerBound + " is larger that upper bound " + upperBound); }
        if (hasExtensionMarker) {
            boolean extensionIsActive = bitqueue.get();
            if (extensionIsActive) { throw new UnsupportedOperationException(
                    "int extension are not supported yet"); }
        }
        final long range = upperBound - lowerBound + 1;
        if (range == 1) { return lowerBound; }
        int bitlength = BigInteger.valueOf(range - 1).bitLength();
        logger.trace("This int will require {} bits, available {}", bitlength, bitqueue.remaining());
        List<Boolean> relevantBits = new ArrayList<>();
        for (int i = 0; i < bitlength; i++) {
            relevantBits.add(bitqueue.get());
        }
        final BigInteger big = new BigInteger(binaryStringFromCollection(relevantBits), 2);  // This
                                                                                            // is
                                                                                            // very
                                                                                            // inefficient,
                                                                                            // I
                                                                                            // know.
        final long result = lowerBound + big.longValue();
        logger.debug("bits {} decoded as {} plus lower bound {} give {}",
                binaryStringFromCollection(relevantBits), big.longValue(), lowerBound, result);
        if ((result < intRange.minValue() || intRange.maxValue() < result)
                && !intRange.hasExtensionMarker()) { throw new AssertionError("Decoded value "
                + result + " is outside of range (" + intRange.minValue() + ".."
                + intRange.maxValue() + ")"); }
        return result;
    }



    private static boolean hasExtensionMarker(AnnotationStore annotations) {
        return annotations.getAnnotation(HasExtensionMarker.class) != null;
    }

    private static boolean isExtension(Field f) {
        return f.getAnnotation(IsExtension.class) != null;
    }

    private static boolean isMandatory(Field f) {
        return !isOptional(f);
    }

    private static boolean isOptional(Field f) {
        return f.getAnnotation(Asn1Optional.class) != null;
    }

    private static class Asn1ContainerFieldSorter {
        /** "Outside extension root" */
        List<Field> extensionFields = new ArrayList<>();
        /** "Within extension root" */
        List<Field> ordinaryFields = new ArrayList<>();
        List<Field> mandatoryOrdinaryFields = new ArrayList<>();
        List<Field> optionalOrdinaryFields = new ArrayList<>();
        List<Field> allFields = new ArrayList<>();  // Excluding test instrumentation.

        Map<Field, Boolean> originalAccess = new HashMap<>();

        Asn1ContainerFieldSorter(Class<?> type) {
            for (Field f : type.getDeclaredFields()) {
                if (isTestInstrumentation(f)) {
                    continue;
                }
                originalAccess.put(f, f.isAccessible());
                f.setAccessible(true);
                if (isExtension(f)) {
                    extensionFields.add(f);
                }
                else {
                    ordinaryFields.add(f);
                }
                allFields.add(f);
            }
            for (Field f : ordinaryFields) {
                if (isMandatory(f)) {
                    mandatoryOrdinaryFields.add(f);
                }
                else {
                    optionalOrdinaryFields.add(f);
                }
            }
        }

        public void revertAccess() {
            for (Entry<Field, Boolean> entry : originalAccess.entrySet()) {
                entry.getKey().setAccessible(entry.getValue());
            }
        }
    }

    private static boolean isTestInstrumentation(Field f) {
        return f.getName().startsWith("$");
    }

    private static void encodeLengthDeterminant(BitBuffer bitbuffer, int n) {
        encodeLengthDeterminant(bitbuffer, n, false);
    }

    private static void encodeLengthDeterminant(BitBuffer bitbuffer,
            int n,
            boolean isLengthOfBitmask) {
        if (isLengthOfBitmask) {
            if (n <= 64) {
                logger.debug(
                        "normally small length of bitmask, length {} <= 64 indicated as bit <0>", n);
                bitbuffer.put(false);
                encodeConstrainedInt(bitbuffer, n, 1, 64);
                return;
            } else {
                logger.debug(
                        "normally small length of bitmask, length {} > 64 indicated as bit <1>", n);
                bitbuffer.put(true);
                encodeLengthDeterminant(bitbuffer, n, false);
                return;
            }
        } else {
            int position = bitbuffer.position();
            if (n < 128) {
                bitbuffer.put(false);
                encodeConstrainedInt(bitbuffer, n, 0, 127);
                logger.debug("Length determinant {}, encoded as <{}>", n,
                        bitbuffer.toBooleanStringFromPosition(position));
                if (bitbuffer.position() - position != 8) { throw new AssertionError(
                        "length determinant encoded not as 8 bits"); }
                return;
            } else if (n < NUM_16K) {
                bitbuffer.put(true);
                bitbuffer.put(false);
                encodeConstrainedInt(bitbuffer, n, 0, NUM_16K - 1);
                logger.debug("Length determinant {}, encoded as 2bits+14bits: <{}>", n,
                        bitbuffer.toBooleanStringFromPosition(position));
                if (bitbuffer.position() - position != 16) { throw new AssertionError(
                        "length determinant encoded not as 16 bits"); }
                return;
            } else {
                throw new UnsupportedOperationException(
                        "Length greater than 16K is not supported yet.");
            }
        }
    }

    private static long decodeLengthDeterminant(BitBuffer bitbuffer) {
        return decodeLengthDeterminant(bitbuffer, false);
    }

    private static long decodeLengthDeterminant(BitBuffer bitbuffer, boolean isLengthOfBitmask) {
        if (isLengthOfBitmask) {
            logger.debug("decoding length of bitmask");
            boolean isGreaterThan64 = bitbuffer.get();
            logger.debug(
                    "length determinant extension preamble size flag: <{}> (preamble size {} 64)",
                    isGreaterThan64 ? "1" : "0", isGreaterThan64 ? ">" : "<=");
            if (!isGreaterThan64) {
                long result = decodeConstrainedInt(bitbuffer, newRange(1, 64, false));
                logger.debug("normally small length of bitmask, length <= 64, decoded as {}",
                        result);
                return result;
            } else {
                logger.debug("normally small length of bitmask, length > 64, decoding as ordinary length determinant...");
                return decodeLengthDeterminant(bitbuffer, false);
            }
        } else {
            boolean bit8 = bitbuffer.get();
            if (!bit8) {  // then value is less than 128
                long result = decodeConstrainedInt(bitbuffer, newRange(0, 127, false));
                logger.debug("length determinant, decoded as {}", result);
                return result;
            } else {
                boolean bit7 = bitbuffer.get();
                if (!bit7) {  // then value is less than 16K
                    long result = decodeConstrainedInt(bitbuffer, newRange(0, NUM_16K - 1, false));
                    logger.debug("length determinant, decoded as {}", result);
                    return result;
                } else {  // "Large" n
                    throw new UnsupportedOperationException(
                            "lengthes longer than 16K are not supported yet.");
                }
            }
        }
    }

    private static void encodeConstrainedInt(
            final BitBuffer bitbuffer,
            final long value,
            final long lowerBound,
            final long upperBound) {
        encodeConstrainedInt(bitbuffer, value, lowerBound, upperBound, false);
    }

    private static void encodeConstrainedInt(
            final BitBuffer bitbuffer,
            final long value,
            final long lowerBound,
            final long upperBound,
            final boolean hasExtensionMarker) {
        if (upperBound < lowerBound) { throw new IllegalArgumentException("Lower bound "
                + lowerBound + " is larger than upper bound " + upperBound); }
        if (!hasExtensionMarker && (value < lowerBound || value > upperBound)) { throw new IllegalArgumentException(
                "Value " + value + " is outside of fixed range " +
                        lowerBound + ".." + upperBound); }
        final long range = upperBound - lowerBound + 1;
        final int position = bitbuffer.position();
        if (hasExtensionMarker) {
            boolean outsideOfRange = value < lowerBound || value > upperBound;
            logger.debug("constrained int with extension marker, {} extension range",
                    outsideOfRange ? "outside" : "within", outsideOfRange ? "1" : "0");
            bitbuffer.put(outsideOfRange);
            if (outsideOfRange) { throw new UnsupportedOperationException(
                    "INT extensions are not supported yet"); }
        }
        if (range == 1) {
            logger.debug("constrained int of empty range, resulting in empty encoding <>");
            return;
        }
        final BigInteger big = BigInteger.valueOf(value - lowerBound);
        final int numPaddingBits = BigInteger.valueOf(range - 1).bitLength() - big.bitLength();
        for (int i = 0; i < numPaddingBits; i++) {
            bitbuffer.put(false);
        }
        for (int i = big.bitLength() - 1; i >= 0; i--) {
            bitbuffer.put(big.testBit(i));
        }
        logger.debug("constrained int {} encoded as <{}>", value,
                bitbuffer.toBooleanStringFromPosition(position));
        return;
    }

    private static List<Boolean> paddedTo(int length, List<Boolean> bitlist) {
        if (length < bitlist.size()) { throw new IllegalArgumentException(
                "List is longer then desired length, " + bitlist.size() + " > " + length); }
        Boolean[] buffer = new Boolean[length - bitlist.size()];
        Arrays.fill(buffer, false);
        List<Boolean> result = new ArrayList<>(length);
        result.addAll(Arrays.asList(buffer));
        result.addAll(bitlist);
        return result;
    }

    public static byte[] bytesFromCollection(List<Boolean> bitlist) {
        int sizeBytes = (bitlist.size() + 7) / 8;
        byte[] result = new byte[sizeBytes];
        int byteId = 0;
        byte bitId = 7;
        for (Boolean b : bitlist) {
            logger.trace("bitId: {}, byteId: {}, value: {}", bitId, byteId, b);
            result[byteId] |= (b ? 1 : 0) << bitId;
            bitId--;
            if (bitId < 0) {
                bitId = 7;
                byteId++;
            }
        }
        int nZeros = sizeBytes * 8 - bitlist.size();
        String zeros = nZeros > 0 ? String.format("%0" + nZeros + "d", 0) : "";
        logger.debug("Padding bits ({}): <{}>", nZeros, zeros);
        return result;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String hexStringFromBytes(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] bytesFromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String binaryStringFromCollection(Collection<Boolean> bitlist) {
        StringBuilder sb = new StringBuilder(bitlist.size());
        for (Boolean b : bitlist) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public static String binaryStringFromBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    public static byte[] bytesFromBinaryString(String s) {
        int len = s.length();
        byte[] result = new byte[(len + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for (int i = 0; i < len; i++)
            if ((c = s.charAt(i)) == '1') result[i / Byte.SIZE] = (byte) (result[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if (c != '0')
                throw new IllegalArgumentException();
        return result;
    }

    private static ArrayDeque<Boolean> collectionFromBinaryString(String s) {
        ArrayDeque<Boolean> result = new ArrayDeque<>(s.length());
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '1' && s.charAt(i) != '0') { throw new IllegalArgumentException(
                    "bad character in 'binary' string " + s.charAt(i)); }
            result.add(s.charAt(i) == '1');
        }
        return result;
    }

    private static ListBitBuffer listBitBufferFromBinaryString(String s) {
        ListBitBuffer result = ListBitBuffer.empty();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '1' && s.charAt(i) != '0') { throw new IllegalArgumentException(
                    "bad character in 'binary' string " + s.charAt(i)); }
            result.put(s.charAt(i) == '1');
        }
        return result;
    }

    private static BitBuffer bitBufferFromBinaryString(String s) {
        ByteBitBuffer result = ByteBitBuffer.allocate(s.length());
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '1' && s.charAt(i) != '0') { throw new IllegalArgumentException(
                    "bad character in 'binary' string " + s.charAt(i)); }
            result.put(s.charAt(i) == '1');
        }
        result.flip();
        return result;
    }

}
