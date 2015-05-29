package net.gcdc.asn1.uper;

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
