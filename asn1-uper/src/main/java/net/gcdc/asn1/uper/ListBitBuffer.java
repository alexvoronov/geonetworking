package net.gcdc.asn1.uper;

import java.util.ArrayDeque;
import java.util.Deque;

public final class ListBitBuffer implements BitBuffer {

    @Override public String toString() {
        return UperEncoder.binaryStringFromCollection(deque);
    }

    private final Deque<Boolean> deque = new ArrayDeque<>();

    @Override public boolean get() {
        return deque.removeFirst();
    }

    @Override public BitBuffer put(boolean element) {
        deque.addLast(element);
        return this;
    }

    @Override public int limit() {
        throw new UnsupportedOperationException("limit for listbuffer");
    }
    public int size() {
        return deque.size();
    }

    private ListBitBuffer() {}

    public static ListBitBuffer empty() {
        return new ListBitBuffer();
    }

    @Override public boolean get(int index) {
        throw new UnsupportedOperationException("absolute get for listbuffer");
    }

    @Override public BitBuffer put(int index, boolean element) {
        throw new UnsupportedOperationException("absolute put for listbuffer");
    }

    @Override public String toBooleanString(int startIndex, int length) {
        throw new UnsupportedOperationException("to string for listbuffer");
    }

    @Override public int capacity() {
        return Integer.MAX_VALUE;
    }

    @Override public int position() {
        throw new UnsupportedOperationException("position for listbuffer");
    }

    @Override public int remaining() {
        return deque.size();
    }

    @Override public BitBuffer flip() {
        throw new UnsupportedOperationException("flip for listbuffer");
    }

    @Override public String toBooleanStringFromPosition(int startIndex) {
        throw new UnsupportedOperationException("to string for listbuffer");
    }

    @Override public byte[] array() {
        throw new UnsupportedOperationException("to byte array for listbuffer");
    }

    @Override public BitBuffer putByte(byte element) {
        throw new UnsupportedOperationException("put byte for listbuffer");

    }

    @Override public byte getByte() {
        throw new UnsupportedOperationException("get byte for listbuffer");
    }


}
