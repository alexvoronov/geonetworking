package net.gcdc.asn1.uper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import net.gcdc.asn1.datatypes.Alphabet;
import net.gcdc.asn1.datatypes.AlphabetBuilder;
import net.gcdc.asn1.datatypes.Asn1BigInteger;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UperEncoderExample2RestrictionTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    /**
     * Example from the Standard on UPER.
     <pre>
PersonnelRecord ::= [APPLICATION 0] IMPLICIT SET {
  name Name,
  title [0] VisibleString,
  number EmployeeNumber,
  dateOfHire [1] Date,
  nameOfSpouse [2] Name,
  children [3] IMPLICIT
    SEQUENCE OF ChildInformation DEFAULT {} }

ChildInformation ::= SET {
  name Name,
  dateOfBirth [0] Date}

Name ::= [APPLICATION 1] IMPLICIT SEQUENCE {
  givenName NameString,
  initial NameString (SIZE(1)),
  familyName NameString }

EmployeeNumber ::= [APPLICATION 2] IMPLICIT INTEGER

Date ::= [APPLICATION 3] IMPLICIT VisibleString (FROM("0".."9") ^ SIZE(8)) -- YYYYMMDD

NameString ::= VisibleString (FROM("a".."z" | "A".."Z" | "-.") ^ SIZE(1..64))
     </pre>
     */
    @Sequence
    public static class PersonenelRecord {
        Name name;
        EmployeeNumber number;
        @RestrictedString(CharacterRestriction.VisibleString)
        String title;
        Date dateOfHire;
        Name nameOfSpouse;
        @Asn1Optional Children children = new Children();

        public PersonenelRecord() {
            this(new Name(), new EmployeeNumber(), "", new Date(), new Name(), new Children());
        }

        public PersonenelRecord(
                Name name,
                EmployeeNumber number,
                String title,
                Date dateOfHire,
                Name nameOfSpouse,
                Children children
                ) {
            this.name = name;
            this.number = number;
            this.title = title;
            this.dateOfHire = dateOfHire;
            this.nameOfSpouse = nameOfSpouse;
            this.children = children;
        }
    }

    @Sequence
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
    @SizeRange(minValue = 1, maxValue = 64)
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

    public static class EmployeeNumber extends Asn1BigInteger {
        public EmployeeNumber() { this(0); }
        public EmployeeNumber(long value) { this(BigInteger.valueOf(value)); }
        public EmployeeNumber(BigInteger value) { super(value); }
    }

    @RestrictedString(value = CharacterRestriction.VisibleString, alphabet = Date.DateAlphabet.class)
    @FixedSize(8)
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
    public static class ChildInformation {
        Name name;
        Date dateOfBirth;

        public ChildInformation() { this(new Name(), new Date()); }
        public ChildInformation(Name name, Date dateOfBirth) {
            this.name = name;
            this.dateOfBirth = dateOfBirth;
        }
    }

    public static class Children extends Asn1SequenceOf<ChildInformation> {
        public Children() { super(); }
        public Children(Collection<ChildInformation> coll) { super(coll); }
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
          new Children(Arrays.asList(
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
              new Date("19590717")
            )
          ))
        );


        byte[] encoded = UperEncoder.encode(record);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("865D51D2888A5125F180998444D3CB2E3E9BF90CB8848B867396E8A88A5125F181089B93D71AA2294497C632AE222222985CE521885D54C170CAC838B8",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, PersonenelRecord.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("reencoded hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

}
