package net.gcdc.asn1.uper;

import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.SizeRange;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BitstringExtensionTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    /**
     * Example PDU:
     * <pre>
     * LaneAttributes ::= BIT STRING {
     *     isVehicleRevocableLane (0),
     *     isVehicleFlyOverLane (1),
     *     hovLaneUseOnly (2),
     *     restrictedToBusUse (3),
     *     restrictedToTaxiUse (4),
     *     restrictedFromPublicUse (5),
     *     hasIRbeaconCoverage (6),
     *     permissionOnRequest (7)
     * } (SIZE (8,...))
     * </pre>
     */
    @SizeRange(minValue = 8, maxValue = 8, hasExtensionMarker = true)
    public static class LaneAttributes extends Asn1SequenceOf<Boolean> {
        public LaneAttributes(Collection<Boolean> coll) { super(coll); }
    }

    @Test
    public void testBitstring() {
        LaneAttributes laTrue = new LaneAttributes(Arrays.asList(true, true, true, true, true, true, true, true));
        String hexTrue = "7F80";
        LaneAttributes laFalse = new LaneAttributes(Arrays.asList(false, false, false, false, false, false, false, false));
        String hexFalse = "0000";
        LaneAttributes laFT = new LaneAttributes(Arrays.asList(false, true, false, true, false, true, false, true));
        String hexFT = "2A80";

        byte[] encoded = UperEncoder.encode(laTrue);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals(hexTrue, UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, LaneAttributes.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);

        assertEquals(hexTrue, UperEncoder.hexStringFromBytes(UperEncoder.encode(laTrue)));
        assertEquals(hexFT, UperEncoder.hexStringFromBytes(UperEncoder.encode(laFT)));
        assertEquals(hexFalse, UperEncoder.hexStringFromBytes(UperEncoder.encode(laFalse)));
    }
}