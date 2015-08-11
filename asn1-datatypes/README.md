# ASN.1 Datatypes

Java annotations to augment Java classes with information from [ASN.1](http://en.wikipedia.org/wiki/Abstract_Syntax_Notation_One) specifications. These annotations can later be used by encoders like [asn1-uper](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper).

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents:**

  - [Status](#status)
  - [Supported ASN.1 Features](#supported-asn1-features)
  - [Design choices](#design-choices)
  - [Examples](#examples)
    - [Example 1: Without restrictions or extension markers](#example-1-without-restrictions-or-extension-markers)
    - [Example 2: With Restrictions, no extension markers](#example-2-with-restrictions-no-extension-markers)
    - [Example 3: With restrictions and extension markers](#example-3-with-restrictions-and-extension-markers)
  - [Other ASN.1 tools](#other-asn1-tools)
  - [Acknowledgments](#acknowledgments)
  - [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->


## Status

Datatypes are enough to handle [camdenm](https://github.com/alexvoronov/geonetworking/tree/master/camdenm). There is no compiler yet, so Java classes and annotations have to be created and added manually.



## Supported ASN.1 Features
The following ASN.1 features are implemented:

ASN.1 | Java
----- | ----
`INTEGER` (unconstrained) | `BigInteger`
`INTEGER` (constrained)   | `short`, `int`, `long`, `BigInteger` (depending on constraint)
`BOOLEAN`                 | `boolean`
`ENUMERATED`              | `enum`
`SEQUENCE`                | class
`CHOICE`                  | class
`BIT STRING` (constrained to fixed length) | class
`BIT STRING` (non-fixed length)            | `List<Boolean>`
`OCTET STRING`            | `List<Byte>`
`IA5String`, `UTF8String`, `VisibleString` | `String`
`SEQUENCE OF T`           | `List<T>` (or `SequenceOfT extends Asn1SequenceOf<T>`)
`SET OF T`                | also `List<T>`


## Design choices


Design goal was to make the library as clean and simple as possible for the end user, even if it will not be very performant. Inspiration was taken from GSON, where they use reflection to extract fields from classes.

ASN.1 has three major ways to construct complex datatypes: `SEQUENCE`, `CHOICE` and `SEQUENCE OF`. They correspond to `struct `, `union` and arrays of C language. (ASN.1 `SET` and `SET OF` are pretty much identical to `SEQUENCE` and `SEQUENCE OF`, at least for PER, so they are not considered separately).

A `SEQUENCE` is encoded as an ordinary Java class. Java Reflection API `Class.getFields()` can return all fields of a class. The documentation says that there is no guarantee on the order in which the fields are returned, however, since Java version 1.6 the fields are returned in the declaration order.

A `CHOICE` is encoded as an ordinary Java class as well, with exactly one field in the class being non-null, corresponding to the element present.

A `SEQUENCE OF` is encoded as `List<T>`. Java type erasure in generics results in the type being lost at run-time if there is no object, so decoding will not work for generics (encoding is fine). So the class have to be non-generic and extend `List<T>`. GSON allows generic classes by using extra parameters. Scala also solved this issue, using Manifests and implicit parameters. For this library, just making a non-generic wrapper class with a concrete type for `List` is good enough. To make instantiation of abstract `List<T>` easier, there is `Asn1SequenceOF<T>` (it was not possible to extend, for example, `ArrayList<T>` directly). 

`OCTET STRING` is just a `SEQUENCE OF byte`. `BIT STRING` is a `SEQUENCE OF boolean`. `ENUMERATED` is an enum.

Integers in ASN.1 are unbounded by default, so `BigInteger` is used to represent them. If an integer is constrained, then `long`, `int` or `short` will be enough to represent it.

All strings, including `IA5String`, `UTF8String` and  `VisibleString` are represented by Java `String`, the difference is only in annotations.

`OPTIONAL` is implemented as a nullable field with an annotation. Using built-in Java 8 `Optional<T>` was not possible due to type erasure, and creating non-generic wrapper classes every time `OPTIONAL` is used is too much of a burden, so a simple nullable field is used. The name for annotation is `@Asn1Optional`, to not clash with built-in `Optional`.

`DEFAULT` is treated in ASN.1 almost exactly as `OPTIONAL`, so the annotation from `OPTIONAL` is used for `DEFAULT` as well. The only difference is that `DEFAULT` have a static initializer in the Java class.

ASN.1 restrictions are implemented as Java Annotations. Integers have `@IntRange`, sequences have `@SizeRange` and `@FixedSize`, strings have alphabets etc.

ASN.1 Extensions are marked by annotations too. A sequence that has an extension marker have annotation `@HasExtensionMarker`, and all elements that come after that marker in ASN.1 are marked with `@IsExtension` in the Java class. `@IntRange()` and `@SizeRange()` also support an optional argument `hasExtension=true`, which is set to `false` by default.




## Examples



Here's how three examples from the Annex A of the UPER standard (pages 44-53 of [ITU X.691 11/2008](http://www.itu.int/rec/dologin_pub.asp?lang=e&id=T-REC-X.691-200811-I!!PDF-E&type=items)) would be encoded in Java.

### Example 1: Without restrictions or extension markers

See this code in action as a part of [asn1-uper](https://github.com/alexvoronov/geonetworking/tree/master/asn1-uper) test suite in [UperEncoderExample1BasicTest.java ](https://github.com/alexvoronov/geonetworking/blob/master/asn1-uper/src/test/java/net/gcdc/asn1/uper/UperEncoderExample1BasicTest.java).

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
  @Asn1Optional SequenceOfChildInformation sequenceOfChildInformation = new SequenceOfChildInformation();

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

public static class SequenceOfChildInformation extends Asn1SequenceOf<ChildInformation> {
  public SequenceOfChildInformation() { super(); }
  public SequenceOfChildInformation(Collection<ChildInformation> coll) { super(coll); }
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
  new SequenceOfChildInformation(Arrays.asList(
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



### Example 2: With Restrictions, no extension markers

See code in [UperEncoderExample2RestrictionTest.java](https://github.com/alexvoronov/geonetworking/blob/master/asn1-uper/src/test/java/net/gcdc/asn1/uper/UperEncoderExample2RestrictionTest.java)

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
  @Asn1Optional SequenceOfChildInformation sequenceOfChildInformation = new SequenceOfChildInformation();

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

public static class SequenceOfChildInformation extends Asn1SequenceOf<ChildInformation> {
  public SequenceOfChildInformation() { super(); }
  public SequenceOfChildInformation(Collection<ChildInformation> coll) { super(coll); }
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
      new Date("19590717")
    )
  ))
);
```

### Example 3: With restrictions and extension markers

See code in [UperEncoderExample3ExtensionTest.java](https://github.com/alexvoronov/geonetworking/blob/master/asn1-uper/src/test/java/net/gcdc/asn1/uper/UperEncoderExample3ExtensionTest.java).

ASN.1 schema:

```asn1
PersonnelRecord ::= [APPLICATION 0] IMPLICIT SET {
name Name,
title [0] VisibleString, 
number EmployeeNumber, 
dateOfHire [1] Date, 
nameOfSpouse [2] Name,
children [3] IMPLICIT
SEQUENCE (SIZE(2, ...)) OF ChildInformation OPTIONAL,
... }

ChildInformation ::= SET { 
name Name, 
dateOfBirth [0] Date,
...,
sex [1] IMPLICIT ENUMERATED {male(1), female(2),
unknown(3)} OPTIONAL 
}

Name ::= [APPLICATION 1] IMPLICIT SEQUENCE { 
  givenName NameString,
  initial NameString (SIZE(1)),
  familyName NameString,
  ... 
}

EmployeeNumber ::= [APPLICATION 2] IMPLICIT INTEGER (0..9999, ...)

Date ::= [APPLICATION 3] IMPLICIT VisibleString
(FROM("0".."9") ^ SIZE(8, ..., 9..20)) -- YYYYMMDD 

NameString ::= VisibleString
(FROM("a".."z" | "A".."Z" | "-.") ^ SIZE(1..64, ...))
```

Corresponding Java code:

```java
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
```

And example instantiation code:

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
```

## Other ASN.1 tools
ITU-T have a [list of ASN.1 tools](http://www.itu.int/en/ITU-T/asn1/Pages/Tools.aspx). IvmaiAsn project also have [its own list](http://ivmaiasn.sourceforge.net/asn1lnk.html).

Here is my (incomplete) list of the open-source tools:

Name | License | Runtime | Compiler | BER, DER? | UPER?
---- | ------- | ------- | -------- | --- | ---
[asn1c](https://github.com/vlm/asn1c) | BSD 2-clause | C | C | ✓ | ✓
[asn1scc](https://github.com/ttsiodras/asn1scc) | Dual: LGPL, commercial | C, Ada | F#, Antlr/Java | ✓ | ✓
[snacc](https://github.com/nevali/snacc) | GPL | C | C, C++ | ✓
[III ASN.1](http://iiiasn1.sourceforge.net/main.html) | Mozilla | C++ | C++ | ✓ | ✓
[libtasn1](http://www.gnu.org/software/libtasn1/) | LGPL | ANSI C99 | C | ✓ (DER) | 
[pyasn1](http://pyasn1.sourceforge.net/) | BSD 2-clause | Python |  [asn1ate](https://github.com/kimgr/asn1ate) (Python) | ✓ | 
[dpkt](https://github.com/kbandla/dpkt/blob/master/dpkt/asn1.py) | BSD 3-Clause | Python | . | ✓ | 
[ASN1js](https://github.com/GlobalSign/ASN1.js) | BSD 3-clause | JavaScript | . | ✓
[asn1js](https://github.com/lapo-luchini/asn1js) | MIT | JavaScript | . | ✓
[node-asn1](https://github.com/mcavage/node-asn1) | MIT | JavaScript | . | ✓ (BER) |
[ASN1.js](https://github.com/indutny/asn1.js) | MIT | JavaScript | . | ✓ (DER) | 
[ASN1s](https://github.com/lastrix/ASN1S) | GPL | Java | Java/Antlr | ✓ (BER) |
[jASN1](https://github.com/juherr/jASN1) | LGPL | Java | Java | ✓ (BER) |
[openASN.1](http://sourceforge.net/projects/openasn1/) | LGPL | Java | Java | ✓ | ✓
[asn1forj](http://sourceforge.net/projects/asn1forj) | GPL | Java | ? | ✓ |
[JAC](http://sourceforge.net/projects/jac-asn1) | GPL | Java | ? | ✓ (BER, CER, DER) | 
[JASN](http://sourceforge.net/projects/jasn) |  GPL | Java | ? | ✓ (BER, DER) | 
[Binary Notes](http://sourceforge.net/projects/bnotes) | Apache | Java, .NET | XSLT | ✓ | ✓
[arc](http://www.forge.com.au/Research/products/arc/arc.htm) | BSD 4-clause | Java | javacc/Java | 
[Cryptix](http://cryptix-asn1.sourceforge.net/) | BSD 2-clause | Java | SableCC | 
[Legion of The Bouncy Castle](https://www.bouncycastle.org/) | MIT, MIT X11 |  Java, C# | . | ✓ (DER, BER) | 
[Apache Harmony](https://harmony.apache.org/subcomponents/classlibrary/asn1_framework.html) | Apache | Java | . | 

## Acknowledgments
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).

Use of annotations and reflection was inspired by [Gson](https://code.google.com/p/google-gson/).

## License

This code is released under the business-friendly Apache 2.0 license.
