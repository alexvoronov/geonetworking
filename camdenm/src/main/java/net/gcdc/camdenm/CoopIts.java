package net.gcdc.camdenm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;

import net.gcdc.asn1.datatypes.Asn1AnonymousType;
import net.gcdc.asn1.datatypes.Asn1Integer;
import net.gcdc.asn1.datatypes.Asn1Optional;
import net.gcdc.asn1.datatypes.Asn1SequenceOf;
import net.gcdc.asn1.datatypes.Asn1String;
import net.gcdc.asn1.datatypes.Asn1VarSizeBitstring;
import net.gcdc.asn1.datatypes.Bitstring;
import net.gcdc.asn1.datatypes.CharacterRestriction;
import net.gcdc.asn1.datatypes.Choice;
import net.gcdc.asn1.datatypes.FixedSize;
import net.gcdc.asn1.datatypes.HasExtensionMarker;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.OctetString;
import net.gcdc.asn1.datatypes.RestrictedString;
import net.gcdc.asn1.datatypes.Sequence;
import net.gcdc.asn1.datatypes.SizeRange;
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

        public ItsPduHeader header(){ return this.header; }
        public CoopAwareness cam(){ return this.cam; }

        @Override public String toString() { return "CAM(" + header + ", " + cam + ")"; }

		public ItsPduHeader getHeader() {
			return header;
		}

		public CoopAwareness getCam() {
			return cam;
		}
    }

    @Sequence
    public static class ItsPduHeader {
        ProtocolVersion protocolVersion;
        MessageId messageID;
        StationID stationID;

        public StationID stationID(){ return this.stationID; }

        @Override public String toString() {
            return "Header(protocolVersion " + protocolVersion + ", messageID " + messageID +
                    ", stationID " + stationID + ")";
        }

        public ProtocolVersion getProtocolVersion() {
			return protocolVersion;
		}

		public MessageId getMessageID() {
			return messageID;
		}

		public StationID getStationID() {
			return stationID;
		}

		@Asn1AnonymousType
        @IntRange(minValue = 0, maxValue = 255)
        public static class ProtocolVersion extends Asn1Integer {
            public static final int currentVersion = 1;
            public ProtocolVersion() { this(currentVersion); }
            public ProtocolVersion(int value) { super(value); }
            public ProtocolVersion(long value) { super(value); }
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
        @Override public String toString() {
            return "CoopAwareness(generationDeltaTime=" + generationDeltaTime + ", "
                    + camParameters + ")";
        }
        GenerationDeltaTime generationDeltaTime;
        CamParameters camParameters;

        public GenerationDeltaTime generationDeltaTime(){ return this.generationDeltaTime; }
        public CamParameters camParameters(){ return this.camParameters; }

        public CoopAwareness() { this(new GenerationDeltaTime(), new CamParameters()); }
        public CoopAwareness(GenerationDeltaTime generationDeltaTime, CamParameters camParameters) {
            this.generationDeltaTime = generationDeltaTime;
            this.camParameters = camParameters;
        }
		public GenerationDeltaTime getGenerationDeltaTime() {
			return generationDeltaTime;
		}
		public CamParameters getCamParameters() {
			return camParameters;
		}
    }

    @IntRange(minValue = 0, maxValue = 65535)
    public static class GenerationDeltaTime extends Asn1Integer {
        public static final int oneMilliSec = 1;
        public static final int oneSecond = 1000;

        public GenerationDeltaTime() { this(100 * oneMilliSec); }
        public GenerationDeltaTime(int value) { super(value); }
    }

    @Sequence
    @HasExtensionMarker
    public static class CamParameters {
        @Override public String toString() {
            return "CamParameters(Basic: " + basicContainer + ", HF: "
                    + highFrequencyContainer + ", LF: " + lowFrequencyContainer
                    + ", Special: " + specialVehicleContainer + ")";
        }

        BasicContainer basicContainer;
        HighFrequencyContainer highFrequencyContainer;
        @Asn1Optional LowFrequencyContainer lowFrequencyContainer;
        @Asn1Optional SpecialVehicleContainer specialVehicleContainer;

        public BasicContainer basicContainer(){ return this.basicContainer; }
        public HighFrequencyContainer highFrequencyContainer(){ return this.highFrequencyContainer; }
        public LowFrequencyContainer lowFrequencyContainer(){ return this.lowFrequencyContainer; }
        public SpecialVehicleContainer specialVehicleContainer(){ return this.specialVehicleContainer; }

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

		public BasicContainer getBasicContainer() {
			return basicContainer;
		}

		public HighFrequencyContainer getHighFrequencyContainer() {
			return highFrequencyContainer;
		}

		public boolean hasLowFrequencyContainer(){
			return lowFrequencyContainer!= null;
		}
		
		public LowFrequencyContainer getLowFrequencyContainer() {
			return lowFrequencyContainer;
		}
		
		public boolean hasspecialVehicleContainer(){
			return specialVehicleContainer!= null;
		}
		
		public SpecialVehicleContainer getSpecialVehicleContainer() {
			return specialVehicleContainer;
		}
    }

    @Sequence
    @HasExtensionMarker
    public static class BasicContainer {
        StationType stationType;
        ReferencePosition referencePosition;

        public StationType stationType(){ return this.stationType; }
        public ReferencePosition referencePosition(){ return this.referencePosition; }
        
        public BasicContainer() { this(new StationType(), new ReferencePosition()); }
        public BasicContainer(StationType stationType, ReferencePosition referencePosition) {
            this.stationType = stationType;
            this.referencePosition = referencePosition;
        }
		public StationType getStationType() {
			return stationType;
		}
		public ReferencePosition getReferencePosition() {
			return referencePosition;
		}
    }

    @Choice
    @HasExtensionMarker
    public static class HighFrequencyContainer {
        @Override public String toString() {
            return "HighFrequencyContainer[CHOICE](Basic: "
                    + basicVehicleContainerHighFrequency + ", RSU: "
                    + rsuContainerHighFrequency + ")";
        }
        BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency;
        RSUContainerHighFrequency rsuContainerHighFrequency;

        public BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency(){ return this.basicVehicleContainerHighFrequency; }
        public RSUContainerHighFrequency rsuContainerHighFrequency(){ return this.rsuContainerHighFrequency; }

        public HighFrequencyContainer() { this(BasicVehicleContainerHighFrequency.builder().create()); }
        public HighFrequencyContainer(BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency) {
            this.basicVehicleContainerHighFrequency = basicVehicleContainerHighFrequency;
            this.rsuContainerHighFrequency = null;
        }
        public HighFrequencyContainer(RSUContainerHighFrequency rsuContainerHighFrequency) {
            this.basicVehicleContainerHighFrequency = null;
            this.rsuContainerHighFrequency = rsuContainerHighFrequency;
        }
        public boolean hasBasicVehicleContainerHighFrequency(){
        	return basicVehicleContainerHighFrequency != null;
        }
        
		public BasicVehicleContainerHighFrequency getBasicVehicleContainerHighFrequency() {
			return basicVehicleContainerHighFrequency;
		}
		
		public boolean hasRsuContainerHighFrequency(){
			return rsuContainerHighFrequency != null;
		}
		
		public RSUContainerHighFrequency getRsuContainerHighFrequency() {
			return rsuContainerHighFrequency;
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

        public Heading heading(){ return this.heading; }
        public Speed speed(){ return this.speed; }
        public DriveDirection driveDirection(){ return this.driveDirection; }
        public VehicleLength vehicleLength(){ return this.vehicleLength; }
        public VehicleWidth vehicleWidth(){ return this.vehicleWidth; }
        public LongitudinalAcceleration longitudinalAcceleration(){ return this.longitudinalAcceleration; }
        public Curvature curvature(){ return this.curvature; }
        public CurvatureCalculationMode curvatureCalculationMode(){ return this.curvatureCalculationMode; }
        public YawRate yawRate(){ return this.yawRate; }
        public AccelerationControl accelerationControl(){ return this.accelerationControl; }
        public LanePosition lanePosition(){ return this.lanePosition; }
        public SteeringWheelAngle steeringWheelAngle(){ return this.steeringWheelAngle; }
        public LateralAcceleration lateralAcceleration(){ return this.lateralAcceleration; }
        public VerticalAcceleration verticalAcceleration(){ return this.verticalAcceleration; }
        public PerformanceClass performanceClass(){ return this.performanceClass; }
        public CenDsrcTollingZone cenDsrcTollingZone(){ return this.cenDsrcTollingZone; }
        
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

		public Heading getHeading() {
			return heading;
		}

		public Speed getSpeed() {
			return speed;
		}

		public DriveDirection getDriveDirection() {
			return driveDirection;
		}

		public VehicleLength getVehicleLength() {
			return vehicleLength;
		}

		public VehicleWidth getVehicleWidth() {
			return vehicleWidth;
		}

		public LongitudinalAcceleration getLongitudinalAcceleration() {
			return longitudinalAcceleration;
		}

		public Curvature getCurvature() {
			return curvature;
		}

		public CurvatureCalculationMode getCurvatureCalculationMode() {
			return curvatureCalculationMode;
		}

		public YawRate getYawRate() {
			return yawRate;
		}
		public boolean hasAccelerationControl(){
			return accelerationControl != null;
		}
		public AccelerationControl getAccelerationControl() {
			return accelerationControl;
		}
		public boolean hasLanePosition(){
			return lanePosition != null;
		}
		public LanePosition getLanePosition() {
			return lanePosition;
		}
		public boolean hasSteeringWheelAngle(){
			return steeringWheelAngle != null;
		}

		public SteeringWheelAngle getSteeringWheelAngle() {
			return steeringWheelAngle;
		}

		public boolean hasLateralAcceleration(){
			return lateralAcceleration != null;
		}
		
		public LateralAcceleration getLateralAcceleration() {
			return lateralAcceleration;
		}
		
		public boolean hasVerticalAcceleration(){
			return verticalAcceleration != null;
		}

		public VerticalAcceleration getVerticalAcceleration() {
			return verticalAcceleration;
		}
		
		public boolean hasPerformanceClass(){
			return performanceClass != null;
		}

		public PerformanceClass getPerformanceClass() {
			return performanceClass;
		}
		
		public boolean hasCenDsrcTollingZone(){
			return cenDsrcTollingZone != null;
		}

		public CenDsrcTollingZone getCenDsrcTollingZone() {
			return cenDsrcTollingZone;
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

        public Latitude latitude(){ return this.latitude; }
        public Longitude longitude(){ return this.longitude; }
        public PosConfidenceEllipse positionConfidenceEllipse(){ return this.positionConfidenceEllipse; }
        public Altitude altitude(){ return this.altitude; }

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
		public Latitude getLatitude() {
			return latitude;
		}
		public Longitude getLongitude() {
			return longitude;
		}
		public PosConfidenceEllipse getPositionConfidenceEllipse() {
			return positionConfidenceEllipse;
		}
		public Altitude getAltitude() {
			return altitude;
		}
    }

    @Sequence
    public static class Heading {
        HeadingValue headingValue;
        HeadingConfidence headingConfidence;

        public HeadingValue headingValue(){ return this.headingValue; }
        public HeadingConfidence headingConfidence(){ return this.headingConfidence; }

        public Heading() { this(new HeadingValue(), new HeadingConfidence()); }
        public Heading(HeadingValue headingValue, HeadingConfidence headingConfidence) {
            this.headingValue = headingValue;
            this.headingConfidence = headingConfidence;
        }
		public HeadingValue getHeadingValue() {
			return headingValue;
		}
		public HeadingConfidence getHeadingConfidence() {
			return headingConfidence;
		}
    }

    @Sequence
    public static class Speed {
        SpeedValue speedValue;
        SpeedConfidence speedConfidence;

        public SpeedValue speedValue(){ return this.speedValue; }
        public SpeedConfidence speedConfidence(){ return this.speedConfidence; }

        public Speed() { this(new SpeedValue(), new SpeedConfidence()); }
        public Speed(SpeedValue speedValue, SpeedConfidence speedConfidence) {
            this.speedValue = speedValue;
            this.speedConfidence = speedConfidence;
        }
		public SpeedValue getSpeedValue() {
			return speedValue;
		}
		public SpeedConfidence getSpeedConfidence() {
			return speedConfidence;
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

        public VehicleLengthValue vehicleLengthValue(){ return this.vehicleLengthValue; }
        public VehicleLengthConfidenceIndication vehicleLengthConfidenceIndication(){ return this.vehicleLengthConfidenceIndication; }

        public VehicleLength() {
            this(new VehicleLengthValue(), VehicleLengthConfidenceIndication.unavailable);
        }

        public VehicleLength(
                VehicleLengthValue vehicleLengthValue,
                VehicleLengthConfidenceIndication vehicleLengthConfidenceIndication) {
            this.vehicleLengthValue = vehicleLengthValue;
            this.vehicleLengthConfidenceIndication = vehicleLengthConfidenceIndication;
        }

		public VehicleLengthValue getVehicleLengthValue() {
			return vehicleLengthValue;
		}

		public VehicleLengthConfidenceIndication getVehicleLengthConfidenceIndication() {
			return vehicleLengthConfidenceIndication;
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

        public LongitudinalAccelerationValue longitudinalAccelerationValue(){ return this.longitudinalAccelerationValue; }
        public AccelerationConfidence longitudinalAccelerationConfidence(){ return this.longitudinalAccelerationConfidence; }

        public LongitudinalAcceleration() { this(new LongitudinalAccelerationValue(), new AccelerationConfidence()); }
        public LongitudinalAcceleration(LongitudinalAccelerationValue longitudinalAccelerationValue,
                AccelerationConfidence longitudinalAccelerationConfidence) {
            this.longitudinalAccelerationValue = longitudinalAccelerationValue;
            this.longitudinalAccelerationConfidence = longitudinalAccelerationConfidence;
        }
		public LongitudinalAccelerationValue getLongitudinalAccelerationValue() {
			return longitudinalAccelerationValue;
		}
		public AccelerationConfidence getLongitudinalAccelerationConfidence() {
			return longitudinalAccelerationConfidence;
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

        public CurvatureValue curvatureValue(){ return this.curvatureValue; }
        public CurvatureConfidence curvatureConfidence(){ return this.curvatureConfidence; }

        public Curvature() { this( new CurvatureValue(), CurvatureConfidence.unavailable ); }
        public Curvature(CurvatureValue curvatureValue, CurvatureConfidence curvatureConfidence) {
            this.curvatureValue = curvatureValue;
            this.curvatureConfidence = curvatureConfidence;
        }
		public CurvatureValue getCurvatureValue() {
			return curvatureValue;
		}
		public CurvatureConfidence getCurvatureConfidence() {
			return curvatureConfidence;
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

        public YawRateValue yawRateValue(){ return this.yawRateValue; }
        public YawRateConfidence yawRateConfidence(){ return this.yawRateConfidence; }

        public YawRate() { this(new YawRateValue(), YawRateConfidence.unavailable); }
        public YawRate(YawRateValue yawRateValue, YawRateConfidence yawRateConfidence) {
            this.yawRateValue = yawRateValue;
            this.yawRateConfidence = yawRateConfidence;
        }
		public YawRateValue getYawRateValue() {
			return yawRateValue;
		}
		public YawRateConfidence getYawRateConfidence() {
			return yawRateConfidence;
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

        public SemiAxisLength semiMajorConfidence(){ return this.semiMajorConfidence; }
        public SemiAxisLength semiMinorConfidence(){ return this.semiMinorConfidence; }
        public HeadingValue semiMajorOrientation(){ return this.semiMajorOrientation; }

        public PosConfidenceEllipse() { this(new SemiAxisLength(), new SemiAxisLength(), new HeadingValue()); }
        public PosConfidenceEllipse (
            SemiAxisLength semiMajorConfidence,
            SemiAxisLength semiMinorConfidence,
            HeadingValue semiMajorOrientation) {
            this.semiMajorConfidence = semiMajorConfidence;
            this.semiMinorConfidence = semiMinorConfidence;
            this.semiMajorOrientation = semiMajorOrientation;
        }
		public SemiAxisLength getSemiMajorConfidence() {
			return semiMajorConfidence;
		}
		public SemiAxisLength getSemiMinorConfidence() {
			return semiMinorConfidence;
		}
		public HeadingValue getSemiMajorOrientation() {
			return semiMajorOrientation;
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

        public AltitudeValue altitudeValue(){ return this.altitudeValue; }
        public AltitudeConfidence altitudeConfidence(){ return this.altitudeConfidence; }

        public Altitude() { this(new AltitudeValue(), AltitudeConfidence.unavailable); }
        public Altitude(AltitudeValue altitudeValue,        AltitudeConfidence altitudeConfidence) {
            this.altitudeValue = altitudeValue;
            this.altitudeConfidence = altitudeConfidence;
        }
		public AltitudeValue getAltitudeValue() {
			return altitudeValue;
		}
		public AltitudeConfidence getAltitudeConfidence() {
			return altitudeConfidence;
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

        public boolean brakePedalEngaged(){ return this.brakePedalEngaged; }
        public boolean gasPedalEngaged(){ return this.gasPedalEngaged; }
        public boolean emergencyBrakeEngaged(){ return this.emergencyBrakeEngaged; }
        public boolean collisionWarningEngaged(){ return this.collisionWarningEngaged; }
        public boolean accEngaged(){ return this.accEngaged; }
        public boolean cruiseControlEngaged(){ return this.cruiseControlEngaged; }
        public boolean speedLimiterEngaged(){ return this.speedLimiterEngaged; }

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
        
            public Builder set(
            		boolean brakePedalEngaged,
            		boolean gasPedalEngaged,
            		boolean emergencyBrakeEngaged,
            		boolean collisionWarningEngaged,
            		boolean accEngaged,
            		boolean cruiseControlEngaged,
            		boolean speedLimiterEngaged
            		){
            	checkCreated();
            	val.brakePedalEngaged = brakePedalEngaged;
            	val.gasPedalEngaged = gasPedalEngaged;
            	val.emergencyBrakeEngaged = emergencyBrakeEngaged;
            	val.collisionWarningEngaged = collisionWarningEngaged;
            	val.accEngaged = accEngaged;
            	val.cruiseControlEngaged = cruiseControlEngaged;
            	val.speedLimiterEngaged = speedLimiterEngaged;
            	return this;
            }
        }

		public boolean isBrakePedalEngaged() {
			return brakePedalEngaged;
		}

		public boolean isGasPedalEngaged() {
			return gasPedalEngaged;
		}

		public boolean isEmergencyBrakeEngaged() {
			return emergencyBrakeEngaged;
		}

		public boolean isCollisionWarningEngaged() {
			return collisionWarningEngaged;
		}

		public boolean isAccEngaged() {
			return accEngaged;
		}

		public boolean isCruiseControlEngaged() {
			return cruiseControlEngaged;
		}

		public boolean isSpeedLimiterEngaged() {
			return speedLimiterEngaged;
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

        public SteeringWheelAngleValue steeringWheelAngleValue(){ return this.steeringWheelAngleValue; }
        public SteeringWheelAngleConfidence steeringWheelAngleConfidence(){ return this.steeringWheelAngleConfidence; }

        public SteeringWheelAngle() { this(new SteeringWheelAngleValue(),
                new SteeringWheelAngleConfidence()); }

        public SteeringWheelAngle(
                SteeringWheelAngleValue steeringWheelAngleValue,
                SteeringWheelAngleConfidence steeringWheelAngleConfidence) {
            this.steeringWheelAngleValue = steeringWheelAngleValue;
            this.steeringWheelAngleConfidence = steeringWheelAngleConfidence;
        }

		public SteeringWheelAngleValue getSteeringWheelAngleValue() {
			return steeringWheelAngleValue;
		}

		public SteeringWheelAngleConfidence getSteeringWheelAngleConfidence() {
			return steeringWheelAngleConfidence;
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

        public LateralAccelerationValue lateralAccelerationValue(){ return this.lateralAccelerationValue; }
        public AccelerationConfidence lateralAccelerationConfidence(){ return this.lateralAccelerationConfidence; }

        public LateralAcceleration() { this(new LateralAccelerationValue(), new AccelerationConfidence()); }
        public LateralAcceleration(LateralAccelerationValue lateralAccelerationValue,
                AccelerationConfidence lateralAccelerationConfidence) {
            this.lateralAccelerationValue = lateralAccelerationValue;
            this.lateralAccelerationConfidence = lateralAccelerationConfidence;
        }
		public LateralAccelerationValue getLateralAccelerationValue() {
			return lateralAccelerationValue;
		}
		public AccelerationConfidence getLateralAccelerationConfidence() {
			return lateralAccelerationConfidence;
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

        public VerticalAccelerationValue verticalAccelerationValue(){ return this.verticalAccelerationValue; }
        public AccelerationConfidence verticalAccelerationConfidence(){ return this.verticalAccelerationConfidence; }
        
        public VerticalAcceleration() { this(new VerticalAccelerationValue(), new AccelerationConfidence()); }
        public VerticalAcceleration(VerticalAccelerationValue verticalAccelerationValue,
                AccelerationConfidence verticalAccelerationConfidence) {
            this.verticalAccelerationValue = verticalAccelerationValue;
            this.verticalAccelerationConfidence = verticalAccelerationConfidence;
        }
		public VerticalAccelerationValue getVerticalAccelerationValue() {
			return verticalAccelerationValue;
		}
		public AccelerationConfidence getVerticalAccelerationConfidence() {
			return verticalAccelerationConfidence;
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

        public Latitude protectedZoneLatitude(){ return this.protectedZoneLatitude; }
        public Longitude protectedZoneLongitude(){ return this.protectedZoneLongitude; }
        public ProtectedZoneID cenDsrcTollingZoneID(){ return this.cenDsrcTollingZoneID; }

        public CenDsrcTollingZone() { this(new Latitude(), new Longitude(), null); }
        public CenDsrcTollingZone(Latitude protectedZoneLatitude, Longitude protectedZoneLongitude,
                ProtectedZoneID cenDsrcTollingZoneID) {
            this.protectedZoneLatitude = protectedZoneLatitude;
            this.protectedZoneLongitude = protectedZoneLongitude;
            this.cenDsrcTollingZoneID = cenDsrcTollingZoneID;
        }
		public Latitude getProtectedZoneLatitude() {
			return protectedZoneLatitude;
		}
		public Longitude getProtectedZoneLongitude() {
			return protectedZoneLongitude;
		}
		
		public boolean hasProtectedZoneID(){
			return cenDsrcTollingZoneID != null;
		}
		
		public ProtectedZoneID getCenDsrcTollingZoneID() {
			return cenDsrcTollingZoneID;
		}
    }

    @IntRange(minValue = 0, maxValue = 134217727)
    public static class ProtectedZoneID extends Asn1Integer { 
    	public ProtectedZoneID() { this(0); }
        public ProtectedZoneID(int value) { super(value); }
    }

    @Choice
    @HasExtensionMarker
    public static class LowFrequencyContainer {
        BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency;

        public BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency(){ return this.basicVehicleContainerLowFrequency; }

        public LowFrequencyContainer() { this(new BasicVehicleContainerLowFrequency()); }
        public LowFrequencyContainer(BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency) {
            this.basicVehicleContainerLowFrequency = basicVehicleContainerLowFrequency;
        }
        
        public boolean hasBasicVehicleContainerLowFrequency(){
        	return basicVehicleContainerLowFrequency!= null;
        }
        
		public BasicVehicleContainerLowFrequency getBasicVehicleContainerLowFrequency() {
			return basicVehicleContainerLowFrequency;
		}
    }

    @Sequence
    public static class BasicVehicleContainerLowFrequency {
        VehicleRole vehicleRole;
        ExteriorLights exteriorLights;
        PathHistory pathHistory;

        public VehicleRole vehicleRole(){ return this.vehicleRole; }
        public ExteriorLights exteriorLights(){ return this.exteriorLights; }
        public PathHistory pathHistory(){ return this.pathHistory; }

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

		public VehicleRole getVehicleRole() {
			return vehicleRole;
		}

		public ExteriorLights getExteriorLights() {
			return exteriorLights;
		}

		public PathHistory getPathHistory() {
			return pathHistory;
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

        public boolean lowBeamHeadlightsOn(){ return this.lowBeamHeadlightsOn; }
        public boolean highBeamHeadlightsOn(){ return this.highBeamHeadlightsOn; }
        public boolean leftTurnSignalOn(){ return this.leftTurnSignalOn; }
        public boolean rightTurnSignalOn(){ return this.rightTurnSignalOn; }
        public boolean daytimeRunningLightsOn(){ return this.daytimeRunningLightsOn; }
        public boolean reverseLightOn(){ return this.reverseLightOn; }
        public boolean fogLightOn(){ return this.fogLightOn; }
        public boolean parkingLightsOn(){ return this.parkingLightsOn; }

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
        
            public Builder set(
            		boolean lowBeamHeadlightsOn,
            		boolean highBeamHeadlightsOn,
            		boolean leftTurnSignalOn,
            		boolean rightTurnSignalOn,
            		boolean daytimeRunningLightsOn,
            		boolean reverseLightOn,
            		boolean fogLightOn,
            		boolean parkingLightsOn
            		){
            	checkCreated();
            	val.lowBeamHeadlightsOn = lowBeamHeadlightsOn;
            	val.highBeamHeadlightsOn = highBeamHeadlightsOn;
            	val.leftTurnSignalOn = leftTurnSignalOn;
            	val.rightTurnSignalOn = rightTurnSignalOn;
            	val.daytimeRunningLightsOn = daytimeRunningLightsOn;
            	val.reverseLightOn = reverseLightOn;
            	val.fogLightOn = fogLightOn;
            	val.parkingLightsOn = parkingLightsOn;
            	return this;
            }
        }

		public boolean isLowBeamHeadlightsOn() {
			return lowBeamHeadlightsOn;
		}

		public boolean isHighBeamHeadlightsOn() {
			return highBeamHeadlightsOn;
		}

		public boolean isLeftTurnSignalOn() {
			return leftTurnSignalOn;
		}

		public boolean isRightTurnSignalOn() {
			return rightTurnSignalOn;
		}

		public boolean isDaytimeRunningLightsOn() {
			return daytimeRunningLightsOn;
		}

		public boolean isReverseLightOn() {
			return reverseLightOn;
		}

		public boolean isFogLightOn() {
			return fogLightOn;
		}

		public boolean isParkingLightsOn() {
			return parkingLightsOn;
		}

    }

    @Sequence
    @HasExtensionMarker
    public static class RSUContainerHighFrequency {
        @Asn1Optional ProtectedCommunicationZonesRSU protectedCommunicationZonesRSU;

        public ProtectedCommunicationZonesRSU protectedCommunicationZonesRSU(){ return this.protectedCommunicationZonesRSU; }

        public RSUContainerHighFrequency() { this(null); }
        public RSUContainerHighFrequency(ProtectedCommunicationZonesRSU protectedCommunicationZonesRSU) {
            this.protectedCommunicationZonesRSU = protectedCommunicationZonesRSU;
        }
        public boolean hasProtectedCommunicationZonesRSU(){
        	return protectedCommunicationZonesRSU != null;
        }
        
		public ProtectedCommunicationZonesRSU getProtectedCommunicationZonesRSU() {
			return protectedCommunicationZonesRSU;
		}
    }

    @SizeRange(minValue = 0, maxValue = 40)
    public static class PathHistory extends Asn1SequenceOf<PathPoint> {
        public PathHistory(PathPoint... coll) {
            this(Arrays.asList(coll));
        }
        public PathHistory(Collection<PathPoint> coll) {
            super(coll);
        }
    }

    @SizeRange(minValue = 1, maxValue = 16)
    public static class ProtectedCommunicationZonesRSU extends Asn1SequenceOf<ProtectedCommunicationZone> {
        public ProtectedCommunicationZonesRSU(ProtectedCommunicationZone... coll) {
            this(Arrays.asList(coll));
        }
        public ProtectedCommunicationZonesRSU(Collection<ProtectedCommunicationZone> coll) {
            super(coll);
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
        
		public ProtectedZoneType getProtectedZoneType() {
			return protectedZoneType;
		}
		
		public boolean hasExpiryTime(){
			return expiryTime != null;
		}
		
		public TimestampIts getExpiryTime() {
			return expiryTime;
		}
		public Latitude getProtectedZoneLatitude() {
			return protectedZoneLatitude;
		}
		public Longitude getProtectedZoneLongitude() {
			return protectedZoneLongitude;
		}
		public boolean hasProtectedZoneRadius(){
			return protectedZoneRadius!=null;
		}
		public ProtectedZoneRadius getProtectedZoneRadius() {
			return protectedZoneRadius;
		}
		public boolean hasProtectedZoneID(){
			return protectedZoneID!=null;
		}
		public ProtectedZoneID getProtectedZoneID() {
			return protectedZoneID;
		}
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
		public DeltaReferencePosition getPathPosition() {
			return pathPosition;
		}
		public boolean hasPathDeltaTime(){
			return pathDeltaTime!=null;
		}
		public PathDeltaTime getPathDeltaTime() {
			return pathDeltaTime;
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
		public DeltaLatitude getDeltaLatitude() {
			return deltaLatitude;
		}
		public DeltaLongitude getDeltaLongitude() {
			return deltaLongitude;
		}
		public DeltaAltitude getDeltaAltitude() {
			return deltaAltitude;
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

        public boolean hasPublicTransportContainer(){
        	return publicTransportContainer != null;
        }
		public PublicTransportContainer getPublicTransportContainer() {
			return publicTransportContainer;
		}
		public boolean hasSpecialTransportContainer(){
        	return specialTransportContainer != null;
        }
		public SpecialTransportContainer getSpecialTransportContainer() {
			return specialTransportContainer;
		}
		public boolean hasDangerousGoodsContainer(){
        	return dangerousGoodsContainer != null;
        }
		public DangerousGoodsContainer getDangerousGoodsContainer() {
			return dangerousGoodsContainer;
		}
		public boolean hasRoadWorksContainerBasic(){
        	return roadWorksContainerBasic != null;
        }
		public RoadWorksContainerBasic getRoadWorksContainerBasic() {
			return roadWorksContainerBasic;
		}
		public boolean hasRescueContainer(){
        	return rescueContainer != null;
        }
		public RescueContainer getRescueContainer() {
			return rescueContainer;
		}
		public boolean hasEmergencyContainer(){
        	return emergencyContainer != null;
        }
		public EmergencyContainer getEmergencyContainer() {
			return emergencyContainer;
		}
		public boolean hasSafetyCarContainer(){
        	return safetyCarContainer != null;
        }
		public SafetyCarContainer getSafetyCarContainer() {
			return safetyCarContainer;
		}
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
		public boolean isEmbarkationStatus() {
			return embarkationStatus;
		}
		public boolean hasPtActivation(){
			return ptActivation!=null;
		}
		public PtActivation getPtActivation() {
			return ptActivation;
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
		public PtActivationType getPtActivationType() {
			return ptActivationType;
		}
		public PtActivationData getPtActivationData() {
			return ptActivationData;
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
		public SpecialTransportType getSpecialTransportType() {
			return specialTransportType;
		}
		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
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
		public DangerousGoodsBasic getDangerousGoodsBasic() {
			return dangerousGoodsBasic;
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
        public boolean hasRoadworksSubCauseCode(){
        	return roadworksSubCauseCode!=null;
        }
		public RoadworksSubCauseCode getRoadworksSubCauseCode() {
			return roadworksSubCauseCode;
		}
		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
		}
		public boolean hasClosedLanes(){
			return closedLanes!=null;
		}
		public ClosedLanes getClosedLanes() {
			return closedLanes;
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
		public boolean isLightBarActivated() {
			return lightBarActivated;
		}
		public boolean isSirenActivated() {
			return sirenActivated;
		}
    }

    @Sequence
    @HasExtensionMarker
    public static class ClosedLanes {
        @Asn1Optional HardShoulderStatus hardShoulderStatus;
        DrivingLaneStatus drivingLaneStatus;

        public HardShoulderStatus hardShoulderStatus(){ return this.hardShoulderStatus; }
        public DrivingLaneStatus drivingLaneStatus(){ return this.drivingLaneStatus; }

        public ClosedLanes() { this(new DrivingLaneStatus()); }
        public ClosedLanes(DrivingLaneStatus drivingLaneStatus) { this(null, drivingLaneStatus); }
        public ClosedLanes(
                HardShoulderStatus hardShoulderStatus,
                DrivingLaneStatus drivingLaneStatus
                ) {
            this.hardShoulderStatus = hardShoulderStatus;
            this.drivingLaneStatus = drivingLaneStatus;
        }
        public boolean hasHardShoulderStatus(){
        	return hardShoulderStatus!=null;
        }
		public HardShoulderStatus getHardShoulderStatus() {
			return hardShoulderStatus;
		}
		public DrivingLaneStatus getDrivingLaneStatus() {
			return drivingLaneStatus;
		}
    }

    public static enum HardShoulderStatus {
        vailableForStopping(0),
        closed(1),
        availableForDriving(2);

        private final int value;
        public int value() { return value; }
        private HardShoulderStatus(int value) { this.value = value; }
        public static HardShoulderStatus defaultValue() { return vailableForStopping; }
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
            this(Arrays.asList(coll));
        }

        protected DrivingLaneStatus() { super(new ArrayList<Boolean>()); }

        protected DrivingLaneStatus(Builder builder) { super(builder.bitset); }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private static final int minSize = Builder.class.getDeclaringClass().getAnnotation(SizeRange.class).minValue();
            private static final int maxSize = Builder.class.getDeclaringClass().getAnnotation(SizeRange.class).maxValue();
            BitSet bitset = new BitSet();
            public DrivingLaneStatus create() {
                if (bitset.size() < minSize) {
                    throw new IllegalStateException("Too few elements: have " + bitset.size() +
                            ", min needed " + minSize);
                }
                return new DrivingLaneStatus(this);
            }

            private Builder() { }

            public Builder setBit(int bitIndex, boolean value) {
                if (bitIndex >= maxSize) {
                    throw new IllegalArgumentException("Index " + bitIndex + " is out of range 0.."
                            + (maxSize - 1) + " for " + Builder.class.getDeclaringClass());
                }
                bitset.set(bitIndex, value);
                return this;
            }

            public Builder outermostLaneClosed(boolean outermostLaneClosed) {
                return setBit(1, outermostLaneClosed);
            }
            public Builder secondLaneFromOutsideClosed(boolean secondLaneFromOutsideClosed) {
                return setBit(2, secondLaneFromOutsideClosed);
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
		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
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

		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
		}
		public boolean hasIncidentIndication(){
			return incidentIndication!=null;
		}
		public CauseCode getIncidentIndication() {
			return incidentIndication;
		}
		public boolean hasEmergencyPriority(){
			return emergencyPriority!=null;
		}
		public EmergencyPriority getEmergencyPriority() {
			return emergencyPriority;
		}
    }

    @Sequence
    public static class CauseCode {
        CauseCodeType causeCode;
        SubCauseCodeType subCauseCode;

        public CauseCodeType causeCode(){ return this.causeCode; }
        public SubCauseCodeType subCauseCode(){ return this.subCauseCode; }

        public CauseCode() { this(new CauseCodeType(), new SubCauseCodeType()); }
        public CauseCode(CauseCodeType causeCode,
        SubCauseCodeType subCauseCode) {
            this.causeCode = causeCode;
            this.subCauseCode = subCauseCode;
        }
		public CauseCodeType getCauseCode() {
			return causeCode;
		}
		public SubCauseCodeType getSubCauseCode() {
			return subCauseCode;
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
		public boolean isRequestForRightOfWay() {
			return requestForRightOfWay;
		}
		public boolean isRequestForFreeCrossingAtATrafficLight() {
			return requestForFreeCrossingAtATrafficLight;
		}
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
		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
		}
		public boolean hasIncidentIndication(){
			return incidentIndication!=null;
		}
		public CauseCode getIncidentIndication() {
			return incidentIndication;
		}
		public boolean hasTrafficRule(){
			return trafficRule!=null;
		}
		public TrafficRule getTrafficRule() {
			return trafficRule;
		}
		public boolean hasSpeedLimit(){
			return speedLimit!=null;
		}
		public SpeedLimit getSpeedLimit() {
			return speedLimit;
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

        public ItsPduHeader header(){ return this.header; }
        public DecentralizedEnvironmentalNotificationMessage denm(){ return this.denm; }

        public Denm() { this (new ItsPduHeader(new MessageId(MessageId.denm)), new DecentralizedEnvironmentalNotificationMessage()); }

        public Denm(ItsPduHeader header, DecentralizedEnvironmentalNotificationMessage denm) {
            this.header = header;
            this.denm = denm;
        }

		public ItsPduHeader getHeader() {
			return header;
		}

		public DecentralizedEnvironmentalNotificationMessage getDenm() {
			return denm;
		}
    }

    @Sequence
    public static class DecentralizedEnvironmentalNotificationMessage {
        ManagementContainer management;
        @Asn1Optional SituationContainer situation;
        @Asn1Optional LocationContainer location;
        @Asn1Optional AlacarteContainer alacarte;

        public ManagementContainer management(){ return this.management; }
        public SituationContainer situation(){ return this.situation; }
        public LocationContainer location(){ return this.location; }
        public AlacarteContainer alacarte(){ return this.alacarte; }

        public DecentralizedEnvironmentalNotificationMessage() { this(new ManagementContainer()); }

        public DecentralizedEnvironmentalNotificationMessage(ManagementContainer management) {
            this(management, null, null, null);
        }

        public DecentralizedEnvironmentalNotificationMessage(
                ManagementContainer management,
                SituationContainer situation,
                LocationContainer location,
                AlacarteContainer alacarte) {
            this.management = management;
            this.situation = situation;
            this.location = location;
            this.alacarte = alacarte;
        }

		public ManagementContainer getManagement() {
			return management;
		}
		public boolean hasSituation(){
			return situation!=null;
		}
		public SituationContainer getSituation() {
			return situation;
		}
		public boolean hasLocation(){
			return location!=null;
		}
		public LocationContainer getLocation() {
			return location;
		}
		public boolean hasAlacarte(){
			return alacarte!=null;
		}
		public AlacarteContainer getAlacarte() {
			return alacarte;
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

        public ActionID actionID(){ return this.actionID; }
        public TimestampIts detectionTime(){ return this.detectionTime; }
        public TimestampIts referenceTime(){ return this.referenceTime; }
        public Termination termination(){ return this.termination; }
        public ReferencePosition eventPosition(){ return this.eventPosition; }
        public RelevanceDistance relevanceDistance(){ return this.relevanceDistance; }
        public RelevanceTrafficDirection relevanceTrafficDirection(){ return this.relevanceTrafficDirection; }
        public ValidityDuration validityDuration(){ return this.validityDuration; }
        public TransmissionInterval transmissionInterval(){ return this.transmissionInterval; }
        public StationType stationType(){ return this.stationType; }

        public ManagementContainer() {
            this.actionID = new ActionID();
            this.detectionTime = new TimestampIts();
            this.referenceTime = new TimestampIts();
            this.eventPosition = new ReferencePosition();
            this.stationType = new StationType();
        }

        public ManagementContainer(
                ActionID actionID,
                TimestampIts detectionTime,
                TimestampIts referenceTime,
                Termination termination,
                ReferencePosition eventPosition,
                RelevanceDistance relevanceDistance,
                RelevanceTrafficDirection relevanceTrafficDirection,
                ValidityDuration validityDuration,
                TransmissionInterval transmissionInterval,
                StationType stationType
                ) {
            this.actionID = actionID;
            this.detectionTime = detectionTime;
            this.referenceTime = referenceTime;
            this.termination = termination;
            this.eventPosition = eventPosition;
            this.relevanceDistance = relevanceDistance;
            this.relevanceTrafficDirection = relevanceTrafficDirection;
            this.validityDuration = validityDuration;
            this.transmissionInterval = transmissionInterval;
            this.stationType = stationType;
        }

        private ManagementContainer(Builder builder) {
            this(
                    builder.actionID,
                    builder.detectionTime,
                    builder.referenceTime,
                    builder.termination,
                    builder.eventPosition,
                    builder.relevanceDistance,
                    builder.relevanceTrafficDirection,
                    builder.validityDuration,
                    builder.transmissionInterval,
                    builder.stationType);
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            ActionID actionID;
            TimestampIts detectionTime;
            TimestampIts referenceTime;
            Termination termination;
            ReferencePosition eventPosition;
            RelevanceDistance relevanceDistance;
            RelevanceTrafficDirection relevanceTrafficDirection;
            ValidityDuration validityDuration;
            TransmissionInterval transmissionInterval;
            StationType stationType;

            public ManagementContainer create() { return new ManagementContainer(this); }

            private Builder() { }

            public Builder actionID                  (ActionID                  actionID)                  { this.actionID                  = actionID;                  return this; }
            public Builder detectionTime             (TimestampIts              detectionTime)             { this.detectionTime             = detectionTime;             return this; }
            public Builder referenceTime             (TimestampIts              referenceTime)             { this.referenceTime             = referenceTime;             return this; }
            public Builder termination               (Termination               termination)               { this.termination               = termination;               return this; }
            public Builder eventPosition             (ReferencePosition         eventPosition)             { this.eventPosition             = eventPosition;             return this; }
            public Builder relevanceDistance         (RelevanceDistance         relevanceDistance)         { this.relevanceDistance         = relevanceDistance;         return this; }
            public Builder relevanceTrafficDirection (RelevanceTrafficDirection relevanceTrafficDirection) { this.relevanceTrafficDirection = relevanceTrafficDirection; return this; }
            public Builder validityDuration          (ValidityDuration          validityDuration)          { this.validityDuration          = validityDuration;          return this; }
            public Builder transmissionInterval      (TransmissionInterval      transmissionInterval)      { this.transmissionInterval      = transmissionInterval;      return this; }
            public Builder stationType               (StationType               stationType)               { this.stationType               = stationType;               return this; }
        }

		public ActionID getActionID() {
			return actionID;
		}

		public TimestampIts getDetectionTime() {
			return detectionTime;
		}

		public TimestampIts getReferenceTime() {
			return referenceTime;
		}
		public boolean hasTermination(){
			return termination!=null;
		}
		public Termination getTermination() {
			return termination;
		}

		public ReferencePosition getEventPosition() {
			return eventPosition;
		}
		public boolean hasRelevanceDistance(){
			return relevanceDistance!=null;
		}
		public RelevanceDistance getRelevanceDistance() {
			return relevanceDistance;
		}
		public boolean hasRelevanceTrafficDirection(){
			return relevanceTrafficDirection!=null;
		}
		public RelevanceTrafficDirection getRelevanceTrafficDirection() {
			return relevanceTrafficDirection;
		}
		public boolean hasValidityDuration(){
			return validityDuration!=null;
		}
		public ValidityDuration getValidityDuration() {
			return validityDuration;
		}
		public boolean hasTransmissionInterval(){
			return transmissionInterval!=null;
		}
		public TransmissionInterval getTransmissionInterval() {
			return transmissionInterval;
		}

		public StationType getStationType() {
			return stationType;
		}
    }


    @Sequence
    public static class ActionID {
        StationID originatingStationID;
        SequenceNumber sequenceNumber;

        public StationID originatingStationID(){ return this.originatingStationID; }
        public SequenceNumber sequenceNumber(){ return this.sequenceNumber; }

        public ActionID() { this (new StationID(), new SequenceNumber()); }

        public ActionID(StationID originatingStationID, SequenceNumber sequenceNumber) {
            this.originatingStationID = originatingStationID;
            this.sequenceNumber = sequenceNumber;
        }

		public StationID getOriginatingStationID() {
			return originatingStationID;
		}

		public SequenceNumber getSequenceNumber() {
			return sequenceNumber;
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
        public static RelevanceDistance defaultValue() { return lessThan50m; }
    }

    public static enum RelevanceTrafficDirection {
        allTrafficDirections(0),
        upstreamTraffic(1),
        downstreamTraffic(2),
        oppositeTraffic(3);

        private final int value;
        public int value() { return value; }
        private RelevanceTrafficDirection(int value) { this.value = value; }
        public static RelevanceTrafficDirection defaultValue() { return allTrafficDirections; }
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
        public static Termination defaultValue() { return isCancellation; }
    }

    @Sequence
    @HasExtensionMarker
    public static class SituationContainer {
        InformationQuality informationQuality;
        CauseCode eventType;
        @Asn1Optional CauseCode linkedCause;
        @Asn1Optional EventHistory eventHistory;

        public InformationQuality informationQuality(){ return this.informationQuality; }
        public CauseCode eventType(){ return this.eventType; }
        public CauseCode linkedCause(){ return this.linkedCause; }
        public EventHistory eventHistory(){ return this.eventHistory; }

        public SituationContainer() { this(new InformationQuality(), new CauseCode()); }
        public SituationContainer(InformationQuality informationQuality, CauseCode eventType) {
            this(informationQuality, eventType, null, null);
        }
        public SituationContainer(
                InformationQuality informationQuality,
                CauseCode eventType,
                CauseCode linkedCause,
                EventHistory eventHistory
                ) {
            this.informationQuality = informationQuality;
            this.eventType = eventType;
            this.linkedCause = linkedCause;
            this.eventHistory = eventHistory;
        }
		public InformationQuality getInformationQuality() {
			return informationQuality;
		}

		public CauseCode getEventType() {
			return eventType;
		}
		public boolean hasLinkedCause(){
			return linkedCause!=null;
		}
		public CauseCode getLinkedCause() {
			return linkedCause;
		}
		public boolean hasEventHistory(){
			return eventHistory!=null;
		}
		public EventHistory getEventHistory() {
			return eventHistory;
		}
    }

    @IntRange(minValue = 0, maxValue = 7)
    public static class InformationQuality extends Asn1Integer {
        public static final int unavailable = 0;
        public static final int lowest = 1;
        public static final int highest = 7;

        public InformationQuality() { this(unavailable); }
        public InformationQuality(int value) { super(value); }
    }

    @SizeRange(minValue = 1, maxValue = 23)
    public static class EventHistory extends Asn1SequenceOf<EventPoint> {
        public EventHistory(EventPoint... coll) {
            this(Arrays.asList(coll));
        }
        public EventHistory(Collection<EventPoint> coll) {
            super(coll);
        }
    }

    @Sequence
    public static class EventPoint {
        DeltaReferencePosition eventPosition;
        @Asn1Optional PathDeltaTime eventDeltaTime;
        InformationQuality informationQuality;

        public DeltaReferencePosition eventPosition(){ return this.eventPosition; }
        public PathDeltaTime eventDeltaTime(){ return this.eventDeltaTime; }
        public InformationQuality informationQuality(){ return this.informationQuality; }

        public EventPoint() { this(new DeltaReferencePosition(), new InformationQuality()); }
        public EventPoint(DeltaReferencePosition eventPosition,
                InformationQuality informationQuality) {
            this(eventPosition, null, informationQuality); }
        public EventPoint(DeltaReferencePosition eventPosition,
            PathDeltaTime eventDeltaTime,
            InformationQuality informationQuality) {
            this.eventPosition = eventPosition;
            this.eventDeltaTime = eventDeltaTime;
            this.informationQuality = informationQuality;
        }
		public DeltaReferencePosition getEventPosition() {
			return eventPosition;
		}
		public boolean hasPathDeltaTime(){
			return eventDeltaTime!=null;
		}
		public PathDeltaTime getEventDeltaTime() {
			return eventDeltaTime;
		}
		public InformationQuality getInformationQuality() {
			return informationQuality;
		}
    }

    @Sequence
    @HasExtensionMarker
    public static class LocationContainer {
        @Asn1Optional Speed eventSpeed;
        @Asn1Optional Heading eventPositionHeading;
        Traces traces;
        @Asn1Optional RoadType roadType;

        public LocationContainer() { this(new Traces()); }
        public LocationContainer(Traces traces) { this(null, null, traces, null); }
        public LocationContainer(
                Speed eventSpeed,
                Heading eventPositionHeading,
                Traces traces,
                RoadType roadType
                ) {
            this.eventSpeed = eventSpeed;
            this.eventPositionHeading = eventPositionHeading;
            this.traces = traces;
            this.roadType = roadType;
        }
        public boolean hasEventSpeed(){
        	return eventSpeed!=null;
        }
		public Speed getEventSpeed() {
			return eventSpeed;
		}
		public boolean hasEventPositionHeading(){
			return eventPositionHeading!=null;
		}
		public Heading getEventPositionHeading() {
			return eventPositionHeading;
		}
		public Traces getTraces() {
			return traces;
		}
		public boolean hasRoadType(){
			return roadType!=null;
		}
		public RoadType getRoadType() {
			return roadType;
		}
    }

    @SizeRange(minValue = 1, maxValue = 7)
    public static class Traces extends Asn1SequenceOf<PathHistory> {
        public Traces(PathHistory... coll) {
            this(Arrays.asList(coll));
        }
        public Traces(Collection<PathHistory> coll) {
            super(coll);
        }
    }

    public static enum RoadType {
        urban_NoStructuralSeparationToOppositeLanes(0),
        urban_WithStructuralSeparationToOppositeLanes(1),
        nonUrban_NoStructuralSeparationToOppositeLanes(2),
        nonUrban_WithStructuralSeparationToOppositeLanes(3);

        private final int value;
        public int value() { return value; }
        private RoadType(int value) { this.value = value; }
        public static RoadType defaultValue() { return urban_NoStructuralSeparationToOppositeLanes; }
    }
    
    /////////////////////////////////////////////

    @Sequence
    @HasExtensionMarker
    public static class AlacarteContainer {
        @Asn1Optional LanePosition lanePosition;
        @Asn1Optional ImpactReductionContainer impactReduction;
        @Asn1Optional Temperature externalTemperature;
        @Asn1Optional RoadWorksContainerExtended roadWorks;
        @Asn1Optional PositioningSolutionType positioningSolution;
        @Asn1Optional StationaryVehicleContainer stationaryVehicle;

        public LanePosition lanePosition(){ return this.lanePosition; }
        public ImpactReductionContainer impactReduction(){ return this.impactReduction; }
        public Temperature externalTemperature(){ return this.externalTemperature; }
        public RoadWorksContainerExtended roadWorks(){ return this.roadWorks; }
        public PositioningSolutionType positioningSolution(){ return this.positioningSolution; }
        public StationaryVehicleContainer stationaryVehicle(){ return this.stationaryVehicle; }
        
        public AlacarteContainer() { this(null, null, null, null, null, null); }
        public AlacarteContainer(
                LanePosition lanePosition,
                ImpactReductionContainer impactReduction,
                Temperature externalTemperature,
                RoadWorksContainerExtended roadWorks,
                PositioningSolutionType positioningSolution,
                StationaryVehicleContainer stationaryVehicle
                ) {
            this.lanePosition = lanePosition;
            this.impactReduction = impactReduction;
            this.externalTemperature = externalTemperature;
            this.roadWorks = roadWorks;
            this.positioningSolution = positioningSolution;
            this.stationaryVehicle = stationaryVehicle;
        }
        public boolean hasLanePosition(){
        	return lanePosition!=null;
        }
		public LanePosition getLanePosition() {
			return lanePosition;
		}
		public boolean hasImpactReductionContainer(){
			return impactReduction!=null;
		}
		public ImpactReductionContainer getImpactReduction() {
			return impactReduction;
		}
		public boolean hasExternalTemperature(){
			return externalTemperature!=null;
		}
		public Temperature getExternalTemperature() {
			return externalTemperature;
		}
		public boolean hasRoadWorks(){
			return roadWorks!=null;
		}
		public RoadWorksContainerExtended getRoadWorks() {
			return roadWorks;
		}
		public boolean hasPositioningSolution(){
			return positioningSolution!=null;
		}
		public PositioningSolutionType getPositioningSolution() {
			return positioningSolution;
		}
		public boolean hasStationaryVehicle(){
			return stationaryVehicle!=null;
		}
		public StationaryVehicleContainer getStationaryVehicle() {
			return stationaryVehicle;
		}
    }

    @Sequence
    public static class ImpactReductionContainer {
        HeightLonCarr heightLonCarrLeft;
        HeightLonCarr heightLonCarrRight;
        PosLonCarr posLonCarrLeft;
        PosLonCarr posLonCarrRight;
        PositionOfPillars positionOfPillars;
        PosCentMass posCentMass;
        WheelBaseVehicle wheelBaseVehicle;
        TurningRadius turningRadius;
        PosFrontAx posFrontAx;
        PositionOfOccupants positionOfOccupants;
        VehicleMass vehicleMass;
        RequestResponseIndication requestResponseIndication;

        public ImpactReductionContainer() {
            this(
                    new HeightLonCarr(),
                    new HeightLonCarr(),
                    new PosLonCarr(),
                    new PosLonCarr(),
                    new PositionOfPillars(),
                    new PosCentMass(),
                    new WheelBaseVehicle(),
                    new TurningRadius(),
                    new PosFrontAx(),
                    new PositionOfOccupants(),
                    new VehicleMass(),
                    RequestResponseIndication.defaultValue());
        }

        // This constructor should be changed to a Builder.
        public ImpactReductionContainer(
                HeightLonCarr heightLonCarrLeft,
                HeightLonCarr heightLonCarrRight,
                PosLonCarr posLonCarrLeft,
                PosLonCarr posLonCarrRight,
                PositionOfPillars positionOfPillars,
                PosCentMass posCentMass,
                WheelBaseVehicle wheelBaseVehicle,
                TurningRadius turningRadius,
                PosFrontAx posFrontAx,
                PositionOfOccupants positionOfOccupants,
                VehicleMass vehicleMass,
                RequestResponseIndication requestResponseIndication
                ) {
            this.heightLonCarrLeft = heightLonCarrLeft;
            this.heightLonCarrRight = heightLonCarrRight;
            this.posLonCarrLeft = posLonCarrLeft;
            this.posLonCarrRight = posLonCarrRight;
            this.positionOfPillars = positionOfPillars;
            this.posCentMass = posCentMass;
            this.wheelBaseVehicle = wheelBaseVehicle;
            this.turningRadius = turningRadius;
            this.posFrontAx = posFrontAx;
            this.positionOfOccupants = positionOfOccupants;
            this.vehicleMass = vehicleMass;
            this.requestResponseIndication = requestResponseIndication;
        }

		public HeightLonCarr getHeightLonCarrLeft() {
			return heightLonCarrLeft;
		}

		public HeightLonCarr getHeightLonCarrRight() {
			return heightLonCarrRight;
		}

		public PosLonCarr getPosLonCarrLeft() {
			return posLonCarrLeft;
		}

		public PosLonCarr getPosLonCarrRight() {
			return posLonCarrRight;
		}

		public PositionOfPillars getPositionOfPillars() {
			return positionOfPillars;
		}

		public PosCentMass getPosCentMass() {
			return posCentMass;
		}

		public WheelBaseVehicle getWheelBaseVehicle() {
			return wheelBaseVehicle;
		}

		public TurningRadius getTurningRadius() {
			return turningRadius;
		}

		public PosFrontAx getPosFrontAx() {
			return posFrontAx;
		}

		public PositionOfOccupants getPositionOfOccupants() {
			return positionOfOccupants;
		}

		public VehicleMass getVehicleMass() {
			return vehicleMass;
		}

		public RequestResponseIndication getRequestResponseIndication() {
			return requestResponseIndication;
		}
    }


    @IntRange(minValue = 1, maxValue = 100)
    public static class HeightLonCarr extends Asn1Integer {
        public static final int oneCentimeter = 1;
        public static final int unavailable = 100;

        public HeightLonCarr() { this(unavailable); }
        public HeightLonCarr(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 127)
    public static class PosLonCarr extends Asn1Integer {
        public static final int oneCentimeter = 1;
        public static final int unavailable = 127;

        public PosLonCarr() { this(unavailable); }
        public PosLonCarr(int value) { super(value); }
    }

    @IntRange(minValue = -60, maxValue = 67)
    public static class Temperature extends Asn1Integer {
        public static final int equalOrSmallerThanMinus60Deg = -60;
        public static final int oneDegreeCelsius = 1;
        public static final int equalOrGreaterThan67Deg = 67;

        public Temperature() { this(27); }
        public Temperature(int value) { super(value); }
    }

    @Sequence
    public static class RoadWorksContainerExtended {
        @Asn1Optional LightBarSirenInUse lightBarSirenInUse;
        @Asn1Optional ClosedLanes closedLanes;
        @Asn1Optional RestrictedTypes restriction;
        @Asn1Optional SpeedLimit speedLimit;
        @Asn1Optional CauseCode incidentIndication;
        @Asn1Optional ItineraryPath recommendedPath;
        @Asn1Optional DeltaReferencePosition startingPointSpeedLimit;
        @Asn1Optional TrafficRule trafficFlowRule;
        @Asn1Optional ReferenceDenms referenceDenms;

        public RoadWorksContainerExtended() { this(null, null, null, null, null, null, null, null, null); }

        // This constructor should be changed to a Builder.
        public RoadWorksContainerExtended(
                LightBarSirenInUse lightBarSirenInUse,
                ClosedLanes closedLanes,
                RestrictedTypes restriction,
                SpeedLimit speedLimit,
                CauseCode incidentIndication,
                ItineraryPath recommendedPath,
                DeltaReferencePosition startingPointSpeedLimit,
                TrafficRule trafficFlowRule,
                ReferenceDenms referenceDenms
                ) {
            this.lightBarSirenInUse = lightBarSirenInUse;
            this.closedLanes = closedLanes;
            this.restriction = restriction;
            this.speedLimit = speedLimit;
            this.incidentIndication = incidentIndication;
            this.recommendedPath = recommendedPath;
            this.startingPointSpeedLimit = startingPointSpeedLimit;
            this.trafficFlowRule = trafficFlowRule;
            this.referenceDenms = referenceDenms;
        }
        public boolean hasLightBarSirenInUse(){
        	return lightBarSirenInUse!=null;
        }
		public LightBarSirenInUse getLightBarSirenInUse() {
			return lightBarSirenInUse;
		}
		public boolean hasClosedLanes(){
			return closedLanes!=null;
		}
		public ClosedLanes getClosedLanes() {
			return closedLanes;
		}
		public boolean hasRestriction(){
			return restriction!=null;
		}
		public RestrictedTypes getRestriction() {
			return restriction;
		}
		public boolean hasSpeedLimit(){
			return speedLimit!=null;
		}
		public SpeedLimit getSpeedLimit() {
			return speedLimit;
		}
		public boolean hasIncidentIndication(){
			return incidentIndication!=null;
		}
		public CauseCode getIncidentIndication() {
			return incidentIndication;
		}
		public boolean hasRecommendedPath(){
			return recommendedPath!=null;
		}
		public ItineraryPath getRecommendedPath() {
			return recommendedPath;
		}
		public boolean hasStartingPointSpeedLimit(){
			return startingPointSpeedLimit!=null;
		}
		public DeltaReferencePosition getStartingPointSpeedLimit() {
			return startingPointSpeedLimit;
		}
		public boolean hasTrafficFlowRule(){
			return trafficFlowRule!=null;
		}
		public TrafficRule getTrafficFlowRule() {
			return trafficFlowRule;
		}
		public boolean hasReferenceDenms(){
			return referenceDenms!=null;
		}
		public ReferenceDenms getReferenceDenms() {
			return referenceDenms;
		}
    }

    @SizeRange(minValue = 1, maxValue = 40)
    public static class ItineraryPath extends Asn1SequenceOf<ReferencePosition> {
        public ItineraryPath(ReferencePosition... coll) {
            this(Arrays.asList(coll));
        }
        public ItineraryPath(Collection<ReferencePosition> coll) {
            super(coll);
        }
    }

    @SizeRange(minValue = 1, maxValue = 3, hasExtensionMarker = true)
    public static class RestrictedTypes extends Asn1SequenceOf<StationType> {
        public RestrictedTypes(StationType... coll) {
            this(Arrays.asList(coll));
        }
        public RestrictedTypes(Collection<StationType> coll) {
            super(coll);
        }
    }

    @IntRange(minValue = 1, maxValue = 30)
    public static class PosPillar extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int unavailable = 30;

        public PosPillar() { this(unavailable); }
        public PosPillar(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 63)
    public static class PosCentMass extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int unavailable = 63;

        public PosCentMass() { this(unavailable); }
        public PosCentMass(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 127)
    public static class WheelBaseVehicle extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int unavailable = 127;

        public WheelBaseVehicle() { this(unavailable); }
        public WheelBaseVehicle(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 255)
    public static class TurningRadius extends Asn1Integer {
        public static final int point4Meters = 1;
        public static final int unavailable = 255;

        public TurningRadius() { this(unavailable); }
        public TurningRadius(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 20)
    public static class PosFrontAx extends Asn1Integer {
        public static final int tenCentimeters = 1;
        public static final int unavailable = 20;

        public PosFrontAx() { this(unavailable); }
        public PosFrontAx(int value) { super(value); }
    }

    @IntRange(minValue = 1, maxValue = 1024)
    public static class VehicleMass extends Asn1Integer {
        public static final int hundredKg = 1;
        public static final int unavailable = 1024;

        public VehicleMass() { this(unavailable); }
        public VehicleMass(int value) { super(value); }
    }

    @Bitstring
    @FixedSize(20)
    public static class PositionOfOccupants {
        boolean row1LeftOccupied;  // Bit (0)
        boolean row1RightOccupied;  // Bit (1)
        boolean row1MidOccupied;  // Bit (2)
        boolean row1NotDetectable;  // Bit (3)
        boolean row1NotPresent;  // Bit (4)
        boolean row2LeftOccupied;  // Bit (5)
        boolean row2RightOccupied;  // Bit (6)
        boolean row2MidOccupied;  // Bit (7)
        boolean row2NotDetectable;  // Bit (8)
        boolean row2NotPresent;  // Bit (9)
        boolean row3LeftOccupied;  // Bit (10)
        boolean row3RightOccupied;  // Bit (11)
        boolean row3MidOccupied;  // Bit (12)
        boolean row3NotDetectable;  // Bit (13)
        boolean row3NotPresent;  // Bit (14)
        boolean row4LeftOccupied;  // Bit (15)
        boolean row4RightOccupied;  // Bit (16)
        boolean row4MidOccupied;  // Bit (17)
        boolean row4NotDetectable;  // Bit (18)
        boolean row4NotPresent;  // Bit (19)
    }

    public static enum RequestResponseIndication {
        request(0),
        response(1);

        private final int value;
        public int value() { return value; }
        public static RequestResponseIndication defaultValue() { return request; }
        private RequestResponseIndication(int value) { this.value = value; }
    }


    @SizeRange(minValue = 1, maxValue = 3, hasExtensionMarker = true)
    public static class PositionOfPillars extends Asn1SequenceOf<PosPillar> {
        public PositionOfPillars(PosPillar... coll) {
            this(Arrays.asList(coll));
        }
        public PositionOfPillars(Collection<PosPillar> coll) {
            super(coll);
        }
    }

    @SizeRange(minValue = 1, maxValue = 8, hasExtensionMarker = true)
    public static class ReferenceDenms extends Asn1SequenceOf<ActionID> {
        public ReferenceDenms(ActionID... coll) {
            this(Arrays.asList(coll));
        }
        public ReferenceDenms(Collection<ActionID> coll) {
            super(coll);
        }
    }

    @HasExtensionMarker
    public static enum PositioningSolutionType {
        noPositioningSolution(0),
        sGNSS(1),
        dGNSS(2),
        sGNSSplusDR(3),
        dGNSSplusDR(4),
        dR(5);

        private final int value;
        public int value() { return value; }
        private PositioningSolutionType(int value) { this.value = value; }
    }

    @Sequence
    public static class StationaryVehicleContainer {
        @Asn1Optional StationarySince stationarySince;
        @Asn1Optional CauseCode stationaryCause;
        @Asn1Optional DangerousGoodsExtended carryingDangerousGoods;
        @Asn1Optional NumberOfOccupants numberOfOccupants;
        @Asn1Optional VehicleIdentification vehicleIdentification;
        @Asn1Optional EnergyStorageType energyStorageType;

        public StationaryVehicleContainer() { this(null, null, null, null, null, null); }
        public StationaryVehicleContainer(
                StationarySince stationarySince,
                CauseCode stationaryCause,
                DangerousGoodsExtended carryingDangerousGoods,
                NumberOfOccupants numberOfOccupants,
                VehicleIdentification vehicleIdentification,
                EnergyStorageType energyStorageType
                ) {
            this.stationarySince = stationarySince;
            this.stationaryCause = stationaryCause;
            this.carryingDangerousGoods = carryingDangerousGoods;
            this.numberOfOccupants = numberOfOccupants;
            this.vehicleIdentification = vehicleIdentification;
            this.energyStorageType = energyStorageType;
        }
        public boolean hasStationarySince(){
        	return stationarySince!=null;
        }
		public StationarySince getStationarySince() {
			return stationarySince;
		}
		public boolean hasStationaryCause(){
			return stationaryCause!=null;
		}
		public CauseCode getStationaryCause() {
			return stationaryCause;
		}
		public boolean hasCarryingDangerousGoods(){
			return carryingDangerousGoods!=null;
		}
		public DangerousGoodsExtended getCarryingDangerousGoods() {
			return carryingDangerousGoods;
		}
		public boolean hasNumberOfOccupants(){
			return numberOfOccupants!=null;
		}
		public NumberOfOccupants getNumberOfOccupants() {
			return numberOfOccupants;
		}
		public boolean hasVehicleIdentification(){
			return vehicleIdentification!=null;
		}
		public VehicleIdentification getVehicleIdentification() {
			return vehicleIdentification;
		}
		public boolean hasEnergyStorageType(){
			return energyStorageType!=null;
		}
		public EnergyStorageType getEnergyStorageType() {
			return energyStorageType;
		}
    }

    public static enum StationarySince {
        lessThan1Minute(0),
        lessThan2Minutes(1),
        lessThan15Minutes(2),
        equalOrGreater15Minutes(3);

        private final int value;
        public int value() { return value; }
        private StationarySince(int value) { this.value = value; }
        public static StationarySince defaultValue() { return lessThan1Minute; }
    }

    @IntRange(minValue = 0, maxValue = 127)
    public static class NumberOfOccupants extends Asn1Integer {
        public static final int oneOccupant = 1;
        public static final int unavailable = 127;

        public NumberOfOccupants() { this(unavailable); }
        public NumberOfOccupants(int value) { super(value); }
    }

    @Sequence
    @HasExtensionMarker
    public static class VehicleIdentification {
        @Asn1Optional WMInumber wMInumber;
        @Asn1Optional VDS vDS;
        public VehicleIdentification() { this(null, null); }
        public VehicleIdentification(
                WMInumber wMInumber,
                VDS vDS) {
            this.wMInumber = wMInumber;
            this.vDS = vDS;
        }
        public boolean hasWMInumber(){
        	return wMInumber!=null;
        }
		public WMInumber getwMInumber() {
			return wMInumber;
		}
		public boolean hasVDS(){
			return vDS!=null;
		}
		public VDS getvDS() {
			return vDS;
		}
    }

    @SizeRange(minValue = 1, maxValue = 3)
    @RestrictedString(CharacterRestriction.IA5String)
    public static class WMInumber extends Asn1String {
        public WMInumber() { this(""); }
        public WMInumber(String value) { super(value); }
    }

    @RestrictedString(CharacterRestriction.IA5String)
    @FixedSize(6)
    public static class VDS extends Asn1String {
        public VDS() { this(""); }
        public VDS(String value) { super(value); }
    }

    @Bitstring
    @FixedSize(7)
    public static class EnergyStorageType {
        boolean hydrogenStorage;  // Bit 0
        boolean electricEnergyStorage;  // Bit 1
        boolean liquidPropaneGas;  // Bit 2
        boolean compressedNaturalGas;  // Bit 3
        boolean diesel;  // Bit 4
        boolean gasoline;  // Bit 5
        boolean ammonia;  // Bit 6
        
        public boolean[] values(){
        	return new boolean[]{hydrogenStorage,electricEnergyStorage,liquidPropaneGas,compressedNaturalGas,diesel,gasoline,ammonia};
        }
        public EnergyStorageType(boolean[] data){
        	hydrogenStorage = data[0];
        	electricEnergyStorage = data[1];
        	liquidPropaneGas = data[2];
        	compressedNaturalGas = data[3];
        	diesel = data[4];
        	gasoline = data[5];
        	ammonia  = data[6];
        }
        
        public EnergyStorageType(){}
    }

    @Sequence
    public static class DangerousGoodsExtended {

        DangerousGoodsBasic dangerousGoodsType;
        UnNumber unNumber;
        boolean elevatedTemperature;
        boolean tunnelsRestricted;
        boolean limitedQuantity;
        @Asn1Optional EmergencyActionCode emergencyActionCode;
        @Asn1Optional PhoneNumber phoneNumber;
        @Asn1Optional CompanyName companyName;

        @Asn1AnonymousType
        @IntRange(minValue = 0, maxValue = 9999)
        public static class UnNumber extends Asn1Integer {
            public UnNumber() { this(0); }
            public UnNumber(int value) { super(value); }
        }

        @Asn1AnonymousType
        @SizeRange(minValue=1, maxValue=24)
        @RestrictedString(CharacterRestriction.IA5String)
        public static class EmergencyActionCode extends Asn1String {
            public EmergencyActionCode() { this(""); }
            public EmergencyActionCode(String value) { super(value); }
        }

        @Asn1AnonymousType
        @SizeRange(minValue=1, maxValue=24)
        @RestrictedString(CharacterRestriction.IA5String)
        public static class PhoneNumber extends Asn1String {
            public PhoneNumber() { this(""); }
            public PhoneNumber(String value) { super(value); }
        }

        @Asn1AnonymousType
        @SizeRange(minValue=1, maxValue=24)
        @RestrictedString(CharacterRestriction.UTF8String)
        public static class CompanyName extends Asn1String {
            public CompanyName() { this(""); }
            public CompanyName(String value) { super(value); }
        }


        public DangerousGoodsExtended() {
            this(DangerousGoodsBasic.defaultValue(), 0, false, false, false, null, null, null);
        }
        
        public DangerousGoodsExtended(
                DangerousGoodsBasic dangerousGoodsType,
                int unNumber,
                boolean elevatedTemperature,
                boolean tunnelsRestricted,
                boolean limitedQuantity
                ) {
            this.dangerousGoodsType = dangerousGoodsType;
            this.unNumber = new UnNumber(unNumber);
            this.elevatedTemperature = elevatedTemperature;
            this.tunnelsRestricted = tunnelsRestricted;
            this.limitedQuantity = limitedQuantity;
        }
        

        public DangerousGoodsExtended(
                DangerousGoodsBasic dangerousGoodsType,
                int unNumber,
                boolean elevatedTemperature,
                boolean tunnelsRestricted,
                boolean limitedQuantity,
                String emergencyActionCode,
                String phoneNumber,
                String companyName
                ) {
            this.dangerousGoodsType = dangerousGoodsType;
            this.unNumber = new UnNumber(unNumber);
            this.elevatedTemperature = elevatedTemperature;
            this.tunnelsRestricted = tunnelsRestricted;
            this.limitedQuantity = limitedQuantity;
            this.emergencyActionCode = new EmergencyActionCode(emergencyActionCode);
            this.phoneNumber = new PhoneNumber(phoneNumber);
            this.companyName = new CompanyName(companyName);
        }

		public DangerousGoodsBasic getDangerousGoodsType() {
			return dangerousGoodsType;
		}

		public UnNumber getUnNumber() {
			return unNumber;
		}

		public boolean isElevatedTemperature() {
			return elevatedTemperature;
		}

		public boolean isTunnelsRestricted() {
			return tunnelsRestricted;
		}

		public boolean isLimitedQuantity() {
			return limitedQuantity;
		}
		public boolean hasEmergencyActionCode(){
			return emergencyActionCode!=null;
		}
		public EmergencyActionCode getEmergencyActionCode() {
			return emergencyActionCode;
		}
		public boolean hasPhoneNumber(){
			return phoneNumber!=null;
		}

		public PhoneNumber getPhoneNumber() {
			return phoneNumber;
		}
		public boolean hasCompanyName(){
			return companyName!=null;
		}
		public CompanyName getCompanyName() {
			return companyName;
		}
    }
}
