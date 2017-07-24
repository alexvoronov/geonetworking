package net.gcdc.asn1.uper;

/**
 * An interface for convenient storage of bits, similar to Java's ByteBuffer.
 *
 * This interface and its implementation are very useful for UPER, since UPER operates on bits
 * regardless of byte boundaries.
 *
 */
public interface BitBuffer {
    boolean get();
    boolean get(int index);
    BitBuffer put(boolean element);
    BitBuffer put(int index, boolean element);
    int limit();
    int capacity();
    int position();
    int remaining();
    BitBuffer flip();
    String toBooleanString(int startIndex, int length);
    String toBooleanStringFromPosition(int startIndex);
    byte[] array();
    BitBuffer putByte(byte element);
    byte getByte();
}
