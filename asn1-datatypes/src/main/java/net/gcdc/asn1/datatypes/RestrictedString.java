package net.gcdc.asn1.datatypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestrictedString {
    CharacterRestriction value();
    Class<? extends Alphabet> alphabet() default DefaultAlphabet.class;
}
