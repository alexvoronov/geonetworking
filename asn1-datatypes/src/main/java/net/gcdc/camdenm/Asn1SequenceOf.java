package net.gcdc.camdenm;

import java.lang.reflect.ParameterizedType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Asn1SequenceOf<T> extends AbstractList<T> {
    private final static Logger logger = LoggerFactory.getLogger(Asn1SequenceOf.class);

    private final List<T> bakingList;

    @Override public T get(int index) { return bakingList.get(index); }
    @Override public int size() { return bakingList.size(); }

    public Asn1SequenceOf() { this(new ArrayList<T>()); }
    public Asn1SequenceOf(Collection<T> coll) {
        logger.trace("Instantinating Sequence Of {} with {}",
                ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0],
                coll);
        bakingList = new ArrayList<>(coll);
    }
}
