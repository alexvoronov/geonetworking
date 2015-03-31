package net.gcdc.camdenm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;

/**
 * A hand-made class for Cooperative Awareness Message (CAM) and Decentralized Environment
 * Notification Message (DENM).
 *
 * @see Common Data Dictionary: <a href="http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=43353">ETSI TS 102 894-2</a>,
 * @see CAM: <a href="http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=37126">CAM: ETSI EN 302 637-2</a>,
 * @see DENM: <a href="http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=37127">ETSI EN 302 637-3</a>
 *
 */
public class CoopIts {

    @Sequence
    public static class Cam {
        ItsPduHeader header;
        CoopAwareness cam;

        public Cam(ItsPduHeader itsPduHeader, CoopAwareness coopAwareness) {
            this.header = itsPduHeader;
            this.cam = coopAwareness;
        }

        public Cam() { this(new ItsPduHeader(new MessageId(MessageId.cam)), new CoopAwareness()); }

        @Override public String toString() { return "{header " + header + ", cam " + cam + "}"; }
    }

    @Sequence
    public static class ItsPduHeader {
        ProtocolVersion protocolVersion;
        MessageId messageID;
        StationID stationID;

        @Override public String toString() {
            return "{protocolVersion " + protocolVersion + ", messageID " + messageID +
                    ", stationID " + stationID + "}";
        }

        @Asn1AnonymousType
        @IntRange(minValue = 0, maxValue = 255)
        public static class ProtocolVersion extends Asn1Integer {
            public static final int currentVersion = 1;
            public ProtocolVersion() { this(currentVersion); }
            public ProtocolVersion(int value) { super(value); }
        }

        @Asn1AnonymousType
        @IntRange(minValue = 0, maxValue = 255)
        public static class MessageId extends Asn1Integer {
            public static final int denm   = 1;
            public static final int cam    = 2;
            public static final int poi    = 3;
            public static final int spat   = 4;
            public static final int map    = 5;
            public static final int ivi    = 6;
            public static final int ev_rsr = 7;
            public MessageId() { this(0); }
            public MessageId(int value) { super(value); }
        }

        public ItsPduHeader() {this (new ProtocolVersion(), new MessageId(), new StationID()); }

        public ItsPduHeader(ProtocolVersion protocolVersion, MessageId messageId, StationID stationId) {
            this.protocolVersion = protocolVersion;
            this.messageID = messageId;
            this.stationID = stationId;
        }

        // Convenience constructor
        public ItsPduHeader(MessageId messageId) {
            this(new ProtocolVersion(), messageId, new StationID());
        }
    }

    @IntRange(minValue = 0, maxValue = 4294967295L)
    public static class StationID extends Asn1Integer {
        public StationID() { this(0); }
        public StationID(long value) { super(value); }
    }

    @Sequence
    public static class CoopAwareness {
        GenerationDeltaTime generationDeltaTime;
        CamParameters camParameters;

        public CoopAwareness() { this(new GenerationDeltaTime(), new CamParameters()); }
        public CoopAwareness(GenerationDeltaTime generationDeltaTime, CamParameters camParameters) {
            this.generationDeltaTime = generationDeltaTime;
            this.camParameters = camParameters;
        }
    }

    @IntRange(minValue = 0, maxValue = 65535)
    public static class GenerationDeltaTime extends Asn1Integer {
        public static final int oneMilliSec = 1;

        public GenerationDeltaTime() { this(100 * oneMilliSec); }
        public GenerationDeltaTime(int value) { super(value); }
    }

    @Sequence
    @HasExtensionMarker
    public static class CamParameters {
        BasicContainer basicContainer;
        HighFrequencyContainer highFrequencyContainer;
        @Asn1Optional LowFrequencyContainer lowFrequencyContainer;
        @Asn1Optional SpecialVehicleContainer specialVehicleContainer;

        public CamParameters() { this(new BasicContainer(), new HighFrequencyContainer()); }

        public CamParameters(
                BasicContainer basicContainer,
                HighFrequencyContainer highFrequencyContainer
                ) {
            this.basicContainer = basicContainer;
            this.highFrequencyContainer = highFrequencyContainer;
        }

        public CamParameters(BasicContainer basicContainer,
        HighFrequencyContainer highFrequencyContainer,
        LowFrequencyContainer lowFrequencyContainer,
        SpecialVehicleContainer specialVehicleContainer) {
            this.basicContainer = basicContainer;
            this.highFrequencyContainer = highFrequencyContainer;
            this.lowFrequencyContainer = lowFrequencyContainer;
            this.specialVehicleContainer = specialVehicleContainer;

        }
    }

    @Sequence
    @HasExtensionMarker
    public static class BasicContainer {
        StationType stationType;
        ReferencePosition referencePosition;
        public BasicContainer() { this(new StationType(), new ReferencePosition()); }
        public BasicContainer(StationType stationType, ReferencePosition referencePosition) {
            this.stationType = stationType;
            this.referencePosition = referencePosition;
        }
    }

    @Choice
    @HasExtensionMarker
    public static class HighFrequencyContainer {
        BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency;
        RSUContainerHighFrequency rsuContainerHighFrequency;

        public HighFrequencyContainer() { this(BasicVehicleContainerHighFrequency.builder().create()); }
        public HighFrequencyContainer(BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency) {
            this.basicVehicleContainerHighFrequency = basicVehicleContainerHighFrequency;
            this.rsuContainerHighFrequency = null;
        }
        public HighFrequencyContainer(RSUContainerHighFrequency rsuContainerHighFrequency) {
            this.basicVehicleContainerHighFrequency = null;
            this.rsuContainerHighFrequency = rsuContainerHighFrequency;
        }
    }

    @Sequence
    public static class BasicVehicleContainerHighFrequency {
        Heading heading;
        Speed speed;
        DriveDirection driveDirection;
        VehicleLength vehicleLength;
        VehicleWidth vehicleWidth;
        LongitudinalAcceleration longitudinalAcceleration;
        Curvature curvature;
        CurvatureCalculationMode curvatureCalculationMode;
        YawRate yawRate;
        @Asn1Optional AccelerationControl accelerationControl;
        @Asn1Optional LanePosition lanePosition;
        @Asn1Optional SteeringWheelAngle steeringWheelAngle;
        @Asn1Optional LateralAcceleration lateralAcceleration;
        @Asn1Optional VerticalAcceleration verticalAcceleration;
        @Asn1Optional PerformanceClass performanceClass;
        @Asn1Optional CenDsrcTollingZone cenDsrcTollingZone;

        protected BasicVehicleContainerHighFrequency() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private BasicVehicleContainerHighFrequency val = new BasicVehicleContainerHighFrequency();
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public BasicVehicleContainerHighFrequency create() { created = true; return val; }

            private Builder() {
                val.heading = new Heading();
                val.speed = new Speed();
                val.driveDirection = DriveDirection.UNAVAILABLE;
                val.vehicleLength = new VehicleLength();
                val.vehicleWidth = new VehicleWidth();
                val.longitudinalAcceleration = new LongitudinalAcceleration();
                val.curvature = new Curvature();
                val.curvatureCalculationMode = CurvatureCalculationMode.unavailable;
                val.yawRate = new YawRate();
            }

            public Builder heading(Heading heading) { checkCreated(); val.heading = heading; return this; }
            public Builder speed(Speed speed) { checkCreated(); val.speed = speed; return this; }
            public Builder driveDirection(DriveDirection driveDirection) { checkCreated(); val.driveDirection = driveDirection; return this; }
            public Builder vehicleLength(VehicleLength vehicleLength) { checkCreated(); val.vehicleLength = vehicleLength; return this; }
            public Builder vehicleWidth(VehicleWidth vehicleWidth) { checkCreated(); val.vehicleWidth = vehicleWidth; return this; }
            public Builder longitudinalAcceleration(LongitudinalAcceleration longitudinalAcceleration) { checkCreated(); val.longitudinalAcceleration = longitudinalAcceleration; return this; }
            public Builder curvature(Curvature curvature) { checkCreated(); val.curvature = curvature; return this; }
            public Builder curvatureCalculationMode(CurvatureCalculationMode curvatureCalculationMode) { checkCreated(); val.curvatureCalculationMode = curvatureCalculationMode; return this; }
            public Builder yawRate(YawRate yawRate) { checkCreated(); val.yawRate = yawRate; return this; }
            public Builder accelerationControl(AccelerationControl accelerationControl) { checkCreated(); val.accelerationControl = accelerationControl; return this; }
            public Builder lanePosition(LanePosition lanePosition) { checkCreated(); val.lanePosition = lanePosition; return this; }
            public Builder steeringWheelAngle(SteeringWheelAngle steeringWheelAngle) { checkCreated(); val.steeringWheelAngle = steeringWheelAngle; return this; }
            public Builder lateralAcceleration(LateralAcceleration lateralAcceleration) { checkCreated(); val.lateralAcceleration = lateralAcceleration; return this; }
            public Builder verticalAcceleration(VerticalAcceleration verticalAcceleration) { checkCreated(); val.verticalAcceleration = verticalAcceleration; return this; }
            public Builder performanceClass(PerformanceClass performanceClass) { checkCreated(); val.performanceClass = performanceClass; return this; }
            public Builder cenDsrcTollingZone(CenDsrcTollingZone cenDsrcTollingZone) { checkCreated(); val.cenDsrcTollingZone = cenDsrcTollingZone; return this; }
        }
    }

    @IntRange(minValue = 0, maxValue = 255)
    public static class StationType extends Asn1Integer {
        public static final int unknown = 0;
        public static final int pedestrian = 1;
        public static final int cyclist = 2;
        public static final int moped = 3;
        public static final int motorcycle = 4;
        public static final int passengerCar = 5;
        public static final int bus = 6;
        public static final int lightTruck = 7;
        public static final int heavyTruck = 8;
        public static final int trailer = 9;
        public static final int specialVehicles = 10;
        public static final int tram = 11;
        public static final int roadSideUnit = 15;

        public StationType() { this(unknown); }
        public StationType(int value) { super(value); }
    }

    @Sequence
    public static class ReferencePosition {
        Latitude latitude;
        Longitude longitude;
        PosConfidenceEllipse positionConfidenceEllipse;
        Altitude altitude;

        public ReferencePosition() {
            this(new Latitude(), new Longitude(), new PosConfidenceEllipse(), new Altitude());
        }
        public ReferencePosition(Latitude latitude,
                Longitude longitude,
                PosConfidenceEllipse positionConfidenceEllipse,
                Altitude altitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.positionConfidenceEllipse = positionConfidenceEllipse;
            this.altitude = altitude;
        }
    }

    @Sequence
    public static class Heading {
        HeadingValue headingValue;
        HeadingConfidence headingConfidence;

        public Heading() { this(new HeadingValue(), new HeadingConfidence()); }
        public Heading(HeadingValue headingValue, HeadingConfidence headingConfidence) {
            this.headingValue = headingValue;
            this.headingConfidence = headingConfidence;
        }
    }

    @Sequence
    public static class Speed {
        SpeedValue speedValue;
        SpeedConfidence speedConfidence;

        public Speed() { this(new SpeedValue(), new SpeedConfidence()); }
        public Speed(SpeedValue speedValue, SpeedConfidence speedConfidence) {
            this.speedValue = speedValue;
            this.speedConfidence = speedConfidence;
        }
    }

    @IntRange(minValue = 0, maxValue = 16383)
    public static class SpeedValue extends Asn1Integer {
        public static final int standstill = 0;
        public static final int oneCentimeterPerSec = 1;
        public static final int unavailable = 16383;

        public SpeedValue() { this(unavailable); }
        public SpeedValue(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 127)
    public static class SpeedConfidence extends Asn1Integer {
        public static final int equalOrWithinOneCentimeterPerSec = 1;
        public static final int equalOrWithinOneMeterPerSec = 100;
        public static final int outOfRange = 126;
        public static final int unavailable = 127;

        public SpeedConfidence() { this(unavailable); }
        public SpeedConfidence(int value) { super(value); }
    }

    public static enum DriveDirection {
        FORWARD     (0),
        BACKWARD    (1),
        UNAVAILABLE (2);

        private final int value;
        public int value() { return value; }
        private DriveDirection(int value) { this.value = value; }
        public static DriveDirection fromCode(int value) {
            for (DriveDirection element : values()) { if (element.value() == value) { return element; } }
            throw new IllegalArgumentException("Can't find element in enum " +
                    DriveDirection.class.getName() + " for code " + value);
        }
    }

    @Sequence
    public static class VehicleLength {
        VehicleLengthValue vehicleLengthValue;
        VehicleLengthConfidenceIndication vehicleLengthConfidenceIndication;

        public VehicleLength() {
            this(new VehicleLengthValue(), VehicleLengthConfidenceIndication.unavailable);
        }

        public VehicleLength(
                VehicleLengthValue vehicleLengthValue,
                VehicleLengthConfidenceIndication vehicleLengthConfidenceIndication) {
            this.vehicleLengthValue = vehicleLengthValue;
            this.vehicleLengthConfidenceIndication = vehicleLengthConfidenceIndication;
        }
    }

    @IntRange(minValue = 1, maxValue = 1023)
    public static class VehicleLengthValue extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int outOfRange = 1022;
        public static final int unavailable = 1023;

        public VehicleLengthValue() { this(unavailable); }
        public VehicleLengthValue(int value) { super(value); }
    }

    public static enum VehicleLengthConfidenceIndication {
        noTrailerPresent(0),
        trailerPresentWithKnownLength(1),
        trailerPresentWithUnknownLength(2),
        trailerPresenceIsUnknown(3),
        unavailable(4);

        private final int value;
        public int value() { return value; }
        private VehicleLengthConfidenceIndication(int value) { this.value = value; }
    }

    @IntRange(minValue = 1, maxValue = 62)
    public static class VehicleWidth extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int outOfRange = 61;
        public static final int unavailable = 62;

        public VehicleWidth() { this(unavailable); }
        public VehicleWidth(int value) { super(value); }
    }

    @Sequence
    public static class LongitudinalAcceleration {
        LongitudinalAccelerationValue longitudinalAccelerationValue;
        AccelerationConfidence longitudinalAccelerationConfidence;

        public LongitudinalAcceleration() { this(new LongitudinalAccelerationValue(), new AccelerationConfidence()); }
        public LongitudinalAcceleration(LongitudinalAccelerationValue longitudinalAccelerationValue,
                AccelerationConfidence longitudinalAccelerationConfidence) {
            this.longitudinalAccelerationValue = longitudinalAccelerationValue;
            this.longitudinalAccelerationConfidence = longitudinalAccelerationConfidence;
        }
    }

    @IntRange(minValue = -160, maxValue = 161)
    public static class LongitudinalAccelerationValue extends Asn1Integer {
        public static final int pointOneMeterPerSecSquaredForward = 1;
        public static final int pointOneMeterPerSecSquaredBackward = -1;
        public static final int unavailable = 161;

        public LongitudinalAccelerationValue() { this(unavailable); }
        public LongitudinalAccelerationValue(int value) { super(value); }
    }

    @IntRange(minValue = 0, maxValue = 102)
    public static class AccelerationConfidence extends Asn1Integer {
        public static final int pointOneMeterPerSecSquared = 1;
        public static final int outOfRange = 101;
        public static final int unavailable = 102;

        public AccelerationConfidence() { this(unavailable); }
        public AccelerationConfidence(int value) { super(value); }
    }

    @Sequence
    public static class Curvature {
        CurvatureValue curvatureValue;
        CurvatureConfidence curvatureConfidence;

        public Curvature() { this( new CurvatureValue(), CurvatureConfidence.unavailable ); }
        public Curvature(CurvatureValue curvatureValue, CurvatureConfidence curvatureConfidence) {
            this.curvatureValue = curvatureValue;
            this.curvatureConfidence = curvatureConfidence;
        }
    }

    @IntRange(minValue = -30000, maxValue = 30001)
    public static class CurvatureValue extends Asn1Integer {
        public static final int straight = 0;
        public static final int reciprocalOf1MeterRadiusToRight = -30000;
        public static final int reciprocalOf1MeterRadiusToLeft = 30000;
        public static final int unavailable = 30001;

        public CurvatureValue() { this(unavailable); }
        public CurvatureValue(int value) { super(value); }
    }

    public static enum CurvatureConfidence {
        onePerMeter_0_00002 (0),
        onePerMeter_0_0001 (1),
        onePerMeter_0_0005 (2),
        onePerMeter_0_002 (3),
        onePerMeter_0_01 (4),
        onePerMeter_0_1 (5),
        outOfRange (6),
        unavailable (7);

        private final int value;
        public int value() { return value; }
        private CurvatureConfidence(int value) { this.value = value; }
    }

    @HasExtensionMarker
    public static enum CurvatureCalculationMode {
        yawRateUsed(0),
        yawRateNotUsed(1),
        unavailable(2);

        private final int value;
        public int value() { return value; }
        private CurvatureCalculationMode(int value) { this.value = value; }
    }

    @Sequence
    public static class YawRate {
        YawRateValue yawRateValue;
        YawRateConfidence yawRateConfidence;

        public YawRate() { this(new YawRateValue(), YawRateConfidence.unavailable); }
        public YawRate(YawRateValue yawRateValue, YawRateConfidence yawRateConfidence) {
            this.yawRateValue = yawRateValue;
            this.yawRateConfidence = yawRateConfidence;
        }
    }

    @IntRange(minValue = -32766, maxValue = 32767)
    public static class YawRateValue extends Asn1Integer {
        public static final int straight = 0;
        public static final int degSec_000_01ToRight = -1;
        public static final int degSec_000_01ToLeft = 1;
        public static final int unavailable = 32767;

        public YawRateValue() { this(unavailable); }
        public YawRateValue(int value) { super(value); }
    }

    public static enum YawRateConfidence {
        degSec_000_01 (0),
        degSec_000_05 (1),
        degSec_000_10 (2),
        degSec_001_00 (3),
        degSec_005_00 (4),
        degSec_010_00 (5),
        degSec_100_00 (6),
        outOfRange (7),
        unavailable (8);

        private final int value;
        public int value() { return value; }
        private YawRateConfidence(int value) { this.value = value; }
    }

    @Sequence
    public static class PosConfidenceEllipse {
        SemiAxisLength semiMajorConfidence;
        SemiAxisLength semiMinorConfidence;
        HeadingValue semiMajorOrientation;

        public PosConfidenceEllipse() { this(new SemiAxisLength(), new SemiAxisLength(), new HeadingValue()); }
        public PosConfidenceEllipse (
            SemiAxisLength semiMajorConfidence,
            SemiAxisLength semiMinorConfidence,
            HeadingValue semiMajorOrientation) {
            this.semiMajorConfidence = semiMajorConfidence;
            this.semiMinorConfidence = semiMinorConfidence;
            this.semiMajorOrientation = semiMajorOrientation;
        }
    }

    @IntRange(minValue = 0, maxValue = 4095)
    public static class SemiAxisLength extends Asn1Integer {
        public static final int oneCentimeter = 1;
        public static final int outOfRange = 4094;
        public static final int unavailable = 4095;

        public SemiAxisLength() { this(unavailable); }
        public SemiAxisLength(int value) { super(value); }
    }

    @Sequence
    public static class Altitude {
        AltitudeValue altitudeValue;
        AltitudeConfidence altitudeConfidence;

        public Altitude() { this(new AltitudeValue(), AltitudeConfidence.unavailable); }
        public Altitude(AltitudeValue altitudeValue,        AltitudeConfidence altitudeConfidence) {
            this.altitudeValue = altitudeValue;
            this.altitudeConfidence = altitudeConfidence;
        }
    }

    @IntRange(minValue = -100000, maxValue = 800001)
    public static class AltitudeValue extends Asn1Integer {
        public static final int referenceEllipsoidSurface = 0;
        public static final int oneCentimeter = 1;
        public static final int unavailable = 800001;

        public AltitudeValue() { this(unavailable); }
        public AltitudeValue(int value) { super(value); }
    }

    public static enum AltitudeConfidence {
        alt_000_01 (0),
        alt_000_02 (1),
        alt_000_05 (2),
        alt_000_10 (3),
        alt_000_20 (4),
        alt_000_50 (5),
        alt_001_00 (6),
        alt_002_00 (7),
        alt_005_00 (8),
        alt_010_00 (9),
        alt_020_00 (10),
        alt_050_00 (11),
        alt_100_00 (12),
        alt_200_00 (13),
        outOfRange (14),
        unavailable (15);

        private final int value;
        public int value() { return value; }
        private AltitudeConfidence(int value) { this.value = value; }
    }

    @IntRange(minValue = 0, maxValue = 3601)
    public static class HeadingValue extends Asn1Integer {
        public static final int wgs84North = 0;
        public static final int wgs84East = 900;
        public static final int wgs84South = 1800;
        public static final int wgs84West = 2700;
        public static final int unavailable = 3601;

        public HeadingValue() { this(unavailable); }
        public HeadingValue(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 127)
    public static class HeadingConfidence extends Asn1Integer {
        public static final int equalOrWithinZeroPointOneDegree = 1;
        public static final int equalOrWithinOneDegree = 10;
        public static final int outOfRange = 126;
        public static final int unavailable = 127;

        public HeadingConfidence() { this(unavailable); }
        public HeadingConfidence(int value) { super(value); }
    }

    @IntRange(minValue = -900000000, maxValue = 900000001)
    public static class Latitude extends Asn1Integer {
        public static final int oneMicrodegreeNorth = 10;
        public static final int oneMicrodegreeSouth = -10;
        public static final int unavailable = 900000001;

        public Latitude() { this(unavailable); }
        public Latitude(int value) { super(value); }
    }

    @IntRange(minValue = -1800000000, maxValue = 1800000001)
    public static class Longitude extends Asn1Integer {
        public static final int oneMicrodegreeEast = 10;
        public static final int oneMicrodegreeWest = -10;
        public static final int unavailable = 1800000001;

        public Longitude() { this(unavailable); }
        public Longitude(int value) { super(value); }
    }

    @Bitstring
    @FixedSize(7)
    public static class AccelerationControl {
        boolean brakePedalEngaged; // Bit 0.
        boolean gasPedalEngaged; // Bit 1.
        boolean emergencyBrakeEngaged; // Bit 2.
        boolean collisionWarningEngaged; // Bit 3.
        boolean accEngaged; // Bit 4.
        boolean cruiseControlEngaged; // Bit 5.
        boolean speedLimiterEngaged; // Bit 6.

        protected AccelerationControl() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private AccelerationControl val = new AccelerationControl();
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public AccelerationControl create() { created = true; return val; }
            private Builder() {}

            public Builder brakePedalEngaged(boolean brakePedalEngaged) { checkCreated(); val.brakePedalEngaged = brakePedalEngaged; return this; }
            public Builder gasPedalEngaged(boolean gasPedalEngaged) { checkCreated(); val.gasPedalEngaged = gasPedalEngaged; return this; }
            public Builder emergencyBrakeEngaged(boolean emergencyBrakeEngaged) { checkCreated(); val.emergencyBrakeEngaged = emergencyBrakeEngaged; return this; }
            public Builder collisionWarningEngaged(boolean collisionWarningEngaged) { checkCreated(); val.collisionWarningEngaged = collisionWarningEngaged; return this; }
            public Builder accEngaged(boolean accEngaged) { checkCreated(); val.accEngaged = accEngaged; return this; }
            public Builder cruiseControlEngaged(boolean cruiseControlEngaged) { checkCreated(); val.cruiseControlEngaged = cruiseControlEngaged; return this; }
            public Builder speedLimiterEngaged(boolean speedLimiterEngaged) { checkCreated(); val.speedLimiterEngaged = speedLimiterEngaged; return this; }
        }
    }

    @IntRange(minValue = -1, maxValue = 14)
    public static class LanePosition extends Asn1Integer {
        public static final int offTheRoad=-1;
        public static final int hardShoulder=0;
        public static final int outermostDrivingLane=1;
        public static final int secondLaneFromOutside=2;

        public LanePosition() { this(outermostDrivingLane); }
        public LanePosition(int value) { super(value); }
    }

    @Sequence
    public static class SteeringWheelAngle {
        SteeringWheelAngleValue steeringWheelAngleValue;
        SteeringWheelAngleConfidence steeringWheelAngleConfidence;

        public SteeringWheelAngle() { this(new SteeringWheelAngleValue(),
                new SteeringWheelAngleConfidence()); }

        public SteeringWheelAngle(
                SteeringWheelAngleValue steeringWheelAngleValue,
                SteeringWheelAngleConfidence steeringWheelAngleConfidence) {
            this.steeringWheelAngleValue = steeringWheelAngleValue;
            this.steeringWheelAngleConfidence = steeringWheelAngleConfidence;
        }
    }

    @IntRange(minValue = -511, maxValue = 512)
    public static class SteeringWheelAngleValue extends Asn1Integer {
        public static final int straight = 0;
        public static final int onePointFiveDegreesToRight = -1;
        public static final int onePointFiveDegreesToLeft = 1;

        public SteeringWheelAngleValue() { this(straight); }
        public SteeringWheelAngleValue(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 127)
    public static class SteeringWheelAngleConfidence extends Asn1Integer {
        public static final int equalOrWithinOnePointFiveDegree = 1;
        public static final int outOfRange = 126;
        public static final int unavailable = 127;

        public SteeringWheelAngleConfidence() { this(unavailable); }
        public SteeringWheelAngleConfidence(int value) { super(value); }
    }

    @Sequence
    public static class LateralAcceleration {
        LateralAccelerationValue lateralAccelerationValue;
        AccelerationConfidence lateralAccelerationConfidence;

        public LateralAcceleration() { this(new LateralAccelerationValue(), new AccelerationConfidence()); }
        public LateralAcceleration(LateralAccelerationValue lateralAccelerationValue,
                AccelerationConfidence lateralAccelerationConfidence) {
            this.lateralAccelerationValue = lateralAccelerationValue;
            this.lateralAccelerationConfidence = lateralAccelerationConfidence;
        }
    }

    @IntRange(minValue = -160, maxValue = 161)
    public static class LateralAccelerationValue extends Asn1Integer {
        public static final int pointOneMeterPerSecSquaredToRight = -1;
        public static final int pointOneMeterPerSecSquaredToLeft = 1;
        public static final int unavailable = 161;

        public LateralAccelerationValue() { this(unavailable); }
        public LateralAccelerationValue(int value) { super(value); }
    }

    @Sequence
    public static class VerticalAcceleration {
        VerticalAccelerationValue verticalAccelerationValue;
        AccelerationConfidence verticalAccelerationConfidence;
        public VerticalAcceleration() { this(new VerticalAccelerationValue(), new AccelerationConfidence()); }
        public VerticalAcceleration(VerticalAccelerationValue verticalAccelerationValue,
                AccelerationConfidence verticalAccelerationConfidence) {
            this.verticalAccelerationValue = verticalAccelerationValue;
            this.verticalAccelerationConfidence = verticalAccelerationConfidence;
        }
    }

    @IntRange(minValue = -160, maxValue = 161)
    public static class VerticalAccelerationValue extends Asn1Integer {
        public static final int pointOneMeterPerSecSquaredUp = 1;
        public static final int pointOneMeterPerSecSquaredDown = -1;
        public static final int unavailable = 161;

        public VerticalAccelerationValue() { this(unavailable); }
        public VerticalAccelerationValue(int value) { super(value); }
    }

    @IntRange(minValue = 0, maxValue = 7)
    public static class PerformanceClass extends Asn1Integer {
        public static final int unavailable = 0;
        public static final int performanceClassA = 1;
        public static final int performanceClassB = 2;

        public PerformanceClass() { this(unavailable); }
        public PerformanceClass(int value) { super(value); }
    }

    @Sequence
    public static class CenDsrcTollingZone {
        Latitude protectedZoneLatitude;
        Longitude protectedZoneLongitude;
        @Asn1Optional ProtectedZoneID cenDsrcTollingZoneID;

        public CenDsrcTollingZone() { this(new Latitude(), new Longitude(), null); }
        public CenDsrcTollingZone(Latitude protectedZoneLatitude, Longitude protectedZoneLongitude,
                ProtectedZoneID cenDsrcTollingZoneID) {
            this.protectedZoneLatitude = protectedZoneLatitude;
            this.protectedZoneLongitude = protectedZoneLongitude;
            this.cenDsrcTollingZoneID = cenDsrcTollingZoneID;
        }
    }

    @IntRange(minValue = 0, maxValue = 134217727)
    public static class ProtectedZoneID extends Asn1Integer { }

    @Choice
    @HasExtensionMarker
    public static class LowFrequencyContainer {
        BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency;

        public LowFrequencyContainer() { this(new BasicVehicleContainerLowFrequency()); }
        public LowFrequencyContainer(BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency) {
            this.basicVehicleContainerLowFrequency = basicVehicleContainerLowFrequency;
        }
    }

    @Sequence
    public static class BasicVehicleContainerLowFrequency {
        VehicleRole vehicleRole;
        ExteriorLights exteriorLights;
        PathHistory pathHistory;

        public BasicVehicleContainerLowFrequency() {
            this(VehicleRole.default_, new ExteriorLights(), new PathHistory());
        }

        public BasicVehicleContainerLowFrequency(
                VehicleRole vehicleRole,
                ExteriorLights exteriorLights,
                PathHistory pathHistory) {
            this.vehicleRole = vehicleRole;
            this.exteriorLights = exteriorLights;
            this.pathHistory = pathHistory;
        }
    }

    public static enum VehicleRole {
        default_(0),
        publicTransport(1),
        specialTransport(2),
        dangerousGoods(3),
        roadWork(4),
        rescue(5),
        emergency(6),
        safetyCar(7),
        agriculture(8),
        commercial(9),
        military(10),
        roadOperator(11),
        taxi(12),
        reserved1(13),
        reserved2(14),
        reserved3(15);

        private final int value;
        public int value() { return value; }
        private VehicleRole(int value) { this.value = value; }
        public static VehicleRole fromCode(int value) {
            for (VehicleRole element : values()) { if (element.value() == value) { return element; } }
            throw new IllegalArgumentException("Can't find element in enum " +
                    VehicleRole.class.getName() + " for code " + value);
        }

    }

    @Bitstring
    @FixedSize(8)
    public static class ExteriorLights {
        boolean lowBeamHeadlightsOn;  // Bit 0.
        boolean highBeamHeadlightsOn;  // Bit 1.
        boolean leftTurnSignalOn;  // Bit 2.
        boolean rightTurnSignalOn;  // Bit 3.
        boolean daytimeRunningLightsOn;  // Bit 4.
        boolean reverseLightOn;  // Bit 5.
        boolean fogLightOn;  // Bit 6.
        boolean parkingLightsOn;  // Bit 7.

        protected ExteriorLights() {}

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private ExteriorLights val = new ExteriorLights();
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public ExteriorLights create() { created = true; return val; }

            private Builder() { }

            public Builder lowBeamHeadlightsOn(boolean lowBeamHeadlightsOn) { checkCreated(); val.lowBeamHeadlightsOn = lowBeamHeadlightsOn; return this; }
            public Builder highBeamHeadlightsOn(boolean highBeamHeadlightsOn) { checkCreated(); val.highBeamHeadlightsOn = highBeamHeadlightsOn; return this; }
            public Builder leftTurnSignalOn(boolean leftTurnSignalOn) { checkCreated(); val.leftTurnSignalOn = leftTurnSignalOn; return this; }
            public Builder rightTurnSignalOn(boolean rightTurnSignalOn) { checkCreated(); val.rightTurnSignalOn = rightTurnSignalOn; return this; }
            public Builder daytimeRunningLightsOn(boolean daytimeRunningLightsOn) { checkCreated(); val.daytimeRunningLightsOn = daytimeRunningLightsOn; return this; }
            public Builder reverseLightOn(boolean reverseLightOn) { checkCreated(); val.reverseLightOn = reverseLightOn; return this; }
            public Builder fogLightOn(boolean fogLightOn) { checkCreated(); val.fogLightOn = fogLightOn; return this; }
            public Builder parkingLightsOn(boolean parkingLightsOn) { checkCreated(); val.parkingLightsOn = parkingLightsOn; return this; }
        }

    }

    @Sequence
    @HasExtensionMarker
    public static class RSUContainerHighFrequency {
        @Asn1Optional ProtectedCommunicationZonesRSU protectedCommunicationZonesRSU;
    }

    @SizeRange(minValue = 0, maxValue = 40)
    public static class PathHistory extends Asn1SequenceOf<PathPoint> {
        public PathHistory(PathPoint... coll) {
            super(Arrays.asList(coll));
        }
    }

    @SizeRange(minValue = 1, maxValue = 16)
    public static class ProtectedCommunicationZonesRSU extends Asn1SequenceOf<ProtectedCommunicationZone> {
        public ProtectedCommunicationZonesRSU(ProtectedCommunicationZone... coll) {
            super(Arrays.asList(coll));
        }
    }

    @Sequence
    public static class ProtectedCommunicationZone {
        ProtectedZoneType protectedZoneType;
        @Asn1Optional TimestampIts expiryTime;
        Latitude protectedZoneLatitude;
        Longitude protectedZoneLongitude;
        @Asn1Optional ProtectedZoneRadius protectedZoneRadius;
        @Asn1Optional ProtectedZoneID protectedZoneID;
    }

    @HasExtensionMarker
    public static enum ProtectedZoneType {
        cenDsrcTolling (0);

        private final int value;
        public int value() { return value; }
        private ProtectedZoneType(int value) { this.value = value; }
    }

    @IntRange(minValue = 0, maxValue = 4398046511103L)
    public static class TimestampIts extends Asn1Integer {
        public static final int utcStartOf2004 = 0;
        public static final int oneMillisecAfterUTCStartOf2004 = 1;

        public TimestampIts() { this(utcStartOf2004); }
        public TimestampIts(long value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 255, hasExtensionMarker = true)
    public static class ProtectedZoneRadius extends Asn1Integer {
        public static final int oneMeter = 1;

        public ProtectedZoneRadius() { this(oneMeter); }
        public ProtectedZoneRadius(int value) { super(value); }
    }

    @Sequence
    public static class PathPoint {
        DeltaReferencePosition pathPosition;
        @Asn1Optional PathDeltaTime pathDeltaTime;

        public PathPoint() { this(new DeltaReferencePosition(), null); }
        public PathPoint(DeltaReferencePosition pathPosition, PathDeltaTime pathDeltaTime) {
            this.pathPosition = pathPosition;
            this.pathDeltaTime = pathDeltaTime;
        }
    }

    @Sequence
    public static class DeltaReferencePosition {
        DeltaLatitude deltaLatitude;
        DeltaLongitude deltaLongitude;
        DeltaAltitude deltaAltitude;

        public DeltaReferencePosition() { this(new DeltaLatitude(), new DeltaLongitude(), new DeltaAltitude()); }
        public DeltaReferencePosition(DeltaLatitude deltaLatitude,
                DeltaLongitude deltaLongitude,
                DeltaAltitude deltaAltitude) {
            this.deltaLatitude = deltaLatitude;
            this.deltaLongitude = deltaLongitude;
            this.deltaAltitude = deltaAltitude;
        }
    }

    @IntRange(minValue = -131071, maxValue = 131072)
    public static class DeltaLatitude extends Asn1Integer {
        public static final int oneMicrodegreeNorth = 10;
        public static final int oneMicrodegreeSouth = -10;
        public static final int unavailable = 131072;

        public DeltaLatitude() { this(unavailable); }
        public DeltaLatitude(int value) { super(value); }
    }

    @IntRange(minValue = -131071, maxValue = 131072)
    public static class DeltaLongitude extends Asn1Integer {
        public static final int oneMicrodegreeEast = 10;
        public static final int oneMicrodegreeWest = -10;
        public static final int unavailable = 131072;

        public DeltaLongitude() { this(unavailable); }
        public DeltaLongitude(int value) { super(value); }
    }

    @IntRange(minValue = -12700, maxValue = 12800)
    public static class DeltaAltitude extends Asn1Integer {
        public static final int oneCentimeterUp = 1;
        public static final int oneCentimeterDown = -1;
        public static final int unavailable = 12800;

        public DeltaAltitude() { this(unavailable); }
        public DeltaAltitude(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 65535, hasExtensionMarker = true)
    public static class PathDeltaTime extends Asn1Integer {
        public static final int tenMilliSecondsInPast = 1;

        public PathDeltaTime() { this(tenMilliSecondsInPast); }
        public PathDeltaTime(int value) { super(value); }
    }

    @Choice
    @HasExtensionMarker
    public static class SpecialVehicleContainer {
        PublicTransportContainer publicTransportContainer;
        SpecialTransportContainer specialTransportContainer;
        DangerousGoodsContainer dangerousGoodsContainer;
        RoadWorksContainerBasic roadWorksContainerBasic;
        RescueContainer rescueContainer;
        EmergencyContainer emergencyContainer;
        SafetyCarContainer safetyCarContainer;

        public SpecialVehicleContainer() { this(new PublicTransportContainer()); }

        public SpecialVehicleContainer(PublicTransportContainer publicTransportContainer) { this.publicTransportContainer = publicTransportContainer; }
        public SpecialVehicleContainer(SpecialTransportContainer specialTransportContainer) { this.specialTransportContainer = specialTransportContainer; }
        public SpecialVehicleContainer(DangerousGoodsContainer dangerousGoodsContainer) { this.dangerousGoodsContainer = dangerousGoodsContainer; }
        public SpecialVehicleContainer(RoadWorksContainerBasic roadWorksContainerBasic) { this.roadWorksContainerBasic = roadWorksContainerBasic; }
        public SpecialVehicleContainer(RescueContainer rescueContainer) { this.rescueContainer = rescueContainer; }
        public SpecialVehicleContainer(EmergencyContainer emergencyContainer) { this.emergencyContainer = emergencyContainer; }
        public SpecialVehicleContainer(SafetyCarContainer safetyCarContainer) { this.safetyCarContainer = safetyCarContainer; }
    }

    @Sequence
    public static class PublicTransportContainer {
        boolean embarkationStatus;
        @Asn1Optional PtActivation ptActivation;
        public PublicTransportContainer() { this(false); }
        public PublicTransportContainer(boolean embarkationStatus) {
            this(embarkationStatus, null);
        }
        public PublicTransportContainer(boolean embarkationStatus, PtActivation ptActivation) {
            this.embarkationStatus = embarkationStatus;
            this.ptActivation = ptActivation;
        }
    }

    @Sequence
    public static class PtActivation {
        PtActivationType ptActivationType;
        PtActivationData ptActivationData;

        public PtActivation() { this(new PtActivationType(), new PtActivationData()); }
        public PtActivation(PtActivationType ptActivationType, PtActivationData ptActivationData) {
            this.ptActivationType = ptActivationType;
            this.ptActivationData = ptActivationData;
        }
    }

    @IntRange(minValue = 0, maxValue = 255)
    public static class PtActivationType extends Asn1Integer {
        public static final int undefinedCodingType = 0;
        public static final int r09_16CodingType = 1;
        public static final int vdv_50149CodingType = 2;

        public PtActivationType() { this(undefinedCodingType); }
        public PtActivationType(int value) { super(value); }
    }

    @OctetString
    @SizeRange(minValue = 1, maxValue = 20)
    public static class PtActivationData extends Asn1SequenceOf<Byte> {
        public PtActivationData() {
            this(new byte[] {0});  // min size is 1.
        }
        public PtActivationData(byte... coll) {
            this(boxed(coll));
        }
        private PtActivationData(Byte... coll) {
            this(Arrays.asList(coll));
        }
        public PtActivationData(Collection<Byte> coll) {
            super(coll);
        }

        private static Byte[] boxed(byte... coll) {
            Byte[] boxedArray = new Byte[coll.length];
            for (int i = 0; i < coll.length; i++) { boxedArray[i] = coll[i]; }
            return boxedArray;
        }
    }

    @Sequence
    public static class SpecialTransportContainer {
        SpecialTransportType specialTransportType;
        LightBarSirenInUse lightBarSirenInUse;

        public SpecialTransportContainer() { this (new SpecialTransportType(), new LightBarSirenInUse()); }
        public SpecialTransportContainer(SpecialTransportType specialTransportType,
        LightBarSirenInUse lightBarSirenInUse) {
            this.specialTransportType = specialTransportType;
            this.lightBarSirenInUse = lightBarSirenInUse;
        }
    }

    @Bitstring
    @FixedSize(4)
    public static class SpecialTransportType {
        boolean heavyLoad;     // Bit 0
        boolean excessWidth;   // Bit 1
        boolean excessLength;  // Bit 2
        boolean excessHeight;  // Bit 3
    }

    @Sequence
    public static class DangerousGoodsContainer {
        DangerousGoodsBasic dangerousGoodsBasic;

        public DangerousGoodsContainer() { this (DangerousGoodsBasic.defaultValue()); }
        public DangerousGoodsContainer(DangerousGoodsBasic dangerousGoodsBasic) {
            this.dangerousGoodsBasic = dangerousGoodsBasic;
        }
    }

    public static enum DangerousGoodsBasic {
        explosives1(0),
        explosives2(1),
        explosives3(2),
        explosives4(3),
        explosives5(4),
        explosives6(5),
        flammableGases(6),
        nonFlammableGases(7),
        toxicGases(8),
        flammableLiquids(9),
        flammableSolids(10),
        substancesLiableToSpontaneousCombustion(11),
        substancesEmittingFlammableGasesUponContactWithWater(12),
        oxidizingSubstances(13),
        organicPeroxides(14),
        toxicSubstances(15),
        infectiousSubstances(16),
        radioactiveMaterial(17),
        corrosiveSubstances(18),
        miscellaneousDangerousSubstances(19);

        private final int value;
        public int value() { return value; }
        private DangerousGoodsBasic(int value) { this.value = value; }
        public static DangerousGoodsBasic defaultValue() { return explosives1; }
        public static DangerousGoodsBasic fromCode(int value) {
            for (DangerousGoodsBasic element : values()) { if (element.value() == value) { return element; } }
            throw new IllegalArgumentException("Can't find element in enum " +
                    DangerousGoodsBasic.class.getName() + " for code " + value);
        }

    }

    @Sequence
    public static class RoadWorksContainerBasic {
        @Asn1Optional RoadworksSubCauseCode roadworksSubCauseCode;
        LightBarSirenInUse lightBarSirenInUse;
        @Asn1Optional ClosedLanes closedLanes;

        public RoadWorksContainerBasic() { this(new LightBarSirenInUse()); }
        public RoadWorksContainerBasic(LightBarSirenInUse lightBarSirenInUse) {
            this(null, lightBarSirenInUse, null);
        }
        public RoadWorksContainerBasic(RoadworksSubCauseCode roadworksSubCauseCode,
                LightBarSirenInUse lightBarSirenInUse,
                ClosedLanes closedLanes) {
            this.roadworksSubCauseCode = roadworksSubCauseCode;
            this.lightBarSirenInUse = lightBarSirenInUse;
            this.closedLanes = closedLanes;
        }
    }

    @IntRange(minValue = 0, maxValue = 255)
    public static class RoadworksSubCauseCode extends Asn1Integer {
        public static final int unavailable = 0;
        public static final int majorRoadworks = 1;
        public static final int roadMarkingWork = 2;
        public static final int slowMovingRoadMaintenance = 3;
        public static final int shortTermStationaryRoadworks = 4;
        public static final int streetCleaning = 5;
        public static final int winterService = 6;

        public RoadworksSubCauseCode() { this(unavailable); }
        public RoadworksSubCauseCode(int value) { super(value); }
    }

    @Bitstring
    @FixedSize(2)
    public static class LightBarSirenInUse {
        boolean lightBarActivated;  // Bit 0.
        boolean sirenActivated;  // Bit 1.

        public LightBarSirenInUse() {}
        public LightBarSirenInUse(boolean lightBarActivated, boolean sirenActivated) {
            this.lightBarActivated = lightBarActivated;
            this.sirenActivated = sirenActivated;
        }
    }

    @Sequence
    @HasExtensionMarker
    public static class ClosedLanes {
        @Asn1Optional HardShoulderStatus hardShoulderStatus;
        DrivingLaneStatus drivingLaneStatus;

        public ClosedLanes() { this(new DrivingLaneStatus()); }
        public ClosedLanes(DrivingLaneStatus drivingLaneStatus) { this(null, drivingLaneStatus); }
        public ClosedLanes(HardShoulderStatus hardShoulderStatus,
                DrivingLaneStatus drivingLaneStatus) {
            this.hardShoulderStatus = hardShoulderStatus;
            this.drivingLaneStatus = drivingLaneStatus;
        }
    }

    public static enum HardShoulderStatus {
        vailableForStopping(0),
        closed(1),
        availableForDriving(2);

        private final int value;
        public int value() { return value; }
        private HardShoulderStatus(int value) { this.value = value; }
    }

    @Bitstring
    @SizeRange(minValue = 1, maxValue = 14)
    public static class DrivingLaneStatus extends Asn1VarSizeBitstring {

        public boolean outermostLaneClosed() { return getBit(1); }
        public boolean secondLaneFromOutsideClosed() { return getBit(2); }

        public DrivingLaneStatus(Collection<Boolean> coll) {
            super(coll);
        }

        public DrivingLaneStatus(Boolean... coll) {
            super(Arrays.asList(coll));
        }

        protected DrivingLaneStatus() { super(new ArrayList<Boolean>()); }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private static final int minSize = Builder.class.getDeclaringClass().getAnnotation(SizeRange.class).minValue();
            private static final int maxSize = Builder.class.getDeclaringClass().getAnnotation(SizeRange.class).maxValue();
            private final DrivingLaneStatus val;
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public DrivingLaneStatus create() {
                if (val.size() < 1) {
                    throw new IllegalStateException("Too few elements: have " + val.size() +
                            ", min needed " + minSize);
                }
                created = true;
                return val;
            }

            private Builder() {
                val = new DrivingLaneStatus();
            }

            public Builder setBit(int bitIndex, boolean value) {
                if (bitIndex >= maxSize) {
                    throw new IllegalArgumentException("Index " + bitIndex + " is out of range 0.."
                            + (maxSize - 1) + " for " + Builder.class.getDeclaringClass());
                }
                checkCreated();
                val.setBit(bitIndex, value);
                return this;
            }

            public Builder outermostLaneClosed(boolean outermostLaneClosed) {
                setBit(1, outermostLaneClosed);
                return this;
            }
            public Builder secondLaneFromOutsideClosed(boolean secondLaneFromOutsideClosed) {
                setBit(2, secondLaneFromOutsideClosed);
                return this;
            }
        }

    }

    @Sequence
    public static class RescueContainer {
        LightBarSirenInUse lightBarSirenInUse;

        public RescueContainer() { this(new LightBarSirenInUse()); }
        public RescueContainer(LightBarSirenInUse lightBarSirenInUse) {
            this.lightBarSirenInUse = lightBarSirenInUse;
        }
    }

    @Sequence
    public static class EmergencyContainer {
        LightBarSirenInUse lightBarSirenInUse;
        @Asn1Optional CauseCode incidentIndication;
        @Asn1Optional EmergencyPriority emergencyPriority;

        public EmergencyContainer() { this(new LightBarSirenInUse()); }

        public EmergencyContainer(LightBarSirenInUse lightBarSirenInUse) {
            this(lightBarSirenInUse, null, null);
        }

        public EmergencyContainer(LightBarSirenInUse lightBarSirenInUse,
        CauseCode incidentIndication,
        EmergencyPriority emergencyPriority) {
            this.lightBarSirenInUse = lightBarSirenInUse;
            this.incidentIndication = incidentIndication;
            this.emergencyPriority = emergencyPriority;
        }
    }

    @Sequence
    public static class CauseCode {
        CauseCodeType causeCode;
        SubCauseCodeType subCauseCode;

        public CauseCode() { this(new CauseCodeType(), new SubCauseCodeType()); }
        public CauseCode(CauseCodeType causeCode,
        SubCauseCodeType subCauseCode) {
            this.causeCode = causeCode;
            this.subCauseCode = subCauseCode;
        }
    }


    @IntRange(minValue = 0, maxValue = 255)
    public static class CauseCodeType extends Asn1Integer {
        public static final int reserved = 0;
        public static final int trafficCondition = 1;
        public static final int accident = 2;
        public static final int roadworks = 3;
        public static final int adverseWeatherCondition_Adhesion = 6;
        public static final int hazardousLocation_SurfaceCondition = 9;
        public static final int hazardousLocation_ObstacleOnTheRoad = 10;
        public static final int hazardousLocation_AnimalOnTheRoad = 11;
        public static final int humanPresenceOnTheRoad = 12;
        public static final int wrongWayDriving = 14;
        public static final int rescueAndRecoveryWorkInProgress = 15;
        public static final int adverseWeatherCondition_ExtremeWeatherCondition = 17;
        public static final int adverseWeatherCondition_Visibility = 18;
        public static final int adverseWeatherCondition_Precipitation = 19;
        public static final int slowVehicle = 26;
        public static final int dangerousEndOfQueue = 27;
        public static final int vehicleBreakdown = 91;
        public static final int postCrash = 92;
        public static final int humanProblem = 93;
        public static final int stationaryVehicle = 94;
        public static final int emergencyVehicleApproaching = 95;
        public static final int hazardousLocation_DangerousCurve = 96;
        public static final int collisionRisk = 97;
        public static final int signalViolation = 98;
        public static final int dangerousSituation = 99;

        public CauseCodeType() { this(reserved); }
        public CauseCodeType(int value) { super(value); }
    }

    @IntRange(minValue = 0, maxValue = 255)
    public static class SubCauseCodeType extends Asn1Integer {
        public SubCauseCodeType() { this(0); }
        public SubCauseCodeType(int value) { super(value); }
    }

    @Bitstring
    @FixedSize(2)
    public static class EmergencyPriority {
        boolean requestForRightOfWay;  // Bit 0.
        boolean requestForFreeCrossingAtATrafficLight;  // Bit 1.
    }

    @Sequence
    public static class SafetyCarContainer {
        LightBarSirenInUse lightBarSirenInUse;
        @Asn1Optional CauseCode incidentIndication;
        @Asn1Optional TrafficRule trafficRule;
        @Asn1Optional SpeedLimit speedLimit;

        public SafetyCarContainer() { this(new LightBarSirenInUse()); }
        public SafetyCarContainer(LightBarSirenInUse lightBarSirenInUse) { this(lightBarSirenInUse, null, null, null); }
        public SafetyCarContainer(LightBarSirenInUse lightBarSirenInUse,
                CauseCode incidentIndication,
             TrafficRule trafficRule,
             SpeedLimit speedLimit) {
            this.lightBarSirenInUse = lightBarSirenInUse;
            this.incidentIndication = incidentIndication;
            this.trafficRule = trafficRule;
            this.speedLimit = speedLimit;
        }
    }

    @HasExtensionMarker
    public static enum TrafficRule {
        noPassing(0),
        noPassingForTrucks(1),
        passToRight(2),
        passToLeft(3);

        private final int value;
        public int value() { return value; }
        private TrafficRule(int value) { this.value = value; }
        public static TrafficRule defaultValue() { return noPassing; }
    }

    @IntRange(minValue = 1, maxValue = 255)
    public static class SpeedLimit extends Asn1Integer {
        public static final int oneKmPerHour = 1;
        public SpeedLimit() { this(oneKmPerHour); }
        public SpeedLimit(int value) { super(value); }
    }

    @Sequence
    public static class Denm {
        ItsPduHeader header;
        DecentralizedEnvironmentalNotificationMessage denm;

        public Denm() { this (new ItsPduHeader(new MessageId(MessageId.denm)), new DecentralizedEnvironmentalNotificationMessage()); }

        public Denm(ItsPduHeader header, DecentralizedEnvironmentalNotificationMessage denm) {
            this.header = header;
            this.denm = denm;
        }
    }

    @Sequence
    public static class DecentralizedEnvironmentalNotificationMessage {
        ManagementContainer management;
        @Asn1Optional Object situation;
        @Asn1Optional Object location;
        @Asn1Optional Object alacarte;

        public DecentralizedEnvironmentalNotificationMessage() { this(new ManagementContainer()); }

        public DecentralizedEnvironmentalNotificationMessage(ManagementContainer management) {
            this(management, null, null, null);
        }

        public DecentralizedEnvironmentalNotificationMessage(ManagementContainer management,
                Object situation,
                Object location,
                Object alacarte) {
            this.management = management;
            this.situation = situation;
            this.location = location;
            this.alacarte = alacarte;
        }
    }

    @Sequence
    @HasExtensionMarker
    public static class ManagementContainer {
        ActionID actionID;
        TimestampIts detectionTime;
        TimestampIts referenceTime;
        @Asn1Optional Termination termination;
        ReferencePosition eventPosition;
        @Asn1Optional RelevanceDistance relevanceDistance;
        @Asn1Optional RelevanceTrafficDirection relevanceTrafficDirection;
        @Asn1Optional ValidityDuration validityDuration = defaultValidity;
        @Asn1Optional TransmissionInterval transmissionInterval;
        StationType stationType;

        protected ManagementContainer() {
            this.actionID = new ActionID();
            this.detectionTime = new TimestampIts();
            this.referenceTime = new TimestampIts();
            this.eventPosition = new ReferencePosition();
            this.stationType = new StationType();
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private ManagementContainer val = new ManagementContainer();
            private boolean created = false;
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public ManagementContainer create() { created = true; return val; }

            private Builder() { }

            public Builder actionID(ActionID actionID) { checkCreated(); val.actionID = actionID; return this; }
            public Builder detectionTime(TimestampIts detectionTime) { checkCreated(); val.detectionTime = detectionTime; return this; }
            public Builder referenceTime(TimestampIts referenceTime) { checkCreated(); val.referenceTime = referenceTime; return this; }
            public Builder termination(Termination termination) { checkCreated(); val.termination = termination; return this; }
            public Builder eventPosition(ReferencePosition eventPosition) { checkCreated(); val.eventPosition = eventPosition; return this; }
            public Builder relevanceDistance(RelevanceDistance relevanceDistance) { checkCreated(); val.relevanceDistance = relevanceDistance; return this; }
            public Builder relevanceTrafficDirection(RelevanceTrafficDirection relevanceTrafficDirection) { checkCreated(); val.relevanceTrafficDirection = relevanceTrafficDirection; return this; }
            public Builder validityDuration(ValidityDuration validityDuration) { checkCreated(); val.validityDuration = validityDuration; return this; }
            public Builder transmissionInterval(TransmissionInterval transmissionInterval) { checkCreated(); val.transmissionInterval = transmissionInterval; return this; }
            public Builder stationType(StationType stationType) { checkCreated(); val.stationType = stationType; return this; }
        }
    }


    @Sequence
    public static class ActionID {
        StationID originatingStationID;
        SequenceNumber sequenceNumber;

        public ActionID() { this (new StationID(), new SequenceNumber()); }

        public ActionID(StationID originatingStationID, SequenceNumber sequenceNumber) {
            this.originatingStationID = originatingStationID;
            this.sequenceNumber = sequenceNumber;
        }
    }

    public static enum RelevanceDistance {
        lessThan50m(0),
        lessThan100m(1),
        lessThan200m(2),
        lessThan500m(3),
        lessThan1000m(4),
        lessThan5km(5),
        lessThan10km(6),
        over10km(7);

        private final int value;
        public int value() { return value; }
        private RelevanceDistance(int value) { this.value = value; }
    }

    public static enum RelevanceTrafficDirection {
        allTrafficDirections(0),
        upstreamTraffic(1),
        downstreamTraffic(2),
        oppositeTraffic(3);

        private final int value;
        public int value() { return value; }
        private RelevanceTrafficDirection(int value) { this.value = value; }
    }

    @IntRange(minValue = 0, maxValue = 65535)
    public static class SequenceNumber extends Asn1Integer {
        public SequenceNumber() { this(0); }
        public SequenceNumber(int value) { super(value); }
    }

    @IntRange(minValue = 0, maxValue = 86400)
    public static class ValidityDuration extends Asn1Integer {
        public static final int timeOfDetection = 0;
        public static final int oneSecondAfterDetection = 1;

        public ValidityDuration() { this(0); }
        public ValidityDuration(int value) { super(value); }
    }

    public static final ValidityDuration defaultValidity = new ValidityDuration(600);

    @IntRange(minValue = 1, maxValue = 10000)
    public static class TransmissionInterval extends Asn1Integer {
        public static final int oneMilliSecond = 1;
        public static final int tenSeconds = 10000;

        public TransmissionInterval() { this(oneMilliSecond); }
        public TransmissionInterval(int value) { super(value); }
    }

    public static enum Termination {
        isCancellation(0),
        isNegation (1);

        private final int value;
        public int value() { return value; }
        private Termination(int value) { this.value = value; }
    }
}
