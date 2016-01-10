package net.gcdc.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;

public class LocalCAM{
  private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);
  private final int LOCAL_CAM_LENGTH = 81;

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
  int vehicleRole = 0;

  public boolean isValidLocalCamBuffer(byte[] data){
    if(data.length < LOCAL_CAM_LENGTH){
      logger.error("Received local CAM is too short. Is: {} Should be: {}", 
        data.length, LOCAL_CAM_LENGTH);
      return false;
    }        
    return true;
  }

  /* For creating a local CAM from a UDP message as received from the vehicle control system. */
  LocalCAM(byte[] receivedData){
    if(!isValidLocalCamBuffer(receivedData)){      
      return;
    }

    ByteBuffer buffer = ByteBuffer.wrap(receivedData);
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
  LocalCAM(Cam camPacket){
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

  /* Return values as a byte array for sending as a local CAM UDP message. */
  byte[] asByteArray(){
    byte[] packetBuffer = new byte[LOCAL_CAM_LENGTH];
    ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
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
    return packetBuffer;
  }
}