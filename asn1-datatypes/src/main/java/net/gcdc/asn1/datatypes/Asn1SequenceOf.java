package net.gcdc.asn1.datatypes;

import java.lang.reflect.ParameterizedType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent ASN.1 construct "SEQUENCE OF".
 * <p/>
 * Extending classes should specify concrete types for T, generic collections can't be decoded (yet?).
 * <p/>
 * Usage example:
 * <PRE>
 * <code>
 * {@literal @}Sequence
 * public class Person {
 *     {@literal @}IntRange(minValue=0, maxValue=100, hasExtensionMarker=true)
 *     int age;
 *     Children children;
 * }
 * public class Children extends {@code Asn1SequenceOf<ChildInformation> } {
 *     public Children() { super(); }
 *     public Children({@code Collection<ChildInformation>} coll) { super(coll); }
 * }
 * </code>
 * </PRE>
 *
 * <p/>
 * Actually, UPER decoder and encoder consider anything that extends {@code List<T>} as a SEQUENCE OF.
 *
 *
 * @param <T> type of elements contained.
 */
public abstract class Asn1SequenceOf<T> extends AbstractList<T> {
    private static final Logger logger = LoggerFactory.getLogger(Asn1SequenceOf.class);

    private final List<T> bakingList;

    @Override public T get(int index) { return bakingList.get(index); }
    @Override public int size() { return bakingList.size(); }

    public Asn1SequenceOf() { this(new ArrayList<T>()); }
    public Asn1SequenceOf(Collection<T> coll) {
        logger.trace("Instantinating Sequence Of {} with {}",
                ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0],
                coll);
        bakingList = new ArrayList<>(coll);
    }
}
