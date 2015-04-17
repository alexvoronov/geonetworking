package net.gcdc.asn1.datatypes;

import java.util.NoSuchElementException;
import java.util.Objects;

/** Represents optional values.
 *
 * Should be replaced by java.util.Optional from Java 8, when project moves to Java 8.
 *
 * @param <T> type of contained elements */
public class Optional<T> {

    private final T       element;
    private final boolean isPresent;

    private Optional(T element, boolean isPresent) {
        this.element = element;
        this.isPresent = isPresent;
    }

    /** @return true if the Option contains a value */
    public boolean isPresent() {
        return isPresent;
    }

    /** @return the element if the option is not empty
     * @throws java.util.NoSuchElementException if the option is empty */
    public T get() {
        if (isPresent) {
            return element;
        } else {
            throw new NoSuchElementException("None.get");
        }
    }

    /** @return the value, if present, otherwise return {@code other}
     * @param other the value to be returned if there is no value present */
    public T orElse(T other) {
        return isPresent() ? get() : other;
    }

    /**
     * Indicates whether some other object is "equal to" this Optional. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Optional} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(element, other.element);
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(element);
    }

    /** Returns an Option containing the value.
     *
     * @param <A> the type of the value
     * @param element contained value
     * @return a new Option that contains the value */
    public static <A> Optional<A> of(final A element) {
        return new Optional<A>(element, true);
    }

    /** Returns an empty option.
     *
     * @param <A>
     * @return an empty Option */
    public static <A> Optional<A> empty() {
        return new Optional<A>(null, false);
    }
}
