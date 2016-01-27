package net.gcdc.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import java.lang.annotation.Annotation;
import net.gcdc.asn1.datatypes.IntRange;
import net.gcdc.asn1.datatypes.Asn1Integer;

public class LocalCam{
    private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);
    private final int LOCAL_CAM_LENGTH = 82;
    
    byte messageID = 2;
    int stationID;
    int genDeltaTimeMillis;
    byte containerMask;
    int stationType;
    int latitude;
    int longitude;                         
    int semiMajorAxisConfidence;
    int semiMinorAxisConfidence;
    int semiMajorOrientation;
    int altitude;
    int heading;
    int headingConfidence;
    int speed;
    int speedConfidence;
    int vehicleLength;
    int vehicleWidth;
    int longitudinalAcceleration;
    int longitudinalAccelerationConfidence;
    int yawRate;
    int yawRateConfidence;
    int vehicleRole;

    /* For creating a local CAM from a UDP message as received from the vehicle control system. */
    LocalCam(byte[] receivedData){
        if(receivedData.length < LOCAL_CAM_LENGTH){
            logger.error("Local CAM is too short. Is: {} Should be: {}", 
                         receivedData.length, LOCAL_CAM_LENGTH);
        }

        ByteBuffer buffer = ByteBuffer.wrap(receivedData);
        messageID = buffer.get();
        if(messageID != MessageId.cam){
            logger.error("Local CAM has incorrect id. Id: {} Should be: {}",
                         messageID, MessageId.cam);
        }
        stationID = buffer.getInt();
        genDeltaTimeMillis = buffer.getInt();
        containerMask = buffer.get();
        stationType = buffer.getInt();
        latitude = buffer.getInt();
        longitude = buffer.getInt();
        semiMajorAxisConfidence = buffer.getInt();
        semiMinorAxisConfidence = buffer.getInt();
        semiMajorOrientation = buffer.getInt();
        altitude = buffer.getInt();
        heading = buffer.getInt();
        headingConfidence = buffer.getInt();
        speed = buffer.getInt();
        speedConfidence = buffer.getInt();
        vehicleLength = buffer.getInt();
        vehicleWidth = buffer.getInt();
        longitudinalAcceleration = buffer.getInt();
        longitudinalAccelerationConfidence = buffer.getInt();
        yawRate = buffer.getInt();
        yawRateConfidence = buffer.getInt();
        vehicleRole = buffer.getInt();
    }

  /* For creating a local CAM from a CAM message as received from another ITS station. */
  LocalCam(Cam camPacket){
    CoopAwareness cam = camPacket.getCam();
    ItsPduHeader header = camPacket.getHeader();
    GenerationDeltaTime generationDeltaTime = cam.getGenerationDeltaTime();         
    CamParameters camParameters = cam.getCamParameters();

    messageID = (byte) header.getMessageID().value;
    stationID = (int) header.getStationID().value;
    genDeltaTimeMillis = (int) generationDeltaTime.value;
    byte containerMask = 0;

    /* BasicContainer */
    BasicContainer basicContainer = camParameters.getBasicContainer();
    stationType = (int) basicContainer.getStationType().value;
    latitude = (int) basicContainer.getReferencePosition().getLatitude().value;
    longitude = (int) basicContainer.getReferencePosition().getLongitude().value;
    semiMajorAxisConfidence = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorConfidence().value;
    semiMinorAxisConfidence = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMinorConfidence().value;
    semiMajorOrientation = (int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorOrientation().value;
    altitude = (int) basicContainer.getReferencePosition().getAltitude().getAltitudeValue().value;

    /* HighFrequencyContainer */
    HighFrequencyContainer highFrequencyContainer = camParameters.getHighFrequencyContainer();
    BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency = highFrequencyContainer.getBasicVehicleContainerHighFrequency();

    heading = (int) basicVehicleContainerHighFrequency.getHeading().getHeadingValue().value;
    headingConfidence = (int) basicVehicleContainerHighFrequency.getHeading().getHeadingConfidence().value;
    speed = (int) basicVehicleContainerHighFrequency.getSpeed().getSpeedValue().value;
    speedConfidence = (int) basicVehicleContainerHighFrequency.getSpeed().getSpeedConfidence().value;
    vehicleLength = (int) basicVehicleContainerHighFrequency.getVehicleLength().getVehicleLengthValue().value;
    vehicleWidth = (int) basicVehicleContainerHighFrequency.getVehicleWidth().value;
    longitudinalAcceleration = (int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationValue().value();
    longitudinalAccelerationConfidence = (int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationConfidence().value();
    yawRate = (int) basicVehicleContainerHighFrequency.getYawRate().getYawRateValue().value;
    yawRateConfidence = (int) basicVehicleContainerHighFrequency.getYawRate().getYawRateConfidence().value();


    /* LowFrequencyContainer */
    LowFrequencyContainer lowFrequencyContainer = null;
    if(camParameters.hasLowFrequencyContainer()){
      containerMask += (1<<7);
      lowFrequencyContainer = camParameters.getLowFrequencyContainer();
      BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency = lowFrequencyContainer.getBasicVehicleContainerLowFrequency();
      vehicleRole = (int) basicVehicleContainerLowFrequency.getVehicleRole().value();
    }

    this.containerMask = containerMask;
  }

    /* Return the IntRange min and max value as a nice string. */
    String getIntRangeString(IntRange intRange){
        String string = "minValue=" + intRange.minValue() + ", maxValue=" + intRange.maxValue();
        return string;
    }

    /* Return true if value is within the IntRange, and false
       otherwise. */
    boolean compareIntRange(int value, IntRange intRange){
        return value <= intRange.maxValue() && value >= intRange.minValue();
    }

    public boolean checkInt(Class<?> classOfT, int value, String name){
        IntRange intRange = (IntRange) classOfT.getAnnotation(IntRange.class);
        if(intRange == null){
            logger.error("{} does not have an IntRange!", classOfT);
            return false;
        }
        if(!compareIntRange(value, intRange)){
            logger.error("{} is outside of range. Value={}, {}",
                         name, value, getIntRangeString(intRange));
            return false;
        }else return true;
    }

    /*
    public boolean checkEnum(Enum classOfT, int value, String name){
        boolean valid = classOfT.isMember(value);
        if(!valid) logger.error("{} is not valid. Value={}",
                                name, value);
        return valid;
    }
    */
    

    /* Check if the local CAM is valid. */
    boolean isValid(){
        boolean valid = true;
        
        if(messageID != MessageId.cam){
            logger.error("MessageID is: {} Should be: {}",
                         messageID, MessageId.cam);
            valid = false;
        }

        if(!checkInt(StationID.class, stationID, "StationID")) valid = false;
        if(!checkInt(GenerationDeltaTime.class, genDeltaTimeMillis, "GenerationDeltaTime")) valid = false;
        if(!checkInt(StationType.class, stationType, "StationType")) valid = false;
        if(!checkInt(Latitude.class, latitude, "Latitude")) valid = false;
        if(!checkInt(Longitude.class, longitude, "Longitude")) valid = false;        
        if(!checkInt(SemiAxisLength.class, semiMajorAxisConfidence, "SemiMajorConfidence")) valid = false;
        if(!checkInt(SemiAxisLength.class, semiMinorAxisConfidence, "SemiMinorConfidence")) valid = false;
        if(!checkInt(HeadingValue.class, semiMajorOrientation, "SemiMajorOrientation")) valid = false;
        if(!checkInt(AltitudeValue.class, altitude, "Altitude")) valid = false;        
        if(!checkInt(HeadingValue.class, heading, "Heading")) valid = false;
        if(!checkInt(HeadingConfidence.class, headingConfidence, "HeadingConfidence")) valid = false;
        if(!checkInt(SpeedValue.class, speed, "Speed")) valid = false;
        if(!checkInt(SpeedConfidence.class, speedConfidence, "SpeedConfidence")) valid = false;
        if(!checkInt(VehicleLengthValue.class, vehicleLength, "VehicleLength")) valid = false;
        if(!checkInt(VehicleWidth.class, vehicleWidth, "VehicleWidth")) valid = false;
        if(!checkInt(LongitudinalAccelerationValue.class, longitudinalAcceleration, "LongitudinalAcceleration")) valid = false;
        if(!checkInt(AccelerationConfidence.class,
                     longitudinalAccelerationConfidence,
                     "LongitudinalAccelerationConfidence")) valid = false;
        if(!checkInt(YawRateValue.class, yawRate, "YawRate")) valid = false;

        /* TODO: Find a cleaner way to check enums. Also, this
         * approach is not very informative.*/
        if(!YawRateConfidence.isMember(yawRateConfidence)){
            logger.error("YawRateConfidence is not valid. Value={}", yawRateConfidence);
            valid = false;
        }
        if(!VehicleRole.isMember(vehicleRole)){
            logger.error("VehicleRole is not valid. Value={}", vehicleRole);
            valid = false;
        }        

        /* TODO:
        byte containerMask;
        */
        return valid;
    }

    /* Return true if the local CAM has a low frequency container. */
    boolean hasLowFrequencyContainer(){
        return (containerMask & (1<<7)) != 0;
    }

    /* Return values as a byte array for sending as a local CAM UDP message. */
    byte[] asByteArray(){
        byte[] packetBuffer = new byte[LOCAL_CAM_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        try{
            buffer.put(messageID);
            buffer.putInt(stationID);
            buffer.putInt(genDeltaTimeMillis);
            buffer.put(containerMask);
            buffer.putInt(stationType);
            buffer.putInt(latitude);
            buffer.putInt(longitude);                         
            buffer.putInt(semiMajorAxisConfidence);
            buffer.putInt(semiMinorAxisConfidence);
            buffer.putInt(semiMajorOrientation);
            buffer.putInt(altitude);
            buffer.putInt(heading);
            buffer.putInt(headingConfidence);
            buffer.putInt(speed);
            buffer.putInt(speedConfidence);
            buffer.putInt(vehicleLength);
            buffer.putInt(vehicleWidth);
            buffer.putInt(longitudinalAcceleration);
            buffer.putInt(longitudinalAccelerationConfidence);
            buffer.putInt(yawRate);
            buffer.putInt(yawRateConfidence);
            buffer.putInt(vehicleRole);
        }catch(BufferOverflowException e){
            logger.error("Error converting local CAM to byte array.", e);
            /* Return an empty byte array as the vehicle control
             * system has to deal with those anyway.
             */
            return new byte[LOCAL_CAM_LENGTH];
    }
        return packetBuffer;
  }

  /* Return values as a proper CAM message for sending to another ITS station. */
  Cam asCam(){
    LowFrequencyContainer lowFrequencyContainer = (containerMask & (1<<7)) != 0 ?
        new LowFrequencyContainer(
                                  new BasicVehicleContainerLowFrequency(
                                                                        VehicleRole.fromCode(vehicleRole),
                                                                        ExteriorLights.builder()
                                                                        .set(false, false, false, false, false, false, false, false)
                                                                        .create(),
                                                                        new PathHistory()
                                                                        ))
        :
        null; 

    //Not used for participating vehicles
    SpecialVehicleContainer specialVehicleContainer = null;

    BasicContainer basicContainer =
        new BasicContainer(new StationType(stationType),
                           new ReferencePosition(new Latitude(latitude),
                                                 new Longitude(longitude),
                                                 new PosConfidenceEllipse(new SemiAxisLength(semiMajorAxisConfidence),
                                                                          new SemiAxisLength(semiMinorAxisConfidence),
                                                                          new HeadingValue(semiMajorOrientation)),
                                                 new Altitude(new AltitudeValue(altitude),
                                                              AltitudeConfidence.unavailable)));
    HighFrequencyContainer highFrequencyContainer =
        new HighFrequencyContainer(BasicVehicleContainerHighFrequency.builder()
                                   .heading(new Heading(new HeadingValue(heading),
                                                        new HeadingConfidence(headingConfidence)))
                                   .speed(new Speed(new SpeedValue(speed),
                                                    new SpeedConfidence(speedConfidence)))
                                   //DriveDirection isn't part of the GCDC spec. Set to unavailable.
                                   .driveDirection(DriveDirection.values()[2])
                                   .vehicleLength(new VehicleLength(new VehicleLengthValue(vehicleLength), VehicleLengthConfidenceIndication.unavailable))
                                   .vehicleWidth(new VehicleWidth(vehicleWidth))
                                   .longitudinalAcceleration(new LongitudinalAcceleration(new LongitudinalAccelerationValue(longitudinalAcceleration),
                                                                                          new AccelerationConfidence(longitudinalAccelerationConfidence)))
                                   //Curvature and CurvatureCalculationMode isn't part of the GCDC spec. Set to unavailable.
                                   .curvature(new Curvature())
                                   .curvatureCalculationMode(CurvatureCalculationMode.values()[2])                                       
                                   .yawRate(new YawRate(new YawRateValue(yawRate),
                                                        //TODO: This code is slow. Cache YawRateConfidence.values() if it's a problem.
                                                        YawRateConfidence.values()[yawRateConfidence]))
                                   .create()
                                   );

        return new Cam(
                       new ItsPduHeader(new ProtocolVersion(1),
                                        new MessageId(MessageId.cam),
                                        new StationID(stationID)),
                new CoopAwareness(
                        new GenerationDeltaTime(genDeltaTimeMillis * GenerationDeltaTime.oneMilliSec),
                        new CamParameters(basicContainer,
                                          highFrequencyContainer,
                                          lowFrequencyContainer,
                                          specialVehicleContainer)));
  }
}
