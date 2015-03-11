package net.gcdc.geonetworking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AreaTest {

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

        Position slottsberget   = new Position(57.702878, 11.927723);
        Position damen          = new Position(57.703864, 11.945876);
        Position sannegordshamn = new Position(57.708769, 11.927088);
        Position semcon         = new Position(57.709526, 11.943996);
        Position teater         = new Position(57.705714, 11.934608);
        Position jarntorget     = new Position(57.699786, 11.953191);
        Position kuggen         = new Position(57.706700, 11.938955);

        Area teaterSemcon = Area.ellipse(teater, 800, 400, 52);
        assertTrue(teaterSemcon.contains(semcon));
        assertFalse(teaterSemcon.contains(jarntorget));

        Area teaterTowardsKuggen = Area.ellipse(teater, 285, 50, 66);
        assertTrue(teaterTowardsKuggen.contains(kuggen));
        assertFalse(teaterTowardsKuggen.contains(semcon));

        Area teaterTowardsKuggenRect = Area.rectangle(teater, 285, 50, 66);
        assertTrue(teaterTowardsKuggenRect.contains(kuggen));
        assertFalse(teaterTowardsKuggenRect.contains(semcon));
        assertFalse(teaterTowardsKuggenRect.contains(sannegordshamn));

        Area aroundTeaterUpToKuggen = Area.circle(teater, 285);
        assertTrue(aroundTeaterUpToKuggen.contains(kuggen));
        assertFalse(aroundTeaterUpToKuggen.contains(semcon));
    }

}
