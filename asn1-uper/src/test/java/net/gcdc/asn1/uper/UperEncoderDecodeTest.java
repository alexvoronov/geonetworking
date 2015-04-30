package net.gcdc.asn1.uper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import net.gcdc.camdenm.CoopIts;
import net.gcdc.camdenm.CoopIts.AccelerationControl;
import net.gcdc.camdenm.CoopIts.Altitude;
import net.gcdc.camdenm.CoopIts.AltitudeConfidence;
import net.gcdc.camdenm.CoopIts.AltitudeValue;
import net.gcdc.camdenm.CoopIts.BasicContainer;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.CamParameters;
import net.gcdc.camdenm.CoopIts.CoopAwareness;
import net.gcdc.camdenm.CoopIts.DrivingLaneStatus;
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
//import static org.junit.Assert.fail;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
import net.gcdc.camdenm.CoopIts.PosPillar;
import net.gcdc.camdenm.CoopIts.PositionOfPillars;
import net.gcdc.camdenm.CoopIts.PtActivationData;
import net.gcdc.camdenm.CoopIts.RSUContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.ReferencePosition;
import net.gcdc.camdenm.CoopIts.SemiAxisLength;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.StationType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UperEncoderDecodeTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoderDecodeTest.class);

    @Test public void decodeTest1() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        Object pdu = new ItsPduHeader(new ProtocolVersion(13), new MessageId(15), new StationID(17));
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("0D0F00000011",
                UperEncoder.hexStringFromBytes(encoded));

        ItsPduHeader decoded = UperEncoder.decode(encoded, ItsPduHeader.class);
        logger.debug("decoded: {}", decoded);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertEquals("0D0F00000011",
                UperEncoder.hexStringFromBytes(reencoded));

    }

    @Test public void decodeChoice1() {
        Object pdu =
            new CoopIts.Cam(
                new ItsPduHeader(new MessageId(MessageId.cam)),
                new CoopAwareness(
                    new CoopIts.GenerationDeltaTime(100),
                    new CamParameters(
                        new BasicContainer(
                            new StationType(CoopIts.StationType.tram),
                            new ReferencePosition(
                                new Latitude(57),
                                new Longitude(13),
                                new PosConfidenceEllipse(
                                    new SemiAxisLength(42),
                                    new SemiAxisLength(42),
                                    new HeadingValue(42)),
                                new Altitude(
                                    new AltitudeValue(42),
                                    AltitudeConfidence.unavailable))),
                        new HighFrequencyContainer(
                            BasicVehicleContainerHighFrequency.builder()
                              .accelerationControl(AccelerationControl.builder()
                                .accEngaged(true)
                                .gasPedalEngaged(true)
                                .create())
                              .create()))));
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        String expectedHex = "010200000000006400B6B49D272D693A41A05405405430D95E40E11FDFFFFEBFE9ED0737530F5FFFB090";
        assertEquals("encoded hex not equal", expectedHex,
                UperEncoder.hexStringFromBytes(encoded));

        Cam decoded = UperEncoder.decode(encoded, Cam.class);
        logger.debug("decoded: {}", decoded);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertEquals("reencoded hex not equal", expectedHex,
                UperEncoder.hexStringFromBytes(reencoded));

    }

    @Test public void decodeChoice2() {
        Object pdu =
            new CoopIts.Cam(
                new ItsPduHeader(new MessageId(MessageId.cam)),
                new CoopAwareness(
                    new CoopIts.GenerationDeltaTime(100),
                    new CamParameters(
                        new BasicContainer(
                            new StationType(CoopIts.StationType.tram),
                            new ReferencePosition(
                                new Latitude(57),
                                new Longitude(13),
                                new PosConfidenceEllipse(
                                    new SemiAxisLength(42),
                                    new SemiAxisLength(42),
                                    new HeadingValue(42)),
                                new Altitude(
                                    new AltitudeValue(42),
                                    AltitudeConfidence.unavailable))),
                        new HighFrequencyContainer(
                            new RSUContainerHighFrequency(null)
                            ))));
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("encodedChoice2 data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        String expectedHex = "010200000000006400B6B49D272D693A41A05405405430D95E80";
        assertEquals("encoded hex not equal", expectedHex,
                UperEncoder.hexStringFromBytes(encoded));

        Cam decoded = UperEncoder.decode(encoded, Cam.class);
        logger.debug("(test) decoded object: {}", decoded);
        byte[] reencoded = UperEncoder.encode(decoded);
        logger.debug("reencodedChoice2 data hex: {}", UperEncoder.hexStringFromBytes(reencoded));
        assertEquals("reencoded hex not equal", expectedHex,
                UperEncoder.hexStringFromBytes(reencoded));
    }

    @Test public void bitstringTest1() {
        Object pdu = DrivingLaneStatus.builder().outermostLaneClosed(true).setBit(3, true).create();
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("encoded bitstring data hex: {}", UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, DrivingLaneStatus.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    /**
     * <pre>
PosPillar ::= INTEGER {tenCentimeters(1), unavailable(30)} (1..30)
PositionOfPillars ::= SEQUENCE (SIZE(1..3, ...)) OF PosPillar
     *  </pre
     */
    @Test public void seqofTest1() {
        byte[] encoded = null;
        byte[] reencoded = null;
        Object pdu = new PositionOfPillars(new PosPillar(8));
        encoded = UperEncoder.encode(pdu);
        logger.debug("encoded seq-of data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        String expectedHex = "07";
        assertEquals("encoded hex not equal", expectedHex, UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, PositionOfPillars.class);
        reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void octetTest() {
//        Object pdu = new PtActivationData(new byte[] {42, 43, 44, (byte)150, -10});
        Object pdu = new PtActivationData(new byte[] {0x2A, 0x2B, (byte)0x96, (byte)0xFF});
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("encoded octetstring data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        String expectedHex = "19515CB7F8";
        assertEquals("encoded hex not equal", expectedHex, UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, PtActivationData.class);
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void test() {
        //fail("Not yet implemented");
    }

}
