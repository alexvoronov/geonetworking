package net.gcdc.asn1.datatypes;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.Collection;

public class Asn1VarSizeBitstring extends AbstractList<Boolean> {

    private final BitSet backing;

    @Override public Boolean get(int index) {
        return backing.get(index);
    }

    @Override public int size() {
        return backing.length();
    }

    public Asn1VarSizeBitstring(Collection<Boolean> coll) {
        backing = new BitSet();
        int bitIndex = 0;
        for (Boolean b : coll) {
            backing.set(bitIndex, b);
            bitIndex++;
        }
    }

    public Asn1VarSizeBitstring(BitSet bitset) {
        backing = (BitSet) bitset.clone();
    }

    protected void setBit(int bitIndex, boolean value) {
        backing.set(bitIndex, value);
    }

    public boolean getBit(int bitIndex) {
        return backing.get(bitIndex);
    }

}
