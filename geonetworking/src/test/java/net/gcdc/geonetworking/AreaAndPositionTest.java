package net.gcdc.geonetworking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class AreaAndPositionTest {

    @Test
    public void test() {
        Position p1 = new Position(58.0, 12.5); // Gbg
        Position p2 = new Position(59.5, 18.0); // Sto

        Position p3 = new Position(50.5, 7); // Cologne
        Position p4 = new Position(53, -1); // Nottingham

        Position p5 = new Position(30, 120); // China

        Position p1to2 = p1.moved(358000, 60);  // should be Sto too

        assertEquals(8915000, p4.distanceInMetersTo(p5), 300000);
        assertEquals( 616000, p3.distanceInMetersTo(p4), 1000);
        assertEquals( 358000, p1.distanceInMetersTo(p2), 1000);

        assertEquals( 60, p1.bearingInDegreesTowards(p2), 10);
        assertEquals(295, p3.bearingInDegreesTowards(p4), 10);

        assertTrue(p2.distanceInMetersTo(p1to2) < 10000);
    }

    @Test
    public void test3() {
        EqualsVerifier.forClass(Position.class).verify();
    }
}
