/* Copyright 2016 Albin Severinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gcdc.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.asn1.datatypes.IntRange;

public class LocalDenm{
    private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);
    private final int LOCAL_DENM_LENGTH = 101;
    /* TODO: Is this the right way to keep sequence numbers? */
    private static int denmSequenceNumber = 0;

    byte messageID = 1;
    int stationID;
    int generationDeltaTime;
    byte containerMask;
    byte managementMask;
    int detectionTime;
    int referenceTime;
    int termination = 0;
    int latitude;
    int longitude;
    int semiMajorConfidence;
    int semiMinorConfidence;
    int semiMajorOrientation;
    int altitude;
    int relevanceDistance;
    int relevanceTrafficDirection;
    int validityDuration;
    int transmissionInterval;
    int stationType;
    byte situationMask;
    int informationQuality;
    int causeCode;
    int subCauseCode;
    int linkedCauseCode;
    int linkedSubCauseCode;
    byte alacarteMask;
    int lanePosition;
    int temperature;
    int positioningSolutionType;

    /* For creating a local DENM from a UDP message as received from the vehicle control system. */
    LocalDenm(byte[] receivedData){
        if(receivedData.length < LOCAL_DENM_LENGTH){
            logger.error("Local DENM is too short. Is: {} Should be: {}", 
                         receivedData.length, LOCAL_DENM_LENGTH);
            throw new IllegalArgumentException();
        }

        ByteBuffer buffer = ByteBuffer.wrap(receivedData);
        messageID = buffer.get();
        stationID = buffer.getInt();
        generationDeltaTime  = buffer.getInt();
        containerMask = buffer.get();
        managementMask = buffer.get();
        detectionTime = buffer.getInt();
        referenceTime = buffer.getInt();
        termination = buffer.getInt();
        latitude = buffer.getInt();
        longitude = buffer.getInt();
        semiMajorConfidence = buffer.getInt();
        semiMinorConfidence = buffer.getInt();
        semiMajorOrientation = buffer.getInt();
        altitude = buffer.getInt();
        relevanceDistance = buffer.getInt();
        relevanceTrafficDirection = buffer.getInt();
        validityDuration = buffer.getInt();
        transmissionInterval = buffer.getInt();
        stationType = buffer.getInt();
        situationMask = buffer.get();
        informationQuality = buffer.getInt();
        causeCode = buffer.getInt();
        subCauseCode = buffer.getInt();
        linkedCauseCode = buffer.getInt();
        linkedSubCauseCode = buffer.getInt();
        alacarteMask = buffer.get();
        lanePosition = buffer.getInt();
        temperature = buffer.getInt();
        positioningSolutionType = buffer.getInt();

        /* Verify that the values are correct and attempt to replace
         * any errors with default values. */        
        if(messageID != MessageId.denm){
            logger.error("Local DENM has incorrect id. Id: {} Should be: {}",
                         messageID, MessageId.denm);
            throw new IllegalArgumentException();
        }
        if(!checkInt(StationID.class, stationID, "StationID")) { throw new IllegalArgumentException(); }
        if(!checkInt(GenerationDeltaTime.class, generationDeltaTime, "GenerationDeltaTime")) { throw new IllegalArgumentException(); }
        //if(!checkInt(containerMask)) { .unavailable; }
        //if(!checkInt(managementMask)) { .unavailable; }

        /* These timestamps are handled differently that what the
         * spec. states. As a workaround to Matlab not supporting long
         * these values are sent as number of increments of 65536ms.
         * We get the true timestamps by multiplying with 65536 and
         * adding the generationDeltaTime. 
         */
        if(!checkInt(TimestampIts.class, detectionTime * 65536 + generationDeltaTime, "DetectionTime")) { throw new IllegalArgumentException(); }
        if(!checkInt(TimestampIts.class, referenceTime * 65536 + generationDeltaTime, "ReferenceTime")) { throw new IllegalArgumentException(); }
        
        if(!Termination.isMember(termination)){
            logger.warn("Termination is not valid. Value={}", termination);
            { termination = (int) Termination.defaultValue().value(); }
        }
        if(!checkInt(Latitude. class, latitude, "Latitude")) { latitude = Latitude.unavailable; }
        if(!checkInt(Longitude.class, longitude, "Longitude")) { longitude = Longitude.unavailable; }
        if(!checkInt(SemiAxisLength.class, semiMajorConfidence, "SemiMajorConfidence")) { semiMajorConfidence = SemiAxisLength.unavailable; }
        if(!checkInt(SemiAxisLength.class, semiMinorConfidence, "SemiMinorConfidence")) { semiMinorConfidence = SemiAxisLength.unavailable; }
        if(!checkInt(HeadingValue.class, semiMajorOrientation, "SemiMajorOrientation")) { semiMajorOrientation = HeadingValue.unavailable; }
        if(!checkInt(AltitudeValue.class, altitude, "Altitude")) { altitude = AltitudeValue.unavailable; }
        if(!RelevanceDistance.isMember(relevanceDistance)){
            logger.warn("RelevanceDistance is not valid. Value={}", relevanceDistance);
            { relevanceDistance = (int) RelevanceDistance.defaultValue().value(); }
        }
        
        if(!RelevanceTrafficDirection.isMember(relevanceTrafficDirection)){
            logger.error("RelevanceTrafficDirection is not valid. Value={}", relevanceTrafficDirection);
            { relevanceTrafficDirection = RelevanceTrafficDirection.defaultValue().value(); }
        }        
        if(!checkInt(ValidityDuration.class, validityDuration, "ValidityDuration")) { validityDuration = (int) net.gcdc.camdenm.CoopIts.defaultValidity.value; }
        if(!checkInt(TransmissionInterval.class, transmissionInterval, "TransmissionInterval")) { transmissionInterval = TransmissionInterval.oneMilliSecond * 100; }
        if(!checkInt(StationType.class, stationType, "StationType")) { stationType = StationType.unknown; }

        //if(!checkInt(situationMask)) { .unavailable; }
        if(!checkInt(InformationQuality.class, informationQuality, "InformationQuality")) { informationQuality = InformationQuality.unavailable; }
        if(!checkInt(CauseCodeType.class, causeCode, "CauseCode")) { throw new IllegalArgumentException(); }
        if(!checkInt(SubCauseCodeType.class, subCauseCode, "SubCauseCode")) { throw new IllegalArgumentException(); }
        if(!checkInt(CauseCodeType.class, linkedCauseCode, "LinkedCauseCode")) { throw new IllegalArgumentException(); }
        if(!checkInt(SubCauseCodeType.class, linkedSubCauseCode, "LinkedSubCauseCode")) { throw new IllegalArgumentException(); }
        //if(!checkInt(alacarteMask)) { .unavailable; }
        if(!checkInt(LanePosition.class, lanePosition, "LanePosition")) { throw new IllegalArgumentException(); }
        if(!checkInt(Temperature.class, temperature, "Temperature")) { temperature = 27; } /*  It's always 27C in Gothenburg :) */
        if(!PositioningSolutionType.isMember(positioningSolutionType)){
            logger.warn("PositioningSolutionType is not valid. Value={}", positioningSolutionType);
            { throw new IllegalArgumentException(); }
        }
    }
    
    /* For creating a local DENM from a DENM message as received from another ITS station. */
    LocalDenm(Denm denmPacket){      
        DecentralizedEnvironmentalNotificationMessage denm = denmPacket.getDenm();
        ItsPduHeader header = denmPacket.getHeader();
        ManagementContainer managementContainer = denm.getManagement();        
        messageID = (byte) header.getMessageID().value;
        stationID = (int) header.getStationID().value;
        generationDeltaTime = (int) managementContainer.getReferenceTime().value % 65536;

        
        containerMask = 0;

        /* ManagementContainer */
        managementMask = 0;
        
        //buffer.putInt((int) managementContainer.getActionID().getOriginatingStationID().value);       
        detectionTime = (int) managementContainer.getDetectionTime().value / 65536;
        referenceTime = (int) managementContainer.getReferenceTime().value / 65536;

        if(managementContainer.hasTermination()){
            managementMask += (1<<7);
            termination = (int) managementContainer.getTermination().value();
        }
        
        latitude = (int) managementContainer.getEventPosition().getLatitude().value;
        longitude = (int) managementContainer.getEventPosition().getLongitude().value;
        semiMajorConfidence = (int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMajorConfidence().value;
        semiMinorConfidence = (int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMinorConfidence().value;
        semiMajorOrientation = (int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMajorOrientation().value;
        altitude = (int) managementContainer.getEventPosition().getAltitude().getAltitudeValue().value;

        if(managementContainer.hasRelevanceDistance()){
            managementMask += (1<<6);            
            relevanceDistance = (int) managementContainer.getRelevanceDistance().value();
        }

        if(managementContainer.hasRelevanceTrafficDirection()){
            managementMask += (1<<5);            
            relevanceTrafficDirection = (int) managementContainer.getRelevanceTrafficDirection().value();
        }

        if(managementContainer.hasValidityDuration()){
            managementMask += (1<<4);            
            validityDuration = (int) managementContainer.getValidityDuration().value;
        }
        
        if(managementContainer.hasTransmissionInterval()){
            managementMask += (1<<3);            
            transmissionInterval = (int) managementContainer.getTransmissionInterval().value;
        }   
        
        stationType = (int) managementContainer.getStationType().value;

        /* SituationContainer */
        SituationContainer situationContainer = null;
        situationMask = 0;
        if(denm.hasSituation()){
            containerMask += (1<<7);
            situationContainer = denm.getSituation();

            informationQuality = (int) situationContainer.getInformationQuality().value;
            causeCode = (int) situationContainer.getEventType().getCauseCode().value;
            subCauseCode = (int) situationContainer.getEventType().getSubCauseCode().value;

            if(situationContainer.hasLinkedCause()){
                situationMask += (1<<7);
                linkedCauseCode = (int) situationContainer.getLinkedCause().getCauseCode().value;
                linkedSubCauseCode = (int) situationContainer.getLinkedCause().getSubCauseCode().value;
            }
        }

        /* Not used for GCDC16 */
        /* LocationContainer */
        /*
          LocationContainer locationContainer = null;
          if(denm.hasLocation()){
          containerMask += (1<<6);            
          locationContainer = denm.getLocation();
          byte locationMask = 0;
          buffer.put(locationMask);
            
          if(locationContainer.hasEventSpeed()){                
          locationMask += (1<<7);                
          buffer.putInt((int) locationContainer.getEventSpeed().getSpeedValue().value;
          buffer.putInt((int) locationContainer.getEventSpeed().getSpeedConfidence().value;
          }else buffer.put(new byte[2*4]);
            
          if(locationContainer.hasEventSpeed()){                
          locationMask += (1<<6);                
          buffer.putInt((int) locationContainer.getEventPositionHeading().getHeadingValue().value;
          buffer.putInt((int) locationContainer.getEventPositionHeading().getHeadingConfidence().value;                
          }else buffer.put(new byte[2*4]);

          if(locationContainer.hasEventSpeed()){                
          locationMask += (1<<5);                
          buffer.putInt((int) locationContainer.getRoadType().value();                          
          }else buffer.putInt(0);

          //Need to update the mask since it has been changed
          buffer.put(headerLength + managementLength + situationLength, locationMask);
          }else buffer.put(new byte[locationLength]);
        */

        /* AlacarteContainer */
        AlacarteContainer alacarteContainer = null;

        alacarteMask = 0;
        if(denm.hasAlacarte()){
            containerMask += (1<<5);            
            alacarteContainer = denm.getAlacarte();

            if(alacarteContainer.hasLanePosition()){
                alacarteMask += (1<<7);
                lanePosition = (int) alacarteContainer.getLanePosition().value;                
            }

            if(alacarteContainer.hasExternalTemperature()){
                alacarteMask += (1<<5);
                temperature = (int) alacarteContainer.getExternalTemperature().value;                
            }

            if(alacarteContainer.hasPositioningSolution()){
                alacarteMask += (1<<3);
                positioningSolutionType = (int) alacarteContainer.getPositioningSolution().value();
            }
        }
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
            logger.warn("{} is outside of range. Value={}, {}",
                        name, value, getIntRangeString(intRange));
            return false;
        }else return true;
    }


    /* Check if the local DENM is valid. */
    boolean isValid(){
        boolean valid = true;

        if(!checkInt(StationID.class, stationID, "StationID")) valid = false;
        if(!checkInt(GenerationDeltaTime.class, generationDeltaTime, "GenerationDeltaTime")) valid = false;
        //if(!checkInt(containerMask)) valid = false;
        //if(!checkInt(managementMask)) valid = false;

        /* These timestamps are handled differently that what the
         * spec. states. As a workaround to Matlab not supporting long
         * these values are sent as number of increments of 65536ms.
         * We get the true timestamps by multiplying with 65536 and
         * adding the generationDeltaTime. 
         */
        if(!checkInt(TimestampIts.class, detectionTime * 65536 + generationDeltaTime, "DetectionTime")) valid = false;
        if(!checkInt(TimestampIts.class, referenceTime * 65536 + generationDeltaTime, "ReferenceTime")) valid = false;
        
        if(!Termination.isMember(termination)){
            logger.error("Termination is not valid. Value={}", termination);
            valid = false;
        }
        if(!checkInt(Latitude. class, latitude, "Latitude")) valid = false;
        if(!checkInt(Longitude.class, longitude, "Longitude")) valid = false;
        if(!checkInt(SemiAxisLength.class, semiMajorConfidence, "SemiMajorConfidence")) valid = false;
        if(!checkInt(SemiAxisLength.class, semiMinorConfidence, "SemiMinorConfidence")) valid = false;
        if(!checkInt(HeadingValue.class, semiMajorOrientation, "SemiMajorOrientation")) valid = false;
        if(!checkInt(AltitudeValue.class, altitude, "Altitude")) valid = false;
        if(!RelevanceDistance.isMember(relevanceDistance)){
            logger.error("RelevanceDistance is not valid. Value={}", relevanceDistance);
            valid = false;
        }
        
        if(!RelevanceTrafficDirection.isMember(relevanceTrafficDirection)){
            logger.error("RelevanceTrafficDirection is not valid. Value={}", relevanceTrafficDirection);
            valid = false;
        }        
        if(!checkInt(ValidityDuration.class, validityDuration, "ValidityDuration")) valid = false;
        if(!checkInt(TransmissionInterval.class, transmissionInterval, "TransmissionInterval")) valid = false;
        if(!checkInt(StationType.class, stationType, "StationType")) valid = false;

        //if(!checkInt(situationMask)) valid = false;
        if(!checkInt(InformationQuality.class, informationQuality, "InformationQuality")) valid = false;
        if(!checkInt(CauseCodeType.class, causeCode, "CauseCode")) valid = false;
        if(!checkInt(SubCauseCodeType.class, subCauseCode, "SubCauseCode")) valid = false;
        if(!checkInt(CauseCodeType.class, linkedCauseCode, "LinkedCauseCode")) valid = false;
        if(!checkInt(SubCauseCodeType.class, linkedSubCauseCode, "LinkedSubCauseCode")) valid = false;
        //if(!checkInt(alacarteMask)) valid = false;
        if(!checkInt(LanePosition.class, lanePosition, "LanePosition")) valid = false;
        if(!checkInt(Temperature.class, temperature, "Temperature")) valid = false;
        if(!PositioningSolutionType.isMember(positioningSolutionType)){
            logger.error("PositioningSolutionType is not valid. Value={}", positioningSolutionType);
            valid = false;
        }
        return valid;
    }    

    /* Return values as a byte array for sending as a local DENM UDP message. */
    byte[] asByteArray(){
        byte[] packetBuffer = new byte[LOCAL_DENM_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        buffer.put(messageID);
        buffer.putInt(stationID);
        buffer.putInt(generationDeltaTime);
        buffer.put(containerMask);
        buffer.put(managementMask);
        buffer.putInt(detectionTime);
        buffer.putInt(referenceTime);
        buffer.putInt(termination);
        buffer.putInt(latitude);
        buffer.putInt(longitude);
        buffer.putInt(semiMajorConfidence);
        buffer.putInt(semiMinorConfidence);
        buffer.putInt(semiMajorOrientation);
        buffer.putInt(altitude);
        buffer.putInt(relevanceDistance);
        buffer.putInt(relevanceTrafficDirection);
        buffer.putInt(validityDuration);
        buffer.putInt(transmissionInterval);
        buffer.putInt(stationType);
        buffer.put(situationMask);
        buffer.putInt(informationQuality);
        buffer.putInt(causeCode );
        buffer.putInt(subCauseCode);
        buffer.putInt(linkedCauseCode);
        buffer.putInt(linkedSubCauseCode);
        buffer.put(alacarteMask);
        buffer.putInt(lanePosition);
        buffer.putInt(temperature);
        buffer.putInt(positioningSolutionType);
        return packetBuffer;
}

    /* Return values as a proper DENM message for sending to another ITS station. */    
    Denm asDenm(){
        /* Management container */
        ManagementContainer managementContainer =
            ManagementContainer.builder()
            .actionID(new ActionID(new StationID(stationID), new SequenceNumber(denmSequenceNumber++%65535)))
            
            /* Referencetime is sent in increments of 65536ms. So to
             * get the current time we need to multiply with that and
             * add the generationDeltaTime. This is a workaround for
             * Simulink not supporting longs.
             */
            .detectionTime(new TimestampIts((long) detectionTime * 65536 + generationDeltaTime))
            .referenceTime(new TimestampIts((long) referenceTime * 65536 + generationDeltaTime))
            .termination((managementMask & (1<<7)) != 0 ? Termination.values()[termination] : null)
            .eventPosition(new ReferencePosition(new Latitude(latitude),
                                                 new Longitude(longitude),
                                                 new PosConfidenceEllipse(new SemiAxisLength(semiMajorConfidence),
                                                                          new SemiAxisLength(semiMinorConfidence),
                                                                          new HeadingValue(semiMajorOrientation)),
                                                 new Altitude(new AltitudeValue(altitude),
                                                              //TODO: Should altitudeconfidence be added?
                                                              AltitudeConfidence.unavailable)))
            .relevanceDistance((managementMask & (1<<6)) != 0 ? RelevanceDistance.values()[relevanceDistance] : null)
            .relevanceTrafficDirection((managementMask & (1<<5)) != 0 ? RelevanceTrafficDirection.values()[relevanceTrafficDirection] : null)
            .validityDuration((managementMask & (1<<4)) != 0 ? new ValidityDuration(validityDuration) : null)
            .transmissionInterval((managementMask & (1<<3)) != 0 ? new TransmissionInterval(transmissionInterval) : null)
            .stationType(new StationType(stationType))
            .create();

        /* Situation container */
        SituationContainer situationContainer = (containerMask & (1<<7)) != 0 ?
            new SituationContainer(new InformationQuality(informationQuality),
                                   new CauseCode(new CauseCodeType(causeCode), new SubCauseCodeType(subCauseCode)),
                                   (situationMask & (1<<7)) != 0 ? new CauseCode(new CauseCodeType(linkedCauseCode),
                                                                                 new SubCauseCodeType(linkedSubCauseCode))
                                   : null,
                                   //TODO: Add EventHistory to SituationContainer
                                   null)
            :null;

        /* Location container */        
        /* TODO: Local message set needs support for variable length
         * packets in order to add the Traces in the location
         * container. Will not be implemented for GCDC16.
         */
        LocationContainer locationContainer = (containerMask & (1<<6)) != 0 ?
            new LocationContainer()
            :null;

        /* Alacarte container */
        AlacarteContainer alacarteContainer = (containerMask & (1<<5)) != 0 ?
            new AlacarteContainer((alacarteMask & (1<<7)) != 0 ? new LanePosition(lanePosition) : null,
                                  //TODO: Currently no plans of implementing.                                  
                                  (alacarteMask & (1<<6)) != 0 ? new ImpactReductionContainer() : null,
                                  (alacarteMask & (1<<5)) != 0 ? new Temperature(temperature) : null,
                                  //TODO: Currently no plans of implementing.                                  
                                  (alacarteMask & (1<<4)) != 0 ? new RoadWorksContainerExtended() : null,
                                  (alacarteMask & (1<<3)) != 0 ? PositioningSolutionType.values()[positioningSolutionType] : null,
                                  //TODO: Currently no plans of implementing.
                                  (alacarteMask & (1<<2)) != 0 ? new StationaryVehicleContainer() : null)                                  
            :null;
                                                                               
        DecentralizedEnvironmentalNotificationMessage decentralizedEnvironmentalNotificationMessage =
            new DecentralizedEnvironmentalNotificationMessage(managementContainer,
                                                              situationContainer,
                                                              locationContainer,
                                                              alacarteContainer);

        return new Denm(
                        new ItsPduHeader(new ProtocolVersion(1),
                                         new MessageId(MessageId.denm),
                                         new StationID(stationID)),
                        decentralizedEnvironmentalNotificationMessage);
    }
}
