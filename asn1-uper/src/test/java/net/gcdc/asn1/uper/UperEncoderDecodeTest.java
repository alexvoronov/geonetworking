package net.gcdc.asn1.uper;

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
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
//import static org.junit.Assert.fail;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
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

    @Test public void test() {
        //fail("Not yet implemented");
    }

}
