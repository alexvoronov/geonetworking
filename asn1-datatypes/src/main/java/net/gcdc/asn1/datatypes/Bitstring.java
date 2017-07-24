package net.gcdc.asn1.datatypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation is used for bitstrings.
 * In UPER, a SEQUENCE OF Booleans would look exactly as bitstring, so this annotation can be
 * omitted for {@code List<Boolean>}.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bitstring {

}
