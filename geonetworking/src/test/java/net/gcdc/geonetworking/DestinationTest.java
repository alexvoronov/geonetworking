package net.gcdc.geonetworking;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class DestinationTest {

    @Test
    public void test3() {
        EqualsVerifier.forClass(Destination.Geobroadcast.class).verify();
    }

    @Test
    public void test4() {
        EqualsVerifier.forClass(Destination.Beacon.class).verify();
    }

    @Test
    public void test5() {
        EqualsVerifier.forClass(Destination.SingleHop.class).verify();
    }
}
