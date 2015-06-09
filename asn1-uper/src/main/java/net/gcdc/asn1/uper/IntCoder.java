package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.IntRange;

class IntCoder implements Encoder, Decoder {

    private static final Map<Class<?>, IntRange> DEFAULT_RANGE;

    static {
        DEFAULT_RANGE = new HashMap<>();
        DEFAULT_RANGE.put(short.class, UperEncoder.newRange(Short.MIN_VALUE, Short.MAX_VALUE, false));
        DEFAULT_RANGE.put(Short.class, UperEncoder.newRange(Short.MIN_VALUE, Short.MAX_VALUE, false));
        DEFAULT_RANGE.put(int.class, UperEncoder.newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        DEFAULT_RANGE.put(Integer.class, UperEncoder.newRange(Integer.MIN_VALUE, Integer.MAX_VALUE, false));
        DEFAULT_RANGE.put(long.class, UperEncoder.newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
        DEFAULT_RANGE.put(Long.class, UperEncoder.newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
        // Byte is not part of this, since we treat byte as unsigned byte, while the rest we treat
        // as it is.

        // Asn1Integer have max range of Long. Bigger ranges require Asn1BigInteger.
        DEFAULT_RANGE.put(Asn1Integer.class, UperEncoder.newRange(Long.MIN_VALUE, Long.MAX_VALUE, false));
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
        UperEncoder.logger.debug("INTEGER");
        IntRange intRange = annotations.getAnnotation(IntRange.class);
        if (intRange == null) {
            intRange = DEFAULT_RANGE.get(classOfT);
        }
        UperEncoder.logger.debug("Integer, range {}..{}", intRange.minValue(), intRange.maxValue());
        long value = UperEncoder.decodeConstrainedInt(bitbuffer, intRange);
        UperEncoder.logger.debug("decoded as {}", value);
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
        UperEncoder.encodeConstrainedInt(bitbuffer, ((Asn1Integer) obj).value(), range.minValue(),
                range.maxValue(), range.hasExtensionMarker());
        UperEncoder.logger.debug("INT({}): {}", obj, bitbuffer.toBooleanStringFromPosition(position));
        return;
    }

}