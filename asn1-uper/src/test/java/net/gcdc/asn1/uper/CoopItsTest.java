package net.gcdc.asn1.uper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import net.gcdc.camdenm.CoopIts;
import net.gcdc.camdenm.CoopIts.AccelerationControl;
import net.gcdc.camdenm.CoopIts.ActionID;
import net.gcdc.camdenm.CoopIts.AlacarteContainer;
import net.gcdc.camdenm.CoopIts.Altitude;
import net.gcdc.camdenm.CoopIts.AltitudeConfidence;
import net.gcdc.camdenm.CoopIts.AltitudeValue;
import net.gcdc.camdenm.CoopIts.BasicContainer;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerLowFrequency;
import net.gcdc.camdenm.CoopIts.CamParameters;
import net.gcdc.camdenm.CoopIts.CauseCode;
import net.gcdc.camdenm.CoopIts.CauseCodeType;
import net.gcdc.camdenm.CoopIts.ClosedLanes;
import net.gcdc.camdenm.CoopIts.CoopAwareness;
import net.gcdc.camdenm.CoopIts.DangerousGoodsBasic;
import net.gcdc.camdenm.CoopIts.DangerousGoodsExtended;
import net.gcdc.camdenm.CoopIts.DecentralizedEnvironmentalNotificationMessage;
import net.gcdc.camdenm.CoopIts.DeltaAltitude;
import net.gcdc.camdenm.CoopIts.DeltaLatitude;
import net.gcdc.camdenm.CoopIts.DeltaLongitude;
import net.gcdc.camdenm.CoopIts.DeltaReferencePosition;
import net.gcdc.camdenm.CoopIts.DrivingLaneStatus;
import net.gcdc.camdenm.CoopIts.EnergyStorageType;
import net.gcdc.camdenm.CoopIts.EventHistory;
import net.gcdc.camdenm.CoopIts.EventPoint;
import net.gcdc.camdenm.CoopIts.ExteriorLights;
import net.gcdc.camdenm.CoopIts.HardShoulderStatus;
import net.gcdc.camdenm.CoopIts.Heading;
import net.gcdc.camdenm.CoopIts.HeadingConfidence;
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HeightLonCarr;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ImpactReductionContainer;
import net.gcdc.camdenm.CoopIts.InformationQuality;
import net.gcdc.camdenm.CoopIts.ItineraryPath;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.LanePosition;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.LightBarSirenInUse;
import net.gcdc.camdenm.CoopIts.LocationContainer;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.LowFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ManagementContainer;
import net.gcdc.camdenm.CoopIts.NumberOfOccupants;
import net.gcdc.camdenm.CoopIts.PathDeltaTime;
import net.gcdc.camdenm.CoopIts.PathHistory;
import net.gcdc.camdenm.CoopIts.PathPoint;
import net.gcdc.camdenm.CoopIts.PosCentMass;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
import net.gcdc.camdenm.CoopIts.PosFrontAx;
import net.gcdc.camdenm.CoopIts.PosLonCarr;
import net.gcdc.camdenm.CoopIts.PosPillar;
import net.gcdc.camdenm.CoopIts.PositionOfOccupants;
import net.gcdc.camdenm.CoopIts.PositionOfPillars;
import net.gcdc.camdenm.CoopIts.PositioningSolutionType;
import net.gcdc.camdenm.CoopIts.PtActivation;
import net.gcdc.camdenm.CoopIts.PtActivationData;
import net.gcdc.camdenm.CoopIts.PtActivationType;
import net.gcdc.camdenm.CoopIts.PublicTransportContainer;
import net.gcdc.camdenm.CoopIts.ReferenceDenms;
import net.gcdc.camdenm.CoopIts.ReferencePosition;
import net.gcdc.camdenm.CoopIts.RelevanceDistance;
import net.gcdc.camdenm.CoopIts.RelevanceTrafficDirection;
import net.gcdc.camdenm.CoopIts.RequestResponseIndication;
import net.gcdc.camdenm.CoopIts.RestrictedTypes;
import net.gcdc.camdenm.CoopIts.RoadType;
import net.gcdc.camdenm.CoopIts.RoadWorksContainerBasic;
import net.gcdc.camdenm.CoopIts.RoadWorksContainerExtended;
import net.gcdc.camdenm.CoopIts.SemiAxisLength;
import net.gcdc.camdenm.CoopIts.SequenceNumber;
import net.gcdc.camdenm.CoopIts.SituationContainer;
import net.gcdc.camdenm.CoopIts.SpecialVehicleContainer;
import net.gcdc.camdenm.CoopIts.Speed;
import net.gcdc.camdenm.CoopIts.SpeedConfidence;
import net.gcdc.camdenm.CoopIts.SpeedLimit;
import net.gcdc.camdenm.CoopIts.SpeedValue;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.StationType;
import net.gcdc.camdenm.CoopIts.StationarySince;
import net.gcdc.camdenm.CoopIts.StationaryVehicleContainer;
import net.gcdc.camdenm.CoopIts.SubCauseCodeType;
import net.gcdc.camdenm.CoopIts.Temperature;
import net.gcdc.camdenm.CoopIts.Termination;
import net.gcdc.camdenm.CoopIts.TimestampIts;
import net.gcdc.camdenm.CoopIts.Traces;
import net.gcdc.camdenm.CoopIts.TrafficRule;
import net.gcdc.camdenm.CoopIts.TransmissionInterval;
import net.gcdc.camdenm.CoopIts.TurningRadius;
import net.gcdc.camdenm.CoopIts.VDS;
import net.gcdc.camdenm.CoopIts.ValidityDuration;
import net.gcdc.camdenm.CoopIts.VehicleIdentification;
import net.gcdc.camdenm.CoopIts.VehicleMass;
import net.gcdc.camdenm.CoopIts.VehicleRole;
import net.gcdc.camdenm.CoopIts.WMInumber;
import net.gcdc.camdenm.CoopIts.WheelBaseVehicle;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoopItsTest {
    private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);

    @Test public void test() {
        //List<Boolean> bitlist = Arrays.asList(true, false);
        //byte[] bytes = UperEncoder.boolToBits(Arrays.asList(true, false));

        assertArrayEquals(new byte[] { 127 }, UperEncoder.bytesFromCollection(Arrays.asList(false, true, true, true, true, true, true, true)));
        assertArrayEquals(new byte[] { 2 }, UperEncoder.bytesFromCollection(Arrays.asList(false, false, false, false, false, false, true, false)));
        assertArrayEquals(new byte[] { 127, 2 }, UperEncoder.bytesFromCollection(Arrays.asList(false, true, true, true, true, true, true, true, false, false, false, false, false, false, true, false)));
        assertArrayEquals(new byte[] { 32 }, UperEncoder.bytesFromCollection(Arrays.asList(false, false, true)));
    }

    @Test public void test2() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Cam cam = new CoopIts.Cam();

        byte[] encoded = UperEncoder.encode(cam);

        logger.debug("data: {}", encoded);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        logger.debug("data bin: {}", UperEncoder.binaryStringFromBytes(encoded));
        assertEquals("0102000000000064000D693A403AD274803FFFFFFC23B7743E00E11FDFFFFEBFE9ED0737530F5FFFB0",
                UperEncoder.hexStringFromBytes(encoded));
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

        byte[] encoded = UperEncoder.encode(cam);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("01020000000000640006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB090",
                UperEncoder.hexStringFromBytes(encoded));
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
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(cam)));
        byte[] encoded = UperEncoder.encode(cam);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("01020000000000644006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE00",
                UperEncoder.hexStringFromBytes(encoded));
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
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(cam)));
        byte[] encoded = UperEncoder.encode(cam);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("01020000000000646006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE060010AFF0",
                UperEncoder.hexStringFromBytes(encoded));
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
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(cam)));
        byte[] encoded = UperEncoder.encode(cam);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("01020000000000646006B49D272D693A41BFFFFFFC23B7743E40E11FDFFFFEBFE9ED0737530F5FFFB09000013FFFFFFFFF1CE3FFFFFFFFF1CE1A0AA8",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, cam.getClass());
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void basicDenmTest() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Denm denm = new CoopIts.Denm();
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(denm)));
        byte[] encoded = UperEncoder.encode(denm);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("010100000000010000000000000000000000000000000006B49D201D693A401FFFFFFE11DBBA1F012C0000",
                UperEncoder.hexStringFromBytes(encoded));
    }

    @Test public void sequenceOfSizeExtensionTest() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Denm denm = new CoopIts.Denm(
          new ItsPduHeader(new MessageId(MessageId.denm)),
          new DecentralizedEnvironmentalNotificationMessage(
            new ManagementContainer(),
            null,
            null,
            new AlacarteContainer(
              null,
              null,
              null,
              new RoadWorksContainerExtended(
                null,
                null,
                new RestrictedTypes(
                  new StationType(StationType.cyclist),
                  new StationType(StationType.cyclist)
                ),
                null,
                null,
                null,
                null,
                null,
                null
              ),
              null,
              null
            )
          )
        );
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(denm)));
        byte[] encoded = UperEncoder.encode(denm);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("010100000000210000000000000000000000000000000006B49D201D693A401FFFFFFE11DBBA1F012C000420102020",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, denm.getClass());
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }


    @Test public void sequenceOfSizeExtensionTest2() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Denm denm = new CoopIts.Denm(
          new ItsPduHeader(new MessageId(MessageId.denm)),
          new DecentralizedEnvironmentalNotificationMessage(
            new ManagementContainer(),
            null,
            null,
            new AlacarteContainer(
              null,
              null,
              null,
              new RoadWorksContainerExtended(
                null,
                null,
                new RestrictedTypes(
                  new StationType(StationType.bus)
                ),
                null,
                null,
                null,
                null,
                null,
                null
              ),
              null,
              null
            )
          )
        );
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(denm)));
        byte[] encoded = UperEncoder.encode(denm);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("010100000000210000000000000000000000000000000006B49D201D693A401FFFFFFE11DBBA1F012C0004200060",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, denm.getClass());
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void DenmTestLong() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Denm denm = new CoopIts.Denm(
          new ItsPduHeader(new MessageId(MessageId.denm)),
          new DecentralizedEnvironmentalNotificationMessage(
            new ManagementContainer(
              new ActionID(
                new StationID(82),
                new SequenceNumber(3879)
              ),
              new TimestampIts(298379),
              new TimestampIts(4879437),
              Termination.defaultValue(),
              new ReferencePosition(),
              RelevanceDistance.defaultValue(),
              RelevanceTrafficDirection.defaultValue(),
              new ValidityDuration(83209),
              new TransmissionInterval(232),
              new StationType(8)
            ),
            new SituationContainer(
                new InformationQuality(3),
                new CauseCode(
                    new CauseCodeType(CauseCodeType.adverseWeatherCondition_Visibility),
                    new SubCauseCodeType(13)
                ),
                new CauseCode(
                    new CauseCodeType(67),
                    new SubCauseCodeType(95)
                ),
                new EventHistory(
                    new EventPoint(
                         new DeltaReferencePosition(),
                         new PathDeltaTime(837),
                         new InformationQuality(6)
                    )
                )
            ),
            new LocationContainer(
                new Speed(
                    new SpeedValue(950),
                    new SpeedConfidence(100)
                ),
                new Heading(
                    new HeadingValue(1800),
                    new HeadingConfidence(37)
                ),
                new Traces(
                    new PathHistory(
                        new PathPoint(
                            new DeltaReferencePosition(
                                new DeltaLatitude(28389),
                                new DeltaLongitude(83487),
                                new DeltaAltitude(-5000)),
                            new PathDeltaTime(200)
                        ),
                        new PathPoint()
                    ),
                    new PathHistory(
                        new PathPoint()
                    )
                ),
                RoadType.defaultValue()
            ),
            new AlacarteContainer(
              new LanePosition(LanePosition.secondLaneFromOutside),
              new ImpactReductionContainer(
                      new HeightLonCarr(),
                      new HeightLonCarr(),
                      new PosLonCarr(),
                      new PosLonCarr(),
                      new PositionOfPillars(
                              new PosPillar()),
                      new PosCentMass(),
                      new WheelBaseVehicle(),
                      new TurningRadius(),
                      new PosFrontAx(),
                      new PositionOfOccupants(),
                      new VehicleMass(),
                      RequestResponseIndication.defaultValue()
              ),
              new Temperature(27),
              new RoadWorksContainerExtended(
                new LightBarSirenInUse(),
                new ClosedLanes(
                  HardShoulderStatus.availableForDriving,
                  new DrivingLaneStatus(true, false, false, true)),
                new RestrictedTypes(
                  new StationType(StationType.bus)
                ),
                new SpeedLimit(120 * SpeedLimit.oneKmPerHour),
                new CauseCode(
                  new CauseCodeType(CauseCodeType.adverseWeatherCondition_Visibility),
                  new SubCauseCodeType(13)
                ),
                new ItineraryPath(
                  new ReferencePosition()
                ),
                new DeltaReferencePosition(),
                TrafficRule.noPassingForTrucks,
                new ReferenceDenms(
                  new ActionID())
              ),
              PositioningSolutionType.sGNSSplusDR,
              new StationaryVehicleContainer(
                StationarySince.lessThan15Minutes,
                null,
                null,
                new NumberOfOccupants(13),
                null,
                new EnergyStorageType()
              )
            )
          )
        );
//        logger.debug("data hex: {}", UperEncoder.toHexString(UperEncoder.boolToBits(UperEncoder.encodeAsList(denm))));
        byte[] encoded = UperEncoder.encode(denm);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("010100000000EF80000029079380000091B160000253A26B5A4E900EB49D200FFFFFFF08EDDD0F828A120738436241A86BE0FFFFFFFFFE39C01A2670EDB1B84242166EE4D1878F0A0031DFFFFFFFFF8E7017FFFFFFFFE39C1F9E3C7FBF0EFDFBFA600001FFABFFC63900CEE241A06B49D201D693A401FFFFFFE11DBBA1FFFFFFFFFFC73840000000000000E58680",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, denm.getClass());
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void DenmTestString() throws IllegalArgumentException, IllegalAccessException {
        CoopIts.Denm denm = new CoopIts.Denm(
          new ItsPduHeader(new MessageId(MessageId.denm)),
          new DecentralizedEnvironmentalNotificationMessage(
            new ManagementContainer(
              new ActionID(
                new StationID(82),
                new SequenceNumber(3879)
              ),
              new TimestampIts(298379),
              new TimestampIts(4879437),
              Termination.defaultValue(),
              new ReferencePosition(),
              RelevanceDistance.defaultValue(),
              RelevanceTrafficDirection.defaultValue(),
              new ValidityDuration(83209),
              new TransmissionInterval(232),
              new StationType(8)
            ),
            new SituationContainer(
                new InformationQuality(3),
                new CauseCode(
                    new CauseCodeType(CauseCodeType.adverseWeatherCondition_Visibility),
                    new SubCauseCodeType(13)
                ),
                new CauseCode(
                    new CauseCodeType(67),
                    new SubCauseCodeType(95)
                ),
                new EventHistory(
                    new EventPoint(
                         new DeltaReferencePosition(),
                         new PathDeltaTime(837),
                         new InformationQuality(6)
                    )
                )
            ),
            new LocationContainer(
                new Speed(
                    new SpeedValue(950),
                    new SpeedConfidence(100)
                ),
                new Heading(
                    new HeadingValue(1800),
                    new HeadingConfidence(37)
                ),
                new Traces(
                    new PathHistory(
                        new PathPoint(
                            new DeltaReferencePosition(
                                new DeltaLatitude(28389),
                                new DeltaLongitude(83487),
                                new DeltaAltitude(-5000)),
                            new PathDeltaTime(200)
                        ),
                        new PathPoint()
                    ),
                    new PathHistory(
                        new PathPoint()
                    )
                ),
                RoadType.defaultValue()
            ),
            new AlacarteContainer(
              new LanePosition(LanePosition.secondLaneFromOutside),
              new ImpactReductionContainer(
                      new HeightLonCarr(),
                      new HeightLonCarr(),
                      new PosLonCarr(),
                      new PosLonCarr(),
                      new PositionOfPillars(
                              new PosPillar()),
                      new PosCentMass(),
                      new WheelBaseVehicle(),
                      new TurningRadius(),
                      new PosFrontAx(),
                      new PositionOfOccupants(),
                      new VehicleMass(),
                      RequestResponseIndication.defaultValue()
              ),
              new Temperature(27),
              new RoadWorksContainerExtended(
                new LightBarSirenInUse(),
                new ClosedLanes(
                  HardShoulderStatus.availableForDriving,
                  new DrivingLaneStatus(true, false, false, true)),
                new RestrictedTypes(
                  new StationType(StationType.bus)
                ),
                new SpeedLimit(120 * SpeedLimit.oneKmPerHour),
                new CauseCode(
                  new CauseCodeType(CauseCodeType.adverseWeatherCondition_Visibility),
                  new SubCauseCodeType(13)
                ),
                new ItineraryPath(
                  new ReferencePosition()
                ),
                new DeltaReferencePosition(),
                TrafficRule.noPassingForTrucks,
                new ReferenceDenms(
                  new ActionID())
              ),
              PositioningSolutionType.sGNSSplusDR,
              new StationaryVehicleContainer(
                StationarySince.lessThan15Minutes,
                null,
                null,
                new NumberOfOccupants(13),
                new VehicleIdentification(
                  new WMInumber("asd"),
                  new VDS("zxcABC")
                ),
                new EnergyStorageType()
              )
            )
          )
        );
//        logger.debug("data hex: {}", UperEncoder.toHexString(UperEncoder.boolToBits(UperEncoder.encodeAsList(denm))));
        byte[] encoded = UperEncoder.encode(denm);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("010100000000EF80000029079380000091B160000253A26B5A4E900EB49D200FFFFFFF08EDDD0F828A120738436241A86BE0FFFFFFFFFE39C01A2670EDB1B84242166EE4D1878F0A0031DFFFFFFFFF8E7017FFFFFFFFE39C1F9E3C7FBF0EFDFBFA600001FFABFFC63900CEE241A06B49D201D693A401FFFFFFE11DBBA1FFFFFFFFFFC73840000000000000E786BB0F3C9EBC63830A1800",
                UperEncoder.hexStringFromBytes(encoded));

        Object decoded = UperEncoder.decode(encoded, denm.getClass());
        byte[] reencoded = UperEncoder.encode(decoded);
        assertArrayEquals("encoded and reencoded", encoded, reencoded);
    }

    @Test public void StringTest() throws IllegalArgumentException, IllegalAccessException {
        Object pdu = new VehicleIdentification(
                new WMInumber("asd"),
                new VDS("zxcABC")
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("761E793D78C7061430",
                UperEncoder.hexStringFromBytes(encoded));
    }

    @Test public void Utf8StringTest() throws IllegalArgumentException, IllegalAccessException {
        Object pdu = new DangerousGoodsExtended(
                DangerousGoodsBasic.explosives4, 13, false, false, true, "abc", "cde", "zxc"
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("E300348B0E2C62C793281BD3C318",
                UperEncoder.hexStringFromBytes(encoded));
    }

    @Test public void Utf8StringTest2() throws IllegalArgumentException, IllegalAccessException {
        Object pdu = new DangerousGoodsExtended(
                DangerousGoodsBasic.explosives4, 13, false, false, true, "abc", "cde", "a"
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("E300348B0E2C62C793280B08",
                UperEncoder.hexStringFromBytes(encoded));
    }

    @Test public void Utf8StringTest3() throws IllegalArgumentException, IllegalAccessException {
        Object pdu = new DangerousGoodsExtended(
                DangerousGoodsBasic.explosives4, 13, false, false, true, "abc", "cde", "abcöxyz"
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("E300348B0E2C62C79328430B131E1DB3C3CBD0",
                UperEncoder.hexStringFromBytes(encoded));
    }



    @Test public void Utf8StringTest4() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            sb.append("1234567890");
        }
        String string150 = sb.toString();
        Object pdu = new Utf8TestClass(
                new Utf8TestClass.CompanyName(string150)
              );
        byte[] encoded = UperEncoder.encode(pdu);
        logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded));
        assertEquals("8096313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930313233343536373839303132333435363738393031323334353637383930",
                UperEncoder.hexStringFromBytes(encoded));
    }

    @Test public void testFail() throws IllegalArgumentException, IllegalAccessException {
        //fail("not implemented yet");
    }

    @Test public void testException() {

        CoopIts.Cam cam =
                new CoopIts.Cam(
                        new ItsPduHeader(new MessageId(MessageId.cam)),
                        new CoopAwareness(
                                new CoopIts.GenerationDeltaTime(100),
                                new CamParameters(
                                        new BasicContainer(
                                                new StationType(CoopIts.StationType.unknown),
                                                new ReferencePosition(
                                                        new Latitude(900000001 + 1),
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

        try {
            byte[] encoded2 = UperEncoder.encode(cam);
        } catch (IllegalArgumentException e) {
            logger.debug("got expected exception {}", e.getMessage());
            return;
        }
        fail("no exception");
    }

}
