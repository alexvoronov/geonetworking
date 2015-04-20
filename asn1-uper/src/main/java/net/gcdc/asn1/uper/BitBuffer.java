package net.gcdc.asn1.uper;

public interface BitBuffer {
    public boolean get();
    public BitBuffer put(boolean element);
    public int limit();
}
