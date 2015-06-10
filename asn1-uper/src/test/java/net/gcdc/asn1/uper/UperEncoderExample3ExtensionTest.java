package net.gcdc.asn1.uper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import net.gcdc.asn1.datatypes.Alphabet;
import net.gcdc.asn1.datatypes.AlphabetBuilder;
import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.HasExtensionMarker;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.IsExtension;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example from the Standard on UPER.
 <pre>
PersonnelRecord ::= [APPLICATION 0] IMPLICIT SET {
name Name,
title [0] VisibleString,
number EmployeeNumber,
dateOfHire [1] Date,
nameOfSpouse [2] Name,
children [3] IMPLICIT SEQUENCE (SIZE(2, ...)) OF ChildInformation OPTIONAL,
... }

ChildInformation ::= SET {
name Name,
dateOfBirth [0] Date,
...,
sex [1] IMPLICIT ENUMERATED {male(1), female(2), unknown(3)} OPTIONAL
}

Name ::= [APPLICATION 1] IMPLICIT SEQUENCE
{ givenName NameString,
initial NameString (SIZE(1)),
familyName NameString,
... }

EmployeeNumber ::= [APPLICATION 2] IMPLICIT INTEGER (0..9999, ...)

Date ::= [APPLICATION 3] IMPLICIT VisibleString (FROM("0".."9") ^ SIZE(8, ..., 9..20)) -- YYYYMMDD

NameString ::= VisibleString (FROM("a".."z" | "A".."Z" | "-.") ^ SIZE(1..64, ...))
 </pre>
 */
public class UperEncoderExample3ExtensionTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    @Sequence
    @HasExtensionMarker
    public static class PersonenelRecord {
        Name name;
        EmployeeNumber number;
        @RestrictedString(CharacterRestriction.VisibleString)
        String title;
        Date dateOfHire;
        Name nameOfSpouse;
        @Asn1Optional SequenceOfChildInformation sequenceOfChildInformation;

        public PersonenelRecord() {
            this(new Name(), new EmployeeNumber(), "", new Date(), new Name(), new SequenceOfChildInformation());
        }

        public PersonenelRecord(
                Name name,
                EmployeeNumber number,
                String title,
                Date dateOfHire,
                Name nameOfSpouse,
                SequenceOfChildInformation sequenceOfChildInformation
                ) {
            this.name = name;
            this.number = number;
            this.title = title;
            this.dateOfHire = dateOfHire;
            this.nameOfSpouse = nameOfSpouse;
            this.sequenceOfChildInformation = sequenceOfChildInformation;
        }
    }

    @Sequence
    @HasExtensionMarker
    public static class Name {
        NameString givenName;
        @FixedSize(1)
        NameString initial;
        NameString familyName;

        public Name() { this(new NameString(), new NameString(), new NameString()); }
        public Name(NameString givenName, NameString initial, NameString familyName) {
            this.givenName = givenName;
            this.initial = initial;
            this.familyName = familyName;
        }
    }

    //"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-."
    @RestrictedString(value = CharacterRestriction.VisibleString, alphabet = NameString.NameStringAlphabet.class)
    @SizeRange(minValue = 1, maxValue = 64, hasExtensionMarker = true)
    public static class NameString extends Asn1String {
        public NameString() { this(""); }
        public NameString(String value) { super(value); }

        public static class NameStringAlphabet extends Alphabet {
            private final static String chars =
                    new AlphabetBuilder().withRange('a', 'z').withRange('A','Z').withChars("-.").chars();
            public NameStringAlphabet() {
                super(chars);
            }
        }
    }

    @IntRange(minValue = 0, maxValue = 9999, hasExtensionMarker = true)
    public static class EmployeeNumber extends Asn1Integer {
        public EmployeeNumber() { this(0); }
        public EmployeeNumber(long value) { super(value); }
    }

    @RestrictedString(value = CharacterRestriction.VisibleString, alphabet = Date.DateAlphabet.class)
    @SizeRange(minValue = 8, maxValue = 8, hasExtensionMarker = true)
    public static class Date extends Asn1String {
        public Date() { this(""); }
        public Date(String value) { super(value); }
        public static class DateAlphabet extends Alphabet {
            private final static String chars = new AlphabetBuilder().withRange('0', '9').chars();
            public DateAlphabet() {
                super(chars);
            }
        }
    }

    @Sequence
    @HasExtensionMarker
    public static class ChildInformation {
        Name name;
        Date dateOfBirth;

        @IsExtension
        @Asn1Optional
        Sex sex;

        public ChildInformation() { this(new Name(), new Date()); }
        public ChildInformation(Name name, Date dateOfBirth) {
            this(name, dateOfBirth, null);
        }
        public ChildInformation(Name name, Date dateOfBirth, Sex sex) {
            this.name = name;
            this.dateOfBirth = dateOfBirth;
            this.sex = sex;
        }
    }

    public static enum Sex {
        male(1),
        female(2),
        unknown(3);

        private final int value;
        public int value() { return value; }
        private Sex(int value) { this.value = value; }
    }

    @SizeRange(minValue=2, maxValue=2, hasExtensionMarker=true)
    public static class SequenceOfChildInformation extends Asn1SequenceOf<ChildInformation> {
        public SequenceOfChildInformation() { super(); }
        public SequenceOfChildInformation(Collection<ChildInformation> coll) { super(coll); }
    }




    @Test public void test() throws IllegalArgumentException, IllegalAccessException {

        PersonenelRecord record = new PersonenelRecord(
          new Name(
            new NameString("John"),
            new NameString("P"),
            new NameString("Smith")
          ),
          new EmployeeNumber(51),
          "Director",
          new Date("19710917"),
          new Name(
            new NameString("Mary"),
            new NameString("T"),
            new NameString("Smith")
          ),
          new SequenceOfChildInformation(Arrays.asList(
            new ChildInformation(
              new Name(
                new NameString("Ralph"),
                new NameString("T"),
                new NameString("Smith")
              ),
              new Date("19571111")
            ),
            new ChildInformation(
              new Name(
                new NameString("Susan"),
                new NameString("B"),
                new NameString("Jones")
              ),
              new Date("19590717"),
              Sex.female
            )
          ))
        );


        byte[] encoded = UperEncoder.encode(record);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("40CBAA3A5108A5125F180330889A7965C7D37F20CB8848B819CE5BA2A114A24BE30113727AE3542294497C619571111822985CE521842EAA60B832B20E2E020280",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, PersonenelRecord.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("reencoded hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

}
