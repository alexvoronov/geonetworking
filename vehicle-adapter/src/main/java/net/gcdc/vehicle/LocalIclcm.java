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
import net.gcdc.camdenm.Iclcm.*;
import net.gcdc.asn1.datatypes.IntRange;

public class LocalIclcm{
    private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);
    private final int LOCAL_iCLCM_LENGTH = 111;

    byte messageID;
    int stationID;
    byte containerMask;
    //HW Container
    int rearAxleLocation;
    int controllerType;
    int responseTimeConstant;
    int responseTimeDelay;
    int targetLongAcc;
    int timeHeadway;
    int cruiseSpeed;
    //LF Container
    byte lowFrequencyMask;
    int participantsReady;
    int startPlatoon;
    int endOfScenario;
    //MIO Container
    int mioID; 
    int mioRange; 
    int mioBearing; 
    int mioRangeRate; 
    //Lane Container
    int lane;
    //Pair ID Container
    int forwardID; 
    int backwardID; 
    //Merge Container
    int mergeRequest;
    int mergeSafeToMerge;
    int mergeFlag;
    int mergeFlagTail;
    int mergeFlagHead;
    //Intersection Container
    int platoonID;
    int distanceTravelledCz;
    int intention;
    int counter;    

    /* For creating a local iCLCM from a UDP message as received from the vehicle control system. */
    LocalIclcm(byte[] receivedData){
        if(receivedData.length < LOCAL_iCLCM_LENGTH){
            logger.error("Local iCLCM is too short. Is: {} Should be: {}", 
                         receivedData.length, LOCAL_iCLCM_LENGTH);
            throw new IllegalArgumentException();            
        }
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);
        messageID = buffer.get();

        
        stationID = buffer.getInt();
        containerMask = buffer.get();
        //HW Container
        rearAxleLocation = buffer.getInt();
        controllerType = buffer.getInt();
        responseTimeConstant = buffer.getInt();
        responseTimeDelay = buffer.getInt();
        targetLongAcc = buffer.getInt();
        timeHeadway = buffer.getInt();
        cruiseSpeed = buffer.getInt();
        //LF Container
        lowFrequencyMask = buffer.get();
        participantsReady = buffer.getInt();
        startPlatoon = buffer.getInt();
        endOfScenario = buffer.getInt();
        //MIO Container
        mioID = buffer.getInt();
        mioRange = buffer.getInt();
        mioBearing = buffer.getInt();
        mioRangeRate = buffer.getInt();
        //Lane container
        lane = buffer.getInt();
        //Pair ID container
        forwardID = buffer.getInt();
        backwardID = buffer.getInt();
        //Merge container
        mergeRequest = buffer.getInt();
        mergeSafeToMerge = buffer.getInt();
        mergeFlag = buffer.getInt();
        mergeFlagTail = buffer.getInt();
        mergeFlagHead = buffer.getInt();
        //Intersection Container
        platoonID = buffer.getInt();
        distanceTravelledCz = buffer.getInt();
        intention = buffer.getInt();
        counter = buffer.getInt();

        /* Verify that the values are correct and attempt to replace
         * any errors with default values. */
        if(messageID != net.gcdc.camdenm.Iclcm.MessageID_iCLCM){
            logger.error("MessageID is: {} Should be: {}",         
                         messageID, net.gcdc.camdenm.Iclcm.MessageID_iCLCM);
            throw new IllegalArgumentException();            
        }        
        
        if(!checkInt(StationID.class, stationID, "StationID")){ throw new IllegalArgumentException(); }
        if(!checkInt(VehicleRearAxleLocation.class, rearAxleLocation, "RearAxleLocation")){ throw new IllegalArgumentException(); }
        if(!checkInt(ControllerType.class, controllerType, "ControllerType")){ throw new IllegalArgumentException(); }
        if(!checkInt(VehicleResponseTimeConstant.class, responseTimeConstant, "ResponseTimeConstant")){ responseTimeConstant = VehicleResponseTimeConstant.unavailable; }
        if(!checkInt(VehicleResponseTimeDelay.class, responseTimeDelay, "ResponseTimeDelay")){ responseTimeDelay = VehicleResponseTimeDelay.unavailable; }
        if(!checkInt(TargetLongitudonalAcceleration.class, targetLongAcc, "TargetLongitudinalAcceleration")){ targetLongAcc = TargetLongitudonalAcceleration.unavailable; }
        if(!checkInt(TimeHeadway.class, timeHeadway, "TimeHeadway")){ timeHeadway = TimeHeadway.unavailable; }
        if(!checkInt(CruiseSpeed.class, cruiseSpeed, "CruiseSpeed")){ cruiseSpeed = CruiseSpeed.unavailable; }

        if(this.hasLowFrequencyContainer()){
            if(this.hasParticipantsReady())
                if(!checkInt(ParticipantsReady.class, participantsReady, "ParticipantsReady")){ throw new IllegalArgumentException(); }

            if(this.hasStartPlatoon())
                if(!checkInt(StartPlatoon.class, startPlatoon, "StartPlatoon")){ throw new IllegalArgumentException(); }

            if(this.hasEndOfScenario())
                if(!checkInt(EndOfScenario.class, endOfScenario, "EndOfScenario")){ throw new IllegalArgumentException(); }
        }
        
        if(!checkInt(StationID.class, mioID, "MioID")){ throw new IllegalArgumentException(); }
        if(!checkInt(MioRange.class, mioRange, "MioRange")){ mioRange = MioRange.unavailable; }
        if(!checkInt(MioBearing.class, mioBearing, "MioBearing")){ mioBearing = MioBearing.unavailable; }
        if(!checkInt(MioRangeRate.class, mioRangeRate, "MioRangeRate")){ mioRangeRate = MioRangeRate.unavailable; }
        if(!checkInt(Lane.class, lane, "Lane")){ lane = Lane.unavailable; }
        if(!checkInt(StationID.class, forwardID, "ForwardID")){ throw new IllegalArgumentException(); }
        if(!checkInt(StationID.class, backwardID, "BackwardID")){ throw new IllegalArgumentException(); }
        if(!checkInt(MergeRequest.class, mergeRequest, "MergeRequest")){ throw new IllegalArgumentException(); }
        if(!checkInt(MergeSafeToMerge.class, mergeSafeToMerge, "MergeSafeToMerge")){ throw new IllegalArgumentException(); }
        if(!checkInt(MergeFlag.class, mergeFlag, "MergeFlag")){ throw new IllegalArgumentException(); }
        if(!checkInt(MergeFlagTail.class, mergeFlagTail, "MergeFLagTail")){ throw new IllegalArgumentException(); }
        if(!checkInt(MergeFlagHead.class, mergeFlagHead, "MergeFlagHead")){ throw new IllegalArgumentException(); }
        if(!checkInt(PlatoonID.class, platoonID, "PlatoonID")){ throw new IllegalArgumentException(); }
        if(!checkInt(DistanceTravelledCZ.class, distanceTravelledCz, "DistanceTravelledCz")){ distanceTravelledCz = 0; }
        if(!checkInt(Intention.class, intention, "Intention")){ throw new IllegalArgumentException(); }
        if(!checkInt(Counter.class, counter, "Counter")){ counter = 0; }
    }
    
    /* For creating a local iCLCM from a iCLCM message as received from another ITS station. */
    LocalIclcm(IgameCooperativeLaneChangeMessage iCLCM){
        IgameCooperativeLaneChangeMessageBody iclcm = iCLCM.getIclm();
        ItsPduHeader header = iCLCM.getHeader();
        messageID = (byte) header.getMessageID().value;
        stationID = (int) header.getStationID().value;
        IclmParameters iclmParameters = iclcm.getIclmParameters();
        containerMask = 0;        

        if(messageID != net.gcdc.camdenm.Iclcm.MessageID_iCLCM){
            logger.warn("Malformed message on BTP port 2010 from station with ID {}", stationID);
            throw new IllegalArgumentException("Malformed message on BTP port 2010");
        }
        
        /* VehicleContainerHighFrequency */
        VehicleContainerHighFrequency vehicleContainerHighFrequency = iclmParameters.getVehicleContainerHighFrequency();
        rearAxleLocation = (int) vehicleContainerHighFrequency.getVehicleRearAxleLocation().value;        
        controllerType = (int) vehicleContainerHighFrequency.getControllerType().value;        
        responseTimeConstant = (int) vehicleContainerHighFrequency.getVehicleResponseTime().getVehicleResponseTimeConstant().value;
        responseTimeDelay = (int) vehicleContainerHighFrequency.getVehicleResponseTime().getVehicleResponseTimeDelay().value;
        targetLongAcc = (int) vehicleContainerHighFrequency.getTargetLongitudinalAcceleration().value;        
        timeHeadway = (int) vehicleContainerHighFrequency.getTimeHeadway().value;
        cruiseSpeed = (int) vehicleContainerHighFrequency.getCruisespeed().value;        

        /* VehicleContainerLowFrequency */
        VehicleContainerLowFrequency lowFrequencyContainer = null;
        lowFrequencyMask = 0;            
        if(iclmParameters.hasLowFrequencyContainer()){
            containerMask += (1<<7);
            lowFrequencyContainer = iclmParameters.getLowFrequencyContainer();

            if(lowFrequencyContainer.hasParticipantsReady()){
                lowFrequencyMask += (1<<7);                
                participantsReady = (int) lowFrequencyContainer.getParticipantsReady().value;                
            }           

            if(lowFrequencyContainer.hasStartPlatoon()){
                lowFrequencyMask += (1<<6);                                
		startPlatoon = (int) lowFrequencyContainer.getStartPlatoon().value;                
            }

            if(lowFrequencyContainer.hasEndOfScenario()){
                lowFrequencyMask += (1<<5);                                
		endOfScenario = (int) lowFrequencyContainer.getEndOfScenario().value;                
            }
        }
        
        /* MostImportantObjectContainer */
        MostImportantObjectContainer mostImportantObjectContainer = iclmParameters.getMostImportantObjectContainer();
        mioID = (int) mostImportantObjectContainer.getMioID().value;        
        mioRange = (int) mostImportantObjectContainer.getMioRange().value;        
        mioBearing = (int) mostImportantObjectContainer.getMioBearing().value();        
        mioRangeRate = (int) mostImportantObjectContainer.getMioRangeRate().value();        

        /* LaneObject */
        LaneObject laneObject = iclmParameters.getLaneObject();
        lane = (int) laneObject.getLane().value();        

        /* PairIdObject */
        PairIdObject pairIdObject = iclmParameters.getPairIdObject();
        forwardID = (int) pairIdObject.getForwardID().value;
        backwardID = (int) pairIdObject.getBackwardID().value;

        /* MergeObject */
        MergeObject mergeObject = iclmParameters.getMergeObject();
        mergeRequest = (int) mergeObject.getMergeRequest().value;        
        mergeSafeToMerge = (int) mergeObject.getMergeSafeToMerge().value;        
        mergeFlag = (int) mergeObject.getMergeFlag().value;        
        mergeFlagTail = (int) mergeObject.getMergeFlagTail().value;        
        mergeFlagHead = (int) mergeObject.getMergeFlagHead().value;        

        /* ScenarioObject */
        ScenarioObject scenarioObject = iclmParameters.getScenarioObject();
        platoonID = (int) scenarioObject.getPlatoonID().value;        
        distanceTravelledCz = (int) scenarioObject.getDistanceTravelledCZ().value;        
        intention = (int) scenarioObject.getIntention().value;        
        counter = (int) scenarioObject.getCounterIntersection().value;      
    }

    /* Return true if the local CAM has a low frequency container. */
    boolean hasLowFrequencyContainer(){
        return (containerMask & (1<<7)) != 0;
    }

    boolean hasParticipantsReady(){
        return (lowFrequencyMask & (1<<7)) != 0;
    }

    boolean hasStartPlatoon(){
        return (lowFrequencyMask & (1<<6)) != 0;
    }

    boolean hasEndOfScenario(){
        return (lowFrequencyMask & (1<<5)) != 0;
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


    /* Check if the local iCLCM is valid. */
    boolean isValid(){
        boolean valid = true;

        if(!checkInt(StationID.class, stationID, "StationID")) valid = false;
        if(!checkInt(VehicleRearAxleLocation.class, rearAxleLocation, "RearAxleLocation")) valid = false;
        if(!checkInt(ControllerType.class, controllerType, "ControllerType")) valid = false;
        if(!checkInt(VehicleResponseTimeConstant.class, responseTimeConstant, "ResponseTimeConstant")) valid = false;
        if(!checkInt(VehicleResponseTimeDelay.class, responseTimeDelay, "ResponseTimeDelay")) valid = false;
        if(!checkInt(TargetLongitudonalAcceleration.class, targetLongAcc, "TargetLongitudinalAcceleration")) valid = false;
        if(!checkInt(TimeHeadway.class, timeHeadway, "TimeHeadway")) valid = false;
        if(!checkInt(CruiseSpeed.class, cruiseSpeed, "CruiseSpeed")) valid = false;
        if(!checkInt(ParticipantsReady.class, participantsReady, "ParticipantsReady")) valid = false;
        if(!checkInt(StartPlatoon.class, startPlatoon, "StartPlatoon")) valid = false;
        if(!checkInt(EndOfScenario.class, endOfScenario, "EndOfScenario")) valid = false;
        if(!checkInt(StationID.class, mioID, "MioID")) valid = false;
        if(!checkInt(MioRange.class, mioRange, "MioRange")) valid = false;
        if(!checkInt(MioBearing.class, mioBearing, "MioBearing")) valid = false;
        if(!checkInt(MioRangeRate.class, mioRangeRate, "MioRangeRate")) valid = false;
        if(!checkInt(Lane.class, lane, "Lane")) valid = false;
        if(!checkInt(StationID.class, forwardID, "ForwardID")) valid = false;
        if(!checkInt(StationID.class, backwardID, "BackwardID")) valid = false;
        if(!checkInt(MergeRequest.class, mergeRequest, "MergeRequest")) valid = false;
        if(!checkInt(MergeSafeToMerge.class, mergeSafeToMerge, "MergeSafeToMerge")) valid = false;
        if(!checkInt(MergeFlag.class, mergeFlag, "MergeFlag")) valid = false;
        if(!checkInt(MergeFlagTail.class, mergeFlagTail, "MergeFLagTail")) valid = false;
        if(!checkInt(MergeFlagHead.class, mergeFlagHead, "MergeFlagHead")) valid = false;
        if(!checkInt(PlatoonID.class, platoonID, "PlatoonID")) valid = false;
        if(!checkInt(DistanceTravelledCZ.class, distanceTravelledCz, "DistanceTravelledCz")) valid = false;
        if(!checkInt(Intention.class, intention, "Intention")) valid = false;
        if(!checkInt(Counter.class, counter, "Counter")) valid = false;        
        return valid;
    }

    /* Return values as a byte array for sending as a local iCLCM UDP message. */
    byte[] asByteArray(){
        byte[] packetBuffer = new byte[LOCAL_iCLCM_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        buffer.put(messageID);
        buffer.putInt(stationID);
        buffer.put(containerMask);
        //HW Container
        buffer.putInt(rearAxleLocation);
        buffer.putInt(controllerType);
        buffer.putInt(responseTimeConstant);
        buffer.putInt(responseTimeDelay);
        buffer.putInt(targetLongAcc);
        buffer.putInt(timeHeadway);
        buffer.putInt(cruiseSpeed);
        //LF Container
        buffer.put(lowFrequencyMask);
        buffer.putInt(participantsReady);
        buffer.putInt(startPlatoon);
        buffer.putInt(endOfScenario);
        //MIO Container
        buffer.putInt(mioID);
        buffer.putInt(mioRange);
        buffer.putInt(mioBearing);
        buffer.putInt(mioRangeRate);
        //Lane container
        buffer.putInt(lane);
        //Pair ID container
        buffer.putInt(forwardID);
        buffer.putInt(backwardID);
        //Merge container
        buffer.putInt(mergeRequest);
        buffer.putInt(mergeSafeToMerge);
        buffer.putInt(mergeFlag);
        buffer.putInt(mergeFlagTail);
        buffer.putInt(mergeFlagHead);
        //Intersection Container
        buffer.putInt(platoonID);
        buffer.putInt(distanceTravelledCz);
        buffer.putInt(intention);
        buffer.putInt(counter);
        return packetBuffer;
    }

    /* Return values as a proper iCLCM message for sending to another ITS station. */
    IgameCooperativeLaneChangeMessage asIclcm(){
        VehicleContainerHighFrequency vehicleContainerHighFrequency =
            new VehicleContainerHighFrequency(new VehicleRearAxleLocation(rearAxleLocation),
                                              new ControllerType(controllerType),
                                              new VehicleResponseTime(new VehicleResponseTimeConstant(responseTimeConstant),
                                                                      new VehicleResponseTimeDelay(responseTimeDelay)),
                                              new TargetLongitudonalAcceleration(targetLongAcc),
                                              new TimeHeadway(timeHeadway),
                                              new CruiseSpeed(cruiseSpeed));

        VehicleContainerLowFrequency vehicleContainerLowFrequency =
            (containerMask & (1<<7)) != 0 ?
            VehicleContainerLowFrequency.builder()
            .participantsReady((lowFrequencyMask & (1<<7)) != 0 ? new ParticipantsReady(participantsReady) : null)
            .startPlatoon((lowFrequencyMask & (1<<6)) != 0 ? new StartPlatoon(startPlatoon) : null)
            .endOfScenario((lowFrequencyMask & (1<<5)) != 0 ? new EndOfScenario(endOfScenario) : null)
            .create()
            : null;

        MostImportantObjectContainer mostImportantObjectContainer =
            new MostImportantObjectContainer(new StationID(mioID),
                                             new MioRange(mioRange),
                                             new MioBearing(mioBearing),
                                             new MioRangeRate(mioRangeRate));       

        LaneObject laneObject =
            new LaneObject(new Lane(lane));

        PairIdObject pairIdObject =
            new PairIdObject(new StationID(forwardID),
                             new StationID(backwardID),
                             new AcknowledgeFlag());

        MergeObject mergeObject =
            new MergeObject(new MergeRequest(mergeRequest),
                            new MergeSafeToMerge(mergeSafeToMerge),
                            new MergeFlag(mergeFlag),
                            new MergeFlagTail(mergeFlagTail),
                            new MergeFlagHead(mergeFlagHead));

        ScenarioObject scenarioObject =            
            new ScenarioObject(new PlatoonID(platoonID),
                               new DistanceTravelledCZ(distanceTravelledCz),
                               new Intention(intention),
                               new Counter(counter));

        IclmParameters iclmParameters =
            new IclmParameters(vehicleContainerHighFrequency,
                               vehicleContainerLowFrequency,
                               mostImportantObjectContainer,
                               laneObject,
                               pairIdObject,
                               mergeObject,
                               scenarioObject);

        //TODO: GenerationDeltaTime isn't part of the iCLCM spec in D3.2
        IgameCooperativeLaneChangeMessageBody igameCooperativeLaneChangeMessageBody =
            new IgameCooperativeLaneChangeMessageBody(new GenerationDeltaTime(),
                                                      iclmParameters);
                                                                                 

        return new IgameCooperativeLaneChangeMessage(new ItsPduHeader(new ProtocolVersion(1),
                                                                      new MessageId(net.gcdc.camdenm.Iclcm.MessageID_iCLCM),
                                                                      new StationID(stationID)),
                                                     igameCooperativeLaneChangeMessageBody);
    }    
}
