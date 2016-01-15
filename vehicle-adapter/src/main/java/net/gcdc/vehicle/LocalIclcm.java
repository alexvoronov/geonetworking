package net.gcdc.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;

import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.camdenm.Iclcm.*;

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
        }
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);
        messageID = buffer.get();
        if(messageID != net.gcdc.camdenm.Iclcm.MessageID_iCLCM){
            logger.error("Local CAM has incorrect id. Id: {} Should be: {}",
                         messageID, net.gcdc.camdenm.Iclcm.MessageID_iCLCM);
        }
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
    }
    
    /* For creating a local iCLCM from a iCLCM message as received from another ITS station. */
    LocalIclcm(IgameCooperativeLaneChangeMessage iCLCM){
        IgameCooperativeLaneChangeMessageBody iclcm = iCLCM.getIclm();
        ItsPduHeader header = iCLCM.getHeader();
        messageID = (byte) header.getMessageID().value;
        stationID = (int) header.getStationID().value;
        IclmParameters iclmParameters = iclcm.getIclmParameters();
        containerMask = 0;

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
