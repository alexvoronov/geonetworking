package net.gcdc.geonetworking;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class GeonetDataTest {

    @Test public void test() {
        EqualsVerifier.forClass(GeonetData.class).verify();
    }

}
