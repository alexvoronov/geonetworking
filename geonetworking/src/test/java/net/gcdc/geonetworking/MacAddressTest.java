package net.gcdc.geonetworking;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class MacAddressTest {

    @Test public void test() {
        EqualsVerifier.forClass(MacAddress.class).verify();
        // TODO: add test for parsing.
        // TODO: pretty-printing of addresses with leading zeros.
    }

}
