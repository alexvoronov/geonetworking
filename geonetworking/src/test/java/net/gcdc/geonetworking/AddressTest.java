package net.gcdc.geonetworking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class AddressTest {

    @Test
    public void test() {
        Address a = Address.random(true, StationType.Bus, 752);
        assertEquals(a.isManual(), true);
        assertEquals(a.stationType(), StationType.Bus);
        assertEquals(a.countryCode(), 752);
        assertNotEquals(Address.random(false, StationType.Bus, 752).lowLevelAddress(), a.lowLevelAddress());
        assertEquals(Address.random(false, StationType.Passenger_Car, 300).isManual(), false);
    }

    @Test
    public void test2() {
        EqualsVerifier.forClass(Address.class).verify();
    }

}
