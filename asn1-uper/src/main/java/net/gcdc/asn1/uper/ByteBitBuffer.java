package net.gcdc.asn1.uper;

import java.io.DataOutputStream;


public class ByteBitBuffer implements BitBuffer {

    byte[] bytes;
    byte[] mask = new byte[] {
     (byte) 0b1000_0000,
            0b0100_0000,
            0b0010_0000,
            0b0001_0000,
            0b0000_1000,
            0b0000_0100,
            0b0000_0010,
            0b0000_0001,
    };

    boolean isFinite;

    int mark;
    int position;
    int limit;

    @Override public boolean get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException("Index " + index + " outside of 0.." + limit);
        }
        boolean result = (bytes[index / 8] & mask[index % 8]) != 0;
        return result;
    }

    @Override public boolean get() {
        boolean result = get(position);
        position++;
        return result;
    }

    private void grow() {
        byte[] newbytes = new byte[2 * bytes.length];
        System.arraycopy(bytes, 0, newbytes, 0, bytes.length);
        bytes = newbytes;
    }

    @Override public BitBuffer put(int index, boolean element) {
        if (bytes.length <= index / 8) {
            if (isFinite) { throw new IndexOutOfBoundsException(); }
            else { grow(); }
        }
        if (element) {
            bytes[index / 8] |= mask[index % 8];
        } else {
            bytes[index / 8] &= ~mask[index % 8];
        }
        return this;
    }

    @Override public BitBuffer put(boolean element) {
        put(position, element);
        position++;
        limit = limit < position ? position : limit;  // TODO: should it be here?
        return this;
    }

    @Override public BitBuffer putByte(byte element) {
        for (int i = 0; i < 8; i++) {
            put((element & mask[i]) != 0);
        }
        return this;
    }

    @Override public byte getByte() {
        byte result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (get() ? 1 : 0) << (7 - i);
        }
        return result;
    }

    @Override public int limit() {
        return limit;
    }

    @Override public String toBooleanString(int startIndex, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = startIndex; i < startIndex + length; i++) {
            sb.append(get(i) ? "1" : "0");
        }
        return sb.toString();
    }

    @Override public int capacity() {
        return isFinite ? bytes.length * 8 : Integer.MAX_VALUE;
    }

    @Override public int position() {
        return position;
    }

    @Override public int remaining() {
        return limit - position;
    }

    private ByteBitBuffer(byte[] backingArray) {
        this.bytes = backingArray;
        this.isFinite = true;
    }

    private ByteBitBuffer(int initialCapacity) {
        this.bytes = new byte[initialCapacity];
        this.isFinite = false;
    }

    public static ByteBitBuffer allocate(int length) {
        return new ByteBitBuffer(new byte[(length + 7) / 8]);
    }

    public static ByteBitBuffer createInfinite() {
        return new ByteBitBuffer(64);
    }

    @Override public BitBuffer flip() {
        limit = position;
        DataOutputStream d = null;
        position = 0;
        return this;
    }

    @Override public String toBooleanStringFromPosition(int startIndex) {
        return toBooleanString(startIndex, position-startIndex);
    }

    @Override public byte[] array() {
        return bytes;
    }

}
