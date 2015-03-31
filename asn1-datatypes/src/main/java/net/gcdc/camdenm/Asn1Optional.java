package net.gcdc.camdenm;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the field is OPTIONAL in ASN.1. Implemented as null. Equivalent to @Nullable.
 *
 * Using Optional<T> would require Manifests to capture generics (like in Gson).
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Asn1Optional {

}
