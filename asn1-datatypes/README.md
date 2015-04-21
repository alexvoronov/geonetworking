# ASN.1 Datatypes

Java annotations to augment Java classes with information from [ASN.1](http://en.wikipedia.org/wiki/Abstract_Syntax_Notation_One) specifications. These annotations can later be used by encoders like [asn1-uper](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper).

### Status

Datatypes are enough to handle [camdenm](https://github.com/alexvoronov/geonetworking/tree/master/camdenm). There is no compiler yet, so Java classes and annotations have to be created and added manually.

### Examples



Here's how two examples from the Appendix of the UPER standard would be encoded in Java.

#### Example 1: Without restrictions or extension markers

ASN.1:

```asn1
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
```


Corresponding Java code:

```java
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
```

And the use of those classes:

```java
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
```



#### Example 2: With Restrictions, no extension markers

ASN.1:

```asn1
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
```

Java classes:

```java
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

@RestrictedString(value = CharacterRestriction.VisibleString, alphabet = NameStringAlphabet.class)
@SizeRange(minValue = 1, maxValue = 64)
public static class NameString extends Asn1String {
    public NameString() { this(""); }
    public NameString(String value) { super(value); }

    public static class NameStringAlphabet implements Alphabet {
        private final static String chars = new AlphabetBuilder().withRange('a', 'z').withRange('A','Z').withChars("-.").chars();
        @Override public String chars() { return chars; }
    }
}

@Sequence
public static class EmployeeNumber extends Asn1BigInteger {
    public EmployeeNumber() { this(0); }
    public EmployeeNumber(long value) { this(BigInteger.valueOf(value)); }
    public EmployeeNumber(BigInteger value) { super(value); }
}

@RestrictedString(value = CharacterRestriction.VisibleString, alphabet = DateAlphabet.class)
@FixedSize(8)
public static class Date extends Asn1String {
    public Date() { this(""); }
    public Date(String value) { super(value); }

    public static class DateAlphabet implements Alphabet {
        private final static String chars = new AlphabetBuilder().withRange('0', '9').chars();
        @Override public String chars() { return chars; }
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
```

And example object instantiation:

```java

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
  new Asn1SequenceOf<ChildInformation>(Arrays.asList(
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

```


### Other ASN.1 tools
ITU-T have a [list of ASN.1 tools](http://www.itu.int/en/ITU-T/asn1/Pages/Tools.aspx).

### Acknowledgments
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).

Use of annotations and reflection was inspired by [Gson](https://code.google.com/p/google-gson/).

### License

This code is released under the business-friendly Apache 2.0 license.
