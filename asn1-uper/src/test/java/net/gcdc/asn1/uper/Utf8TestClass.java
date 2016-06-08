package net.gcdc.asn1.uper;

import net.gcdc.asn1.datatypes.Asn1AnonymousType;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;

/**
 * A class for testing UTF8String.
 <pre>
 TestSeq ::= SEQUENCE {
 companyName     UTF8String (SIZE (1..200))}
 </pre>
 */
@Sequence
public class Utf8TestClass {
    CompanyName companyName;

    @Asn1AnonymousType
    @SizeRange(minValue=1, maxValue=200)
    @RestrictedString(CharacterRestriction.UTF8STRING)
    public static class CompanyName extends Asn1String {
        public CompanyName() { this(""); }
        public CompanyName(String value) { super(value); }
    }

    public Utf8TestClass() { this(new CompanyName()); }
    public Utf8TestClass(CompanyName companyName) {
        this.companyName = companyName;
    }
}