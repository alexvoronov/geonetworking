package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.Asn1VarSizeBitstring;
import net.gcdc.asn1.datatypes.Bitstring;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.Choice;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.HasExtensionMarker;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.IsExtension;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "quick-and-dirty" implementation of ASN.1 encoder for UPER (Unaligned Packed Encoding Rules).
 *
 * @see ITU-T Recommendation <a href="http://www.itu.int/ITU-T/recommendations/rec.aspx?rec=x.691">X.691</a>
 *
 */
public class UperEncoder {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    private final static int NUM_16K = 16384;
    @SuppressWarnings("unused")
    private final static int NUM_32K = 32768;
    @SuppressWarnings("unused")
    private final static int NUM_48K = 49152;
    @SuppressWarnings("unused")
    private final static int NUM_64K = 65536;

    private static final Map<Class<?>, IntRange> DEFAULT_RANGE;

    static {
        DEFAULT_RANGE = new HashMap<>();
        DEFAULT_RANGE.put(  short.class, newRange(  Short.MIN_VALUE,   Short.MAX_VALUE, false));
        DEFAULT_RANGE.put(  Short.class, newRange(  Short.MIN_VALUE,   Short.MAX_VALUE, false));
        DEFAULT_RANGE.put(    int.class, newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        DEFAULT_RANGE.put(Integer.class, newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        DEFAULT_RANGE.put(   long.class, newRange(   Long.MIN_VALUE,    Long.MAX_VALUE, false));
        DEFAULT_RANGE.put(   Long.class, newRange(   Long.MIN_VALUE,    Long.MAX_VALUE, false));

        // Asn1Integer have max range of Long. Bigger ranges will require Asn1BigInteger.
        DEFAULT_RANGE.put(Asn1Integer.class, newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
    }

    private static IntRange newRange(final long minValue, final long maxValue, final boolean hasExtensionMarker) {
        return new IntRange() {
            @Override public Class<? extends Annotation> annotationType() { return IntRange.class; }
            @Override public long minValue() { return minValue; }
            @Override public long maxValue() { return maxValue; }
            @Override public boolean hasExtensionMarker() { return hasExtensionMarker; }
        };
    }

    public static <T> byte[] encode(T obj) throws IllegalArgumentException,
    IllegalAccessException {
        return boolToBits(encodeAsList(obj));
    }

    public static <T> T decode(byte[] bytes, Class<T> classOfT) throws InstantiationException,
            IllegalAccessException {
        Queue<Boolean> bitQueue = queueFromBinary(toBinary(bytes));
        logger.debug("Decoding {} from {}", classOfT.getName(), toBinary(bitQueue));
        T result = decode(bitQueue, classOfT);
        if (bitQueue.size() > 7) { throw new IllegalArgumentException("Can't fully decode "
                + classOfT.getName() + ", remaining bits: " + bitQueue); }
        return result;
        // throw new IllegalArgumentException("Can't decode " + classOfT.getName() + " because of "
        // + e);
    }

    public static <T> List<Boolean> encodeAsList(T obj) throws IllegalArgumentException,
            IllegalAccessException {
        Class<?> type = obj.getClass();
        if (obj instanceof Asn1Integer || obj instanceof Long || obj instanceof Integer || obj instanceof Short) {
            IntRange range = type.getAnnotation(IntRange.class);
            if (range == null) { range = DEFAULT_RANGE.get(obj); }
            List<Boolean> result = encodeConstrainedInt(((Asn1Integer) obj).value(), range.minValue(), range.maxValue(), range.hasExtensionMarker());
            logger.debug("INT({}): {}", obj, toBinary(result));
            return result;
        } else if (obj instanceof Byte) {
            List<Boolean> result = encodeConstrainedInt(((Byte) obj).byteValue() & 0xff, 0, 255);
            logger.debug("BYTE {}", result);
            return result;
        } else if (obj instanceof Boolean) {
            List<Boolean> result = new ArrayList<>();
            result.add((Boolean) obj);
            logger.debug("BOOLEAN {}", obj);
            return result;
        } else if (type.getAnnotation(Sequence.class) != null) {
            logger.debug("SEQUENCE {}", type.getName());
            List<Boolean> bitlist = new ArrayList<>();
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
            if (hasExtensionMarker(type)) {
                logger.debug("with extension marker");
                bitlist.add(!sorter.extensionFields.isEmpty());
            }
            // Bitmask for optional fields.
            for (Field f : sorter.optionalOrdinaryFields) {
                logger.debug("with optional field {} {}", f.getName(), f.get(obj) != null ? "present" : "absent" );
                bitlist.add(f.get(obj) != null);  // null means the field is absent.
            }
            // All ordinary fields (fields within extension root).
            for (Field f : sorter.ordinaryFields) {
                if ((isMandatory(f) || f.get(obj) != null) && !isTestInstrumentation(f)) {
                    logger.debug("Field : {}", f.getName());
                    bitlist.addAll(encodeAsList(f.get(obj)));
                }
            }
            if (hasExtensionMarker(type) && !sorter.extensionFields.isEmpty()) {
                throw new UnsupportedOperationException("Extension fields are not implemented yet");
            }
            sorter.revertAccess();
            return bitlist;
        } else if (type.getAnnotation(Choice.class) != null) {
            logger.debug("CHOICE");
            List<Boolean> bitlist = new ArrayList<>();
            int index = 0;
            int currentIndex = 0;
            Object nonNullObject = null;
            Field nonNullField = null;
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
            for (Field f : sorter.ordinaryFields) {
                if (f.get(obj) != null) {
                    index = currentIndex;
                    nonNullObject = f.get(obj);
                    nonNullField = f;
                    break;
                }
                currentIndex++;
            }
            if (nonNullObject != null) {
                if (hasExtensionMarker(type)) {
                    boolean extensionBit = false;
                    logger.debug("with extension marker, set to {}", extensionBit);
                    bitlist.add(extensionBit);
                }
                if (sorter.ordinaryFields.size() > 1) {  // Encode index only if more than one.
                    logger.debug("with chosen element indexed {}", index);
                    bitlist.addAll(encodeConstrainedInt(index, 0, sorter.ordinaryFields.size() - 1));
                }
                bitlist.addAll(encodeAsList(nonNullObject));
                return bitlist;
            } else if (hasExtensionMarker(type)) {
                currentIndex = 0;
                for (Field f : sorter.extensionFields) {
                    if (f.get(obj) != null) {
                        index = currentIndex;
                        nonNullObject = f.get(obj);
                        nonNullField = f;
                        break;
                    }
                    currentIndex++;
                }
                if (nonNullField == null) {
                    throw new IllegalArgumentException("All fields of Choice are null");
                }
                boolean extensionBit = true;
                logger.debug("with extension marker, set to {}", extensionBit);
                bitlist.add(extensionBit);
                throw new UnsupportedOperationException("Choice extension is not implemented yet");
            } else {
                throw new IllegalArgumentException("Not Extension and All ordinary fields of Choice are null");
            }
        } else if (type.isEnum()) {
            if (!hasExtensionMarker(type)) {
                List<?> values = Arrays.asList(type.getEnumConstants());
                int index = values.indexOf(obj);
                List<Boolean> result = encodeConstrainedInt(index, 0, values.size() - 1);
                logger.debug("ENUM({}) {}", index, toBinary(result));
                return result;
            } else {
                List<Boolean> result = new ArrayList<>();
                List<Object> valuesWithinExtensionRoot = new ArrayList<>();
                List<Object> valuesOutsideExtensionRoot = new ArrayList<>();
                for (Object c : type.getEnumConstants()) {
                    if (c.getClass().getAnnotation(IsExtension.class) == null) {  // double-check getClass
                        valuesWithinExtensionRoot.add(c);
                    } else {
                        valuesOutsideExtensionRoot.add(c);
                    }
                }
                if (valuesWithinExtensionRoot.contains(obj)) {
                    result.add(false);
                    int index = valuesWithinExtensionRoot.indexOf(obj);
                    result.addAll(encodeConstrainedInt(index, 0, valuesWithinExtensionRoot.size() - 1));
                    logger.debug("ENUM w/ext ({}) {}", index, toBinary(result));
                    return result;
                } else {
                    throw new UnsupportedOperationException("Enum extensions are not supported yet");
                }
            }
        } else if (type.getAnnotation(Bitstring.class) != null && !(obj instanceof Asn1VarSizeBitstring)) {
            if(hasExtensionMarker(type)) {
                throw new UnsupportedOperationException("Bitstring with extensions is not implemented yet");
            }
            FixedSize size = type.getAnnotation(FixedSize.class);
            if (size != null) {
                Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(type);
                if (sorter.ordinaryFields.size() != size.value()) { throw new AssertionError(
                        "Declared size (" + size.value() +
                                ") and number of fields (" + sorter.ordinaryFields.size() +
                                ") do not match!"); }
                List<Boolean> bitlist = new ArrayList<>();
                for (Field f : sorter.ordinaryFields) {
                    bitlist.add(f.getBoolean(obj));
                }
                logger.debug("BITSTRING {}: {}", obj.getClass().getName(), toBinary(bitlist));
                return bitlist;
            } else {
                throw new UnsupportedOperationException("Bitstrings of variable size are not implemented yet");
            }
        } else if (type.getAnnotation(Bitstring.class) != null && (obj instanceof Asn1VarSizeBitstring)) {
            if(hasExtensionMarker(type)) {
                throw new UnsupportedOperationException("Bitstring with extensions is not implemented yet");
            }
            Asn1VarSizeBitstring bitstring = (Asn1VarSizeBitstring) obj;
            FixedSize fixedSize = type.getAnnotation(FixedSize.class);
            SizeRange sizeRange = type.getAnnotation(SizeRange.class);
            if (fixedSize != null) {
                List<Boolean> bitlist = new ArrayList<>();
                for (int i = 0; i < fixedSize.value(); i++) {
                    bitlist.add(bitstring.getBit(i));
                }
                logger.debug("BITSTRING {}: {}", obj.getClass().getName(), toBinary(bitlist));
                return bitlist;
            } else if (sizeRange != null) {
                List<Boolean> lengthBits = encodeConstrainedInt(bitstring.size(), sizeRange.minValue(), sizeRange.maxValue());
                List<Boolean> valuebits = new ArrayList<>();
                for (int i = 0; i < bitstring.size(); i++) {
                    valuebits.add(bitstring.getBit(i));
                }
                logger.debug("BITSTRING {} size {}: {}", obj.getClass().getName(), toBinary(lengthBits), toBinary(valuebits));
                List<Boolean> result = new ArrayList<>();
                result.addAll(lengthBits);
                result.addAll(valuebits);
                return result;
            } else {
                throw new IllegalArgumentException("Both SizeRange and FixedSize are null");
            }
        } else if (obj instanceof List<?>) {
            logger.debug("SEQUENCE OF");
            List<Boolean> bitlist = new ArrayList<>();
            List<?> objAsList = (List<?>) obj;
            SizeRange sizeRange = type.getAnnotation(SizeRange.class);
            if (sizeRange == null) {
                throw new UnsupportedOperationException("Sequence-of without size range is not implemented yet ");
            }
            boolean outsideOfRange =  objAsList.size() < sizeRange.minValue() || sizeRange.maxValue() < objAsList.size();
            if (outsideOfRange && !sizeRange.hasExtensionMarker()) {
                throw new IllegalArgumentException("Out-of-range size for " + obj.getClass() + ", expected " +
                        sizeRange.minValue() + ".." + sizeRange.maxValue() + ", got " + objAsList.size());
            }
            if (sizeRange.hasExtensionMarker()) {
                bitlist.add(outsideOfRange);
                logger.debug("With Extension Marker, {} of range ({} <= {} <= {})", (outsideOfRange?"outside":"inside"), sizeRange.minValue(), objAsList.size(), sizeRange.maxValue());
                if (outsideOfRange) {
                    throw new UnsupportedOperationException("Sequence-of size range extensions are not implemented yet, range " +
                            sizeRange.minValue() + ".." + sizeRange.maxValue() + ", requested size " + objAsList.size());
                }
            }
            List<Boolean> sizeBits = encodeConstrainedInt(objAsList.size(), sizeRange.minValue(), sizeRange.maxValue());
            logger.debug("size {} : {}", objAsList.size(), toBinary(sizeBits));
            for (Object o : objAsList) { logger.debug("  all elems of Seq Of: {}", o); }
            bitlist.addAll(sizeBits);
            for (Object elem : objAsList) {
                bitlist.addAll(encodeAsList(elem));
            }
            return bitlist;
        } else if (obj instanceof String || obj instanceof Asn1String) {
            logger.debug("STRING {}", obj);
            String string = (obj instanceof String) ? ((String) obj) : ((Asn1String) obj).value();
            RestrictedString restrictionAnnotation = type.getAnnotation(RestrictedString.class);
            if (restrictionAnnotation == null) {
                throw new UnsupportedOperationException("Unrestricted character strings are not supported yet. All annotations: " + Arrays.asList(type.getAnnotations()));
            }
            CharacterRestriction restriction = restrictionAnnotation.value();
            FixedSize fixedSize = type.getAnnotation(FixedSize.class);
            SizeRange sizeRange = type.getAnnotation(SizeRange.class);
            if (fixedSize != null && fixedSize.value() != string.length()) {
                throw new IllegalArgumentException("Bad string length, expected " + fixedSize.value() + ", got " + string.length());
            }
            if (sizeRange != null && (string.length() < sizeRange.minValue() || sizeRange.maxValue() < string.length())) {
                throw new IllegalArgumentException("Bad string length, expected " + sizeRange.minValue() + ".." + sizeRange.maxValue() + ", got " + string.length());
            }
            if (restriction == CharacterRestriction.UTF8String) {  // UTF8 length varies, so no sizes.
                List<Boolean> bitlist = new ArrayList<>();
                List<Boolean> stringEncoding = new ArrayList<>();
                for (char c : string.toCharArray()) {
                    stringEncoding.addAll(encodeChar(c, restriction));
                }
                if (stringEncoding.size() % 8 != 0) {
                    throw new AssertionError("utf8 encoding resulted not in multiple of 8 bits");
                }
                int numOctets = stringEncoding.size() / 8;
                List<Boolean> lengthEncoding = encodeLengthDeterminant(numOctets);
                bitlist.addAll(lengthEncoding);
                bitlist.addAll(stringEncoding);
                logger.debug("UTF8String {}, length det {} ({}) bits {}", string, stringEncoding.size(), toBinary(lengthEncoding), toBinary(stringEncoding));
                return bitlist;
            } else if (fixedSize != null) {
                if (fixedSize.value() != string.length()) {
                    throw new IllegalArgumentException("String length does not match constraints");
                }
                List<Boolean> bitlist = new ArrayList<>();
                for (int i = 0; i < fixedSize.value(); i++) {
                    bitlist.addAll(encodeChar(string.charAt(i), restriction));
                }
                logger.debug("STRING {}: {}", obj.getClass().getName(), toBinary(bitlist));
                return bitlist;
            } else if (sizeRange != null) {
                List<Boolean> lengthBits = encodeConstrainedInt(string.length(), sizeRange.minValue(), sizeRange.maxValue());
                List<Boolean> valuebits = new ArrayList<>();
                for (int i = 0; i < string.length(); i++) {
                    valuebits.addAll(encodeChar(string.charAt(i), restriction));
                }
                logger.debug("STRING {} size {}: {}", obj.getClass().getName(), toBinary(lengthBits), toBinary(valuebits));
                List<Boolean> result = new ArrayList<>();
                result.addAll(lengthBits);
                result.addAll(valuebits);
                return result;
            } else {
                throw new IllegalArgumentException("Both SizeRange and FixedSize are null");
            }
        } else {
            throw new UnsupportedOperationException("Can't encode type " + obj.getClass());
        }
    }

    public static <T> T decode(Queue<Boolean> bitlist, Class<T> classOfT) throws InstantiationException, IllegalAccessException {
        logger.debug("Decoding {} from remaining bits ({}): {}", classOfT.getName(), bitlist.size(), toBinary(bitlist));
        if (Asn1Integer.class.isAssignableFrom(classOfT)) {
            IntRange intRange = classOfT.getAnnotation(IntRange.class);
            if (intRange == null) { intRange = DEFAULT_RANGE.get(classOfT); }
            logger.debug("Integer, range {}..{}", intRange.minValue(), intRange.maxValue());
            long value = decodeConstraainedInt(bitlist, intRange);
            Constructor<T> constructor = null;
            Class<?>[] numericTypes = new Class<?> [] {long.class, int.class, short.class};
            for (Class<?> t : numericTypes) {
                try {
                    constructor = classOfT.getConstructor(t);
                } catch (NoSuchMethodException e) {
                    // ignore
                } catch (SecurityException e) {
                    throw new IllegalArgumentException("can't access constructor of " + classOfT.getName() + ": " + e);
                }
            }
            if (constructor == null) {
                throw new IllegalArgumentException("can't find any numeric constructor for " + classOfT.getName() + ", all constructors " + Arrays.asList(classOfT.getConstructors()));
            }
                try {
                    Class<?> typeOfConstructorArgument = constructor.getParameterTypes()[0];
                    logger.debug("constructor type is {}", typeOfConstructorArgument.getName());
                    if (typeOfConstructorArgument.isAssignableFrom(long.class)) {
                        return constructor.newInstance(value);
                    } else if  (typeOfConstructorArgument.isAssignableFrom(int.class)) {
                        return constructor.newInstance((int)value);
                    } else if  (typeOfConstructorArgument.isAssignableFrom(short.class)) {
                        return constructor.newInstance((short)value);
                    } else {
                        throw new IllegalArgumentException("unrecognized constructor argument " + typeOfConstructorArgument.getName());
                    }
                } catch (IllegalArgumentException | InvocationTargetException e1) {
                    throw new IllegalArgumentException("failed to invoke constructor of " + classOfT.getName() + ": " + e1);
                }
           // }
        } else if (classOfT.getAnnotation(Sequence.class) != null) {
            T result = classOfT.newInstance();
            Asn1ContainerFieldSorter sorter = new Asn1ContainerFieldSorter(classOfT);
            if (hasExtensionMarker(classOfT)) {
                logger.debug("with extension marker");
                boolean extensionPresent = bitlist.poll();
                //bitlist.add(!sorter.extensionFields.isEmpty());
            }
            // Bitmask for optional fields.
            Queue<Boolean> optionalFieldsMask = new ArrayDeque<Boolean>(sorter.optionalOrdinaryFields.size());
            for (Field f : sorter.optionalOrdinaryFields) {
                logger.debug("optional field {}", f);
                optionalFieldsMask.add(bitlist.poll());
            }
            // All ordinary fields (fields within extension root).
            for (Field f : sorter.ordinaryFields) {
                if (!isTestInstrumentation(f) && (isMandatory(f) || (isOptional(f) && optionalFieldsMask.poll()))) {
                    logger.debug("Field : {}", f.getName());
                    f.set(result, decode(bitlist, f.getType()));
                }
            }
            if (hasExtensionMarker(classOfT) && !sorter.extensionFields.isEmpty()) {
                throw new UnsupportedOperationException("Extension fields are not implemented yet");
            }
            sorter.revertAccess();
            return result;
        } else {
            throw new IllegalArgumentException("can't decode class " + classOfT.getName() + ", annotations: " + Arrays.asList(classOfT.getAnnotations()));
        }
    }


    private static long decodeConstraainedInt(Queue<Boolean> bitqueue, IntRange intRange) {
        long lowerBound = intRange.minValue();
        long upperBound = intRange.maxValue();
        boolean hasExtensionMarker = intRange.hasExtensionMarker();
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Lower bound " + lowerBound + " is larger that upper bound " + upperBound);
        }
        if (hasExtensionMarker) {
            boolean extensionBit = bitqueue.poll();
            if (extensionBit) {
                throw new UnsupportedOperationException("int extension are not supported yet");
            }
        }
        final long range = upperBound - lowerBound + 1;
        if (range == 1) { return lowerBound; }
        int bitlength = BigInteger.valueOf(range-1).bitLength();
        logger.debug("This int will require {} bits, available {}", bitlength, bitqueue.size());
        List<Boolean> relevantBits = new ArrayList<>();
        for (int i = 0; i < bitlength; i++) {
            if (bitqueue.isEmpty()) {
                throw new IllegalArgumentException("Reached end of input bitlist");
            }
            relevantBits.add(bitqueue.poll());
        }
        final BigInteger big = new BigInteger(+1, fromBinaryString(toBinary(relevantBits)));  // This is very inefficient, I know.
        return lowerBound + big.longValue();
    }

    public static List<Boolean> encodeChar(char c, CharacterRestriction restriction) {
        switch (restriction) {
            case IA5String:
                return encodeConstrainedInt(
                        StandardCharsets.US_ASCII.encode(CharBuffer.wrap(new char[] {c})).get() & 0xff,
                        0,
                        127);
            case UTF8String:
                List<Boolean> bitlist = new ArrayList<>();
                ByteBuffer buffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(new char[] {c}));
                for (int i = 0; i < buffer.limit(); i++) {
                    bitlist.addAll(encodeConstrainedInt(buffer.get() & 0xff, 0, 255));
                }
                return bitlist;
            default:
                throw new UnsupportedOperationException("String type " + restriction + " is not supported yet");
        }
    }

    public static byte[] boolToBits(List<Boolean> bitlist) {
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
        return result;
    }

    private static boolean hasExtensionMarker(Class<?> type) {
        return type.getAnnotation(HasExtensionMarker.class) != null;
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

        Map<Field, Boolean> originalAccess = new HashMap<>();

        Asn1ContainerFieldSorter(Class<?> type) {
            for (Field f : type.getDeclaredFields()) {
                if (isTestInstrumentation(f)) { continue; }
                originalAccess.put(f, f.isAccessible());
                f.setAccessible(true);
                if (isExtension(f)) { extensionFields.add(f); }
                else { ordinaryFields.add(f); }
            }
            for (Field f : ordinaryFields) {
                if (isMandatory(f)) { mandatoryOrdinaryFields.add(f); }
                else { optionalOrdinaryFields.add(f); }
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

    public static List<Boolean> encodeLengthDeterminant(int n) {
        final List<Boolean> bitlist = new ArrayList<>();
        if (n < 128) {
            bitlist.add(false);
            bitlist.addAll(encodeConstrainedInt(n, 0, 127));
            logger.debug("Length determinant {}: {}", n, toBinary(bitlist));
            if (bitlist.size() != 8) {
                throw new AssertionError("length determinant encoded not as 8 bits");
            }
            return bitlist;
        } else if (n < NUM_16K) {
            bitlist.add(true);
            bitlist.add(false);
            bitlist.addAll(encodeConstrainedInt(n, 0, NUM_16K-1));
            logger.debug("Length determinant {}: {}", n, toBinary(bitlist));
            if (bitlist.size() != 16) {
                throw new AssertionError("length determinant encoded not as 16 bits");
            }
            return bitlist;
        } else {
            throw new UnsupportedOperationException("Length greater than 16K is not supported yet.");
        }
    }

    public static List<Boolean> encodeConstrainedInt(
            final long value,
            final long lowerBound,
            final long upperBound) {
        return encodeConstrainedInt(value, lowerBound, upperBound, false);
    }
    public static List<Boolean> encodeConstrainedInt(
            final long value,
            final long lowerBound,
            final long upperBound,
            final boolean hasExtensionMarker) {
        if (upperBound < lowerBound) {
            throw new IllegalArgumentException("Lower bound " + lowerBound + " is larger that upper bound " + upperBound);
        }
        if (!hasExtensionMarker && (value < lowerBound || value > upperBound)) {
            throw new IllegalArgumentException("Value " + value + " is outside of fixed range " +
                    lowerBound + ".." + upperBound);
        }
        final long range = upperBound - lowerBound + 1;
        final List<Boolean> bitlist = new ArrayList<>();
        if (hasExtensionMarker) {
            boolean outsideOfRange = value < lowerBound || value > upperBound;
            logger.debug("With extension marker, {} extension range", outsideOfRange? "outside":"within");
            bitlist.add(outsideOfRange);
            if (outsideOfRange) {
                throw new UnsupportedOperationException("INT extensions are not supported yet");
            }
        }
        if (range == 1) { return bitlist; }
        final BigInteger big = BigInteger.valueOf(value - lowerBound);
        for (int i = big.bitLength() - 1; i >= 0; i--) {
            bitlist.add(big.testBit(i));
        }
        int requiredLength = BigInteger.valueOf(range - 1).bitLength() + (hasExtensionMarker ? 1 : 0);
        logger.trace("val {} ({}..{}) required length: {}, resulting len: {}, maxval: {}",
                value, lowerBound, upperBound, requiredLength, paddedTo(requiredLength, bitlist).size(), toBinary( BigInteger.valueOf(range).toByteArray()));
        return paddedTo(requiredLength, bitlist);
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

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] fromHexString(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String toBinary(Collection<Boolean> bitlist) {
        StringBuilder sb = new StringBuilder(bitlist.size());
        for (Boolean b : bitlist) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    public static String toBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    public static byte[] fromBinaryString(String s) {
        int len = s.length();
        byte[] result = new byte[(len + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for (int i = 0; i < len; i++)
            if ((c = s.charAt(i)) == '1')
                result[i / Byte.SIZE] = (byte) (result[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if (c != '0')
                throw new IllegalArgumentException();
        return result;
    }

    private static ArrayDeque<Boolean> queueFromBinary(String s) {
        ArrayDeque<Boolean> result = new ArrayDeque<>(s.length());
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '1' && s.charAt(i) != '0') {
                throw new IllegalArgumentException("bad character in 'binary' string " + s.charAt(i));
            }
            result.add(s.charAt(i) == '1');
        }
        return result;
    }
}
