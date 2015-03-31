package net.gcdc.camdenm;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import net.gcdc.camdenm.CoopIts.AccelerationControl;
import net.gcdc.camdenm.CoopIts.Altitude;
import net.gcdc.camdenm.CoopIts.AltitudeConfidence;
import net.gcdc.camdenm.CoopIts.AltitudeValue;
import net.gcdc.camdenm.CoopIts.BasicContainer;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerLowFrequency;
import net.gcdc.camdenm.CoopIts.CamParameters;
import net.gcdc.camdenm.CoopIts.ClosedLanes;
import net.gcdc.camdenm.CoopIts.CoopAwareness;
import net.gcdc.camdenm.CoopIts.DrivingLaneStatus;
import net.gcdc.camdenm.CoopIts.ExteriorLights;
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.LightBarSirenInUse;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.LowFrequencyContainer;
import net.gcdc.camdenm.CoopIts.PathHistory;
import net.gcdc.camdenm.CoopIts.PathPoint;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
import net.gcdc.camdenm.CoopIts.PtActivation;
import net.gcdc.camdenm.CoopIts.PtActivationData;
import net.gcdc.camdenm.CoopIts.PtActivationType;
import net.gcdc.camdenm.CoopIts.PublicTransportContainer;
import net.gcdc.camdenm.CoopIts.ReferencePosition;
import net.gcdc.camdenm.CoopIts.RoadWorksContainerBasic;
import net.gcdc.camdenm.CoopIts.SemiAxisLength;
import net.gcdc.camdenm.CoopIts.SpecialVehicleContainer;
import net.gcdc.camdenm.CoopIts.StationType;
import net.gcdc.camdenm.CoopIts.VehicleRole;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoopItsTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    @Test public void test() {
        //List<Boolean> bitlist = Arrays.asList(true, false);
        //byte[] bytes = UperEncoder.boolToBits(Arrays.asList(true, false));

        assertArrayEquals(new byte[] { 127 }, UperEncoder.boolToBits(Arrays.asList(false, true, true, true, true, true, true, true)));
        assertArrayEquals(new byte[] { 2 }, UperEncoder.boolToBits(Arrays.asList(false, false, false, false, false, false, true, false)));
        assertArrayEquals(new byte[] { 127, 2 }, UperEncoder.boolToBits(Arrays.asList(false, true, true, true, true, true, true, true, false, false, false, false, false, false, true, false)));
        assertArrayEquals(new byte[] { 32 }, UperEncoder.boolToBits(Arrays.asList(false, false, true)));
    }

    @Test public void test2() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Cam cam = new CoopIts.Cam();

        byte[] encoded = UperEncoder.boolToBits(UperEncoder.encodeAsList(cam));

        logger.debug("data: {}", encoded);
        logger.debug("data hex: {}", UperEncoder.toHexString(encoded));
        logger.debug("data bin: {}", UperEncoder.toBinary(encoded));
        assertEquals("0102000000000064000D693A403AD274803FFFFFFC23B7743E00E11FDFFFFEBFE9ED0737530F5FFFB0",
                UperEncoder.toHexString(encoded));
    }

    @Test public void test3() throws IllegalArgumentException, IllegalAccessException {

        CoopIts.Cam cam =
                new CoopIts.Cam(
                        new ItsPduHeader(new MessageId(MessageId.cam)),
                        new CoopAwareness(
                                new CoopIts.GenerationDeltaTime(100),
                                new CamParameters(
                                        new BasicContainer(
                                                new StationType(CoopIts.StationType.unknown),
                                                new ReferencePosition(
                                                        new Latitude(57),
                                                        new Longitude(13),
                                                        new PosConfidenceEllipse(
                                                                new SemiAxisLength(SemiAxisLength.unavailable),
                                                                new SemiAxisLength(),
                                                                new HeadingValue(HeadingValue.unavailable)),
                                                        new Altitude(
                                                                new AltitudeValue(AltitudeValue.unavailable),
                                                                AltitudeConfidence.unavailable))),
                                        new HighFrequencyContainer(
                                                BasicVehicleContainerHighFrequency.builder()
                                                    .accelerationControl(AccelerationControl.builder()
                                                        .accEngaged(true)
                                                        .gasPedalEngaged(true)
                                                        .create())
                                                    .create()))));

        byte[] encoded = UperEncoder.boolToBits(UperEncoder.encodeAsList(cam));
        logger.debug("data hex: {}", UperEncoder.toHexString(encoded));
        assertEquals("01020000000000640006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB090",
                UperEncoder.toHexString(encoded));
    }

    @Test public void test4() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Cam cam =
                new CoopIts.Cam(
                        new ItsPduHeader(new MessageId(MessageId.cam)),
                        new CoopAwareness(
                                new CoopIts.GenerationDeltaTime(100),
                                new CamParameters(
                                        new BasicContainer(
                                                new StationType(CoopIts.StationType.unknown),
                                                new ReferencePosition(
                                                        new Latitude(57),
                                                        new Longitude(13),
                                                        new PosConfidenceEllipse(
                                                                new SemiAxisLength(SemiAxisLength.unavailable),
                                                                new SemiAxisLength(),
                                                                new HeadingValue(HeadingValue.unavailable)),
                                                        new Altitude(
                                                                new AltitudeValue(AltitudeValue.unavailable),
                                                                AltitudeConfidence.unavailable))),
                                        new HighFrequencyContainer(
                                                BasicVehicleContainerHighFrequency.builder()
                                                    .accelerationControl(AccelerationControl.builder()
                                                        .accEngaged(true)
                                                        .gasPedalEngaged(true)
                                                        .create())
                                                    .create()),
                                        new LowFrequencyContainer(
                                                new BasicVehicleContainerLowFrequency(
                                                        VehicleRole.default_,
                                                        ExteriorLights.builder().create(),
                                                        new PathHistory(
                                                                new PathPoint(),
                                                                new PathPoint()))),
                                        null)));
        logger.debug("data hex: {}", UperEncoder.toHexString(UperEncoder.boolToBits(UperEncoder.encodeAsList(cam))));
        byte[] encoded = UperEncoder.boolToBits(UperEncoder.encodeAsList(cam));
        logger.debug("data hex: {}", UperEncoder.toHexString(encoded));
        assertEquals("01020000000000644006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE00",
                UperEncoder.toHexString(encoded));
    }

    @Test public void test5() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Cam cam =
                new CoopIts.Cam(
                        new ItsPduHeader(new MessageId(MessageId.cam)),
                        new CoopAwareness(
                                new CoopIts.GenerationDeltaTime(100),
                                new CamParameters(
                                        new BasicContainer(
                                                new StationType(CoopIts.StationType.unknown),
                                                new ReferencePosition(
                                                        new Latitude(57),
                                                        new Longitude(13),
                                                        new PosConfidenceEllipse(
                                                                new SemiAxisLength(SemiAxisLength.unavailable),
                                                                new SemiAxisLength(),
                                                                new HeadingValue(HeadingValue.unavailable)),
                                                        new Altitude(
                                                                new AltitudeValue(AltitudeValue.unavailable),
                                                                AltitudeConfidence.unavailable))),
                                        new HighFrequencyContainer(
                                                BasicVehicleContainerHighFrequency.builder()
                                                    .accelerationControl(AccelerationControl.builder()
                                                        .accEngaged(true)
                                                        .gasPedalEngaged(true)
                                                        .create())
                                                    .create()),
                                        new LowFrequencyContainer(
                                                new BasicVehicleContainerLowFrequency(
                                                        VehicleRole.default_,
                                                        ExteriorLights.builder().create(),
                                                        new PathHistory(
                                                                new PathPoint(),
                                                                new PathPoint()))),
                                        new SpecialVehicleContainer(
                                                new PublicTransportContainer(
                                                        true,
                                                        new PtActivation(
                                                                new PtActivationType(PtActivationType.undefinedCodingType),
                                                                new PtActivationData(
                                                                        (byte) 0x0a,
                                                                        (byte) 0xff)))))));
        logger.debug("data hex: {}", UperEncoder.toHexString(UperEncoder.boolToBits(UperEncoder.encodeAsList(cam))));
        byte[] encoded = UperEncoder.boolToBits(UperEncoder.encodeAsList(cam));
        logger.debug("data hex: {}", UperEncoder.toHexString(encoded));
        assertEquals("01020000000000646006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE060010AFF0",
                UperEncoder.toHexString(encoded));
    }

    @Test public void test6() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Cam cam =
                new CoopIts.Cam(
                        new ItsPduHeader(new MessageId(MessageId.cam)),
                        new CoopAwareness(
                                new CoopIts.GenerationDeltaTime(100),
                                new CamParameters(
                                        new BasicContainer(
                                                new StationType(CoopIts.StationType.unknown),
                                                new ReferencePosition(
                                                        new Latitude(57),
                                                        new Longitude(13),
                                                        new PosConfidenceEllipse(
                                                                new SemiAxisLength(SemiAxisLength.unavailable),
                                                                new SemiAxisLength(),
                                                                new HeadingValue(HeadingValue.unavailable)),
                                                        new Altitude(
                                                                new AltitudeValue(AltitudeValue.unavailable),
                                                                AltitudeConfidence.unavailable))),
                                        new HighFrequencyContainer(
                                                BasicVehicleContainerHighFrequency.builder()
                                                    .accelerationControl(AccelerationControl.builder()
                                                        .accEngaged(true)
                                                        .gasPedalEngaged(true)
                                                        .create())
                                                    .create()),
                                        new LowFrequencyContainer(
                                                new BasicVehicleContainerLowFrequency(
                                                        VehicleRole.default_,
                                                        ExteriorLights.builder().create(),
                                                        new PathHistory(
                                                                new PathPoint(),
                                                                new PathPoint()))),
                                        new SpecialVehicleContainer(
                                                new RoadWorksContainerBasic(
                                                        null,
                                                        new LightBarSirenInUse(),
                                                        new ClosedLanes(
                                                                DrivingLaneStatus.builder()
                                                                    .setBit(3, true)
                                                                    .setBit(5, true)
                                                                    .outermostLaneClosed(true)
                                                                    .create()
                                                        ))))));
        logger.debug("data hex: {}", UperEncoder.toHexString(UperEncoder.boolToBits(UperEncoder.encodeAsList(cam))));
        byte[] encoded = UperEncoder.boolToBits(UperEncoder.encodeAsList(cam));
        logger.debug("data hex: {}", UperEncoder.toHexString(encoded));
        assertEquals("01020000000000646006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE1A0AA8",
                UperEncoder.toHexString(encoded));
    }

    @Test public void testFail() throws IllegalArgumentException, IllegalAccessException {
        //fail("not implemented yet");
    }

}
