package net.gcdc.asn1.uper;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Arrays;

import net.gcdc.asn1.datatypes.Asn1BigInteger;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UperEncoderExampleTest {
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
  givenName VisibleString,
  initial VisibleString,
  familyName VisibleString }

EmployeeNumber ::= [APPLICATION 2] IMPLICIT INTEGER

Date ::= [APPLICATION 3] IMPLICIT VisibleString -- YYYYMMDD
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
        @Asn1Optional Asn1SequenceOf<ChildInformation> children = new Asn1SequenceOf<ChildInformation>();

        public PersonenelRecord() {
            this(new Name(), new EmployeeNumber(), "", new Date(), new Name(), new Asn1SequenceOf<ChildInformation>());
        }

        public PersonenelRecord(
                Name name,
                EmployeeNumber number,
                String title,
                Date dateOfHire,
                Name nameOfSpouse,
                Asn1SequenceOf<ChildInformation> children
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
        @RestrictedString(CharacterRestriction.VisibleString)
        String givenName;
        @RestrictedString(CharacterRestriction.VisibleString)
        String initial;
        @RestrictedString(CharacterRestriction.VisibleString)
        String familyName;

        public Name() { this("", "", ""); }
        public Name(String givenName, String initial, String familyName) {
            this.givenName = givenName;
            this.initial = initial;
            this.familyName = familyName;
        }
    }

    @Sequence
    public static class EmployeeNumber extends Asn1BigInteger {
        public EmployeeNumber() { this(0); }
        public EmployeeNumber(long value) { this(BigInteger.valueOf(value)); }
        public EmployeeNumber(BigInteger value) { super(value); }
    }

    @RestrictedString(CharacterRestriction.VisibleString)
    public static class Date extends Asn1String {
        public Date() { this(""); }
        public Date(String value) { super(value); }
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



    @Test public void test() throws IllegalArgumentException, IllegalAccessException {

        PersonenelRecord record = new PersonenelRecord(
          new Name(
            "John",
            "P",
            "Smith"
          ),
          new EmployeeNumber(51),
          "Director",
          new Date("19710917"),
          new Name(
            "Mary",
            "T",
            "Smith"),
          new Asn1SequenceOf<ChildInformation>(Arrays.asList(
            new ChildInformation(
              new Name(
                "Ralph",
                "T",
                "Smith"
              ),
              new Date("19571111")
            ),
            new ChildInformation(
              new Name(
                "Susan",
                "B",
                "Jones"
              ),
              new Date("19590717")
            )
          ))
        );


        byte[] encoded = UperEncoder.encode(record);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("824ADFA3700D005A7B74F4D0026611134F2CB8FA6FE410C5CB762C1CB16E09370F2F20350169EDD3D340102D2C3B386801A80B4F6E9E9A0218B96ADD8B162C4169F5E787700C20595BF765E610C5CB572C1BB16E",
                UperEncoder.hexStringFromBytes(encoded));
    }

}
