package net.gcdc.asn1.datatypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
/**
 * This annotation indicates that the class is not present in the original ASN.1 declaration.
 * This happens when SEQUENCE members have restrictions (ranges, alphabets etc).
 *
 * This annotation plays no role in the UPER encoding.
 *
 */
@Target({ElementType.TYPE})
public @interface Asn1AnonymousType {

}
