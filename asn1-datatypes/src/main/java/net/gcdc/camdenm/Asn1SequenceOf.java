package net.gcdc.camdenm;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Asn1SequenceOf<T> extends AbstractList<T> {

    private final List<T> bakingList;

    @Override public T get(int index) {
        return bakingList.get(index);
    }

    @Override public int size() {
        return bakingList.size();
    }

    public Asn1SequenceOf() {
        bakingList = new ArrayList<>();
    }

    public Asn1SequenceOf(Collection<T> coll) {
        bakingList = new ArrayList<>(coll);
    }
}
