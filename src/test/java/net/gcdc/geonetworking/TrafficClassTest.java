package net.gcdc.geonetworking;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class TrafficClassTest {

    @Test public void test() {
        EqualsVerifier.forClass(TrafficClass.class).verify();
        // TODO: add test for store-carry-forward flag.
    }

}
