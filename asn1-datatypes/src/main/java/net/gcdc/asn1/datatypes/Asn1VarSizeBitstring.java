package net.gcdc.asn1.datatypes;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Asn1VarSizeBitstring booleen = (Asn1VarSizeBitstring) o;
        return Objects.equals(backing, booleen.backing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), backing);
    }
}
