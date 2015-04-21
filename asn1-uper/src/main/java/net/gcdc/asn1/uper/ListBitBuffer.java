package net.gcdc.asn1.uper;

import java.util.ArrayDeque;
import java.util.Deque;

public class ListBitBuffer implements BitBuffer {

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
        return deque.size();
    }
    public int size() {
        return deque.size();
    }

    private ListBitBuffer() {}

    public static ListBitBuffer empty() {
        return new ListBitBuffer();
    }


}
