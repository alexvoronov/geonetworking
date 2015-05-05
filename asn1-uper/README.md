# ASN.1 UPER Encoder and Decoder

ASN.1 Encoder and Decoder for Unaligned Packed Encoding Rules (UPER, ITU-T Recommendation [X.691](http://www.itu.int/rec/T-REC-X.691/en) | ISO/IEC 8825-2). Encoder encodes objects of classes annotated with [asn1-datatypes](https://github.com/alexvoronov/geonetworking/tree/master/asn1-datatypes).


### Status
Both Encoder and Decoder can handle [camdenm](https://github.com/alexvoronov/geonetworking/tree/master/camdenm). Decoder can have some problems with unexpected input (values outside of extension root), but works fine for proper input.


### Acknowledgments
This implementation was partly developed within [i-GAME](http://gcdc.net/i-game) project that has received funding from the European Union's Seventh Framework Programme for research, technological development and demonstration under grant agreement no [612035](http://cordis.europa.eu/project/rcn/110506_en.html).

Use of reflection was inspired by [Gson](https://code.google.com/p/google-gson/).

Excellent OSS Nokalva [ASN.1 Playground](http://asn1-playground.oss.com/) was used to catch a few errors in the encoder results.


### License
This code is released under the business-friendly Apache 2.0 license.
