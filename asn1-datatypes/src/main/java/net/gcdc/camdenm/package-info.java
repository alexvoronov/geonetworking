/** Annotations to create Java classes that correspond to ASN.1 specifications.
 *
 * Some annotations (e.g. {@link SizeRange}, {@link FixedSize}, {@link IntRange},
 * {@link RestrictedString}) are Type-only annotations and sometime require creating extra classes,
 * they can be extended to work as Field annotations too, but this will require modifications to the
 * Encoder. */
package net.gcdc.camdenm;
