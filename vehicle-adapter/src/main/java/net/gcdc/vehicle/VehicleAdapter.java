package net.gcdc.vehicle;

import java.io.IOException;
import java.nio.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.geonetworking.Area;
import net.gcdc.geonetworking.Area.*;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination.Geobroadcast;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.InvalidOptionSpecificationException;
import com.lexicalscope.jewel.cli.Option;

/*
import net.gcdc.camdenm.CoopIts.AccelerationControl;
import net.gcdc.camdenm.CoopIts.AccelerationConfidence;
import net.gcdc.camdenm.CoopIts.ActionID;
import net.gcdc.camdenm.CoopIts.AlacarteContainer;
import net.gcdc.camdenm.CoopIts.Altitude;
import net.gcdc.camdenm.CoopIts.AltitudeConfidence;
import net.gcdc.camdenm.CoopIts.AltitudeValue;
import net.gcdc.camdenm.CoopIts.BasicContainer;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerLowFrequency;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.CamParameters;
import net.gcdc.camdenm.CoopIts.CoopAwareness;
import net.gcdc.camdenm.CoopIts.Curvature;
import net.gcdc.camdenm.CoopIts.CurvatureConfidence;
import net.gcdc.camdenm.CoopIts.CurvatureValue;
import net.gcdc.camdenm.CoopIts.DangerousGoodsBasic;
import net.gcdc.camdenm.CoopIts.DangerousGoodsContainer;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.camdenm.CoopIts.DecentralizedEnvironmentalNotificationMessage;
import net.gcdc.camdenm.CoopIts.DriveDirection;
import net.gcdc.camdenm.CoopIts.EmergencyContainer;
import net.gcdc.camdenm.CoopIts.ExteriorLights;
import net.gcdc.camdenm.CoopIts.GenerationDeltaTime;
import net.gcdc.camdenm.CoopIts.Heading;
import net.gcdc.camdenm.CoopIts.HeadingConfidence;
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.LightBarSirenInUse;
import net.gcdc.camdenm.CoopIts.LocationContainer;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.LongitudinalAcceleration;
import net.gcdc.camdenm.CoopIts.LongitudinalAccelerationValue;
import net.gcdc.camdenm.CoopIts.LowFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ManagementContainer;
import net.gcdc.camdenm.CoopIts.PathHistory;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
import net.gcdc.camdenm.CoopIts.PtActivation;
import net.gcdc.camdenm.CoopIts.PtActivationData;
import net.gcdc.camdenm.CoopIts.PtActivationType;
import net.gcdc.camdenm.CoopIts.PublicTransportContainer;
import net.gcdc.camdenm.CoopIts.ReferencePosition;
import net.gcdc.camdenm.CoopIts.RescueContainer;
import net.gcdc.camdenm.CoopIts.RoadWorksContainerBasic;
import net.gcdc.camdenm.CoopIts.SafetyCarContainer;
import net.gcdc.camdenm.CoopIts.SemiAxisLength;
import net.gcdc.camdenm.CoopIts.SequenceNumber;
import net.gcdc.camdenm.CoopIts.SpecialTransportContainer;
import net.gcdc.camdenm.CoopIts.SpecialTransportType;
import net.gcdc.camdenm.CoopIts.SpecialVehicleContainer;
import net.gcdc.camdenm.CoopIts.Speed;
import net.gcdc.camdenm.CoopIts.SpeedConfidence;
import net.gcdc.camdenm.CoopIts.SpeedValue;
import net.gcdc.camdenm.CoopIts.StationType;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.SituationContainer;
import net.gcdc.camdenm.CoopIts.Termination;
import net.gcdc.camdenm.CoopIts.TimestampIts;
import net.gcdc.camdenm.CoopIts.VehicleLength;
import net.gcdc.camdenm.CoopIts.VehicleLengthValue;
import net.gcdc.camdenm.CoopIts.VehicleLengthConfidenceIndication;
import net.gcdc.camdenm.CoopIts.VehicleRole;
import net.gcdc.camdenm.CoopIts.VehicleWidth;
import net.gcdc.camdenm.CoopIts.YawRate;
import net.gcdc.camdenm.CoopIts.YawRateConfidence;
import net.gcdc.camdenm.CoopIts.YawRateValue;
*/
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;

import net.gcdc.camdenm.Iclcm.*;

import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.Optional;
//import net.gcdc.geonetworking.StationType;

import org.threeten.bp.Instant;

public class VehicleAdapter {
    private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);

    private final DatagramSocket rcvSocket;
    private final GeonetStation station;
    private final BtpSocket btpSocket;

    private final static short PORT_CAM  = 2001;
    private final static short PORT_DENM = 2002;
    private final static short PORT_ICLCM = 2010;

    /* GCDC requires the non-standard max rate of 25Hz */
    private final static long CAM_INTERVAL_MIN_MS = 40;
    private final static long CAM_INTERVAL_MAX_MS = 1000;

    /* GCDC requires 1Hz for the low frequency container */
    private final static long CAM_LOW_FREQ_INTERVAL_MS = 1000;

    private final static long CAM_INITIAL_DELAY_MS = 20;  // At startup.

    private final static int HIGH_DYNAMICS_CAM_COUNT = 4;

    public final static double CAM_LIFETIME_SECONDS = 0.9;
    public final static double iCLCM_LIFETIME_SECONDS = 0.9;

    //public final static int MAX_UDP_LENGTH = 65535;
    public final static int MAX_UDP_LENGTH = 300;

    //TODO: Remove and use CLI arguments instead
    public static int simulink_cam_port = 5000;
    public static int simulink_denm_port = simulink_cam_port + 1;
    public static int simulink_iclcm_port = simulink_denm_port + 1;

    public static InetAddress SIMULINK_ADDRESS;

    public static final ExecutorService executor = Executors.newCachedThreadPool();

    public static VehiclePositionProvider vehiclePositionProvider;

    /* Unpack a message from Simulink and create a CAM message. The
     * Simulink message must be formatted according to the local
     * message set defined here:
     * https://github.com/Zeverin/GCDC16-Chalmers-Communication/tree/master/Documentation
     *
     * Please note that this is the first draft and that everything
     * may change :)
     */
    public Cam simulinkToCam(byte[] receivedData){
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);

        try{
            byte messageId = buffer.get();
            if(messageId != MessageId.cam){
                logger.error("Incorrect local CAM received: " + receivedData);
                return null;
            }


            /* Unpack up til longitude here since we need them to
             * update the position stored in the vehicle adapter.
             */
            int stationId = buffer.getInt();
            int genDeltaTimeMillis = buffer.getInt();            
            byte containerMask = buffer.get();            
            int stationType = buffer.getInt();
            int latitude = buffer.getInt();
            int longitude = buffer.getInt();            

            Cam cam = createCam(stationId,
                                genDeltaTimeMillis,
                                containerMask,
                                stationType,
                                latitude,
                                longitude,
                                buffer.getInt(), /* semiMajorAxisConfidence */
                                buffer.getInt(), /* semiMinorAxisConfidence */
                                buffer.getInt(), /* semiMajorOrientation */
                                buffer.getInt(), /* altitude */
                                buffer.getInt(), /* heading */
                                buffer.getInt(), /* headingConfidence */
                                buffer.getInt(), /* speed */
                                buffer.getInt(), /* speedConfidence */
                                buffer.getInt(), /* VehicleLength */
                                buffer.getInt(), /* VehicleWidth */                                
                                buffer.getInt(), /* longitudinalAcceleration */
                                buffer.getInt(), /* longitudinalAccelerationConfidence */
                                buffer.getInt(), /* yawRate */
                                buffer.getInt(), /* yawRateConfidence */
                                buffer.getInt());/* vehicleRole */                                    
            
            //TODO: Thread crashes when running this...
            //vehiclePositionProvider.updatePosition(latitude, longitude);
            return cam;
            
        }catch(BufferUnderflowException e){
            logger.error("Failed to create CAM from Simulink message: " + e);
            return null;
        }
    }

    /* Unpack a CAM message and create a Simulink message.
     */
    public void camToSimulink(Cam camPacket, byte[] packetBuffer) throws BufferOverflowException{
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);

        CoopAwareness cam = camPacket.getCam();
        ItsPduHeader header = camPacket.getHeader();
        GenerationDeltaTime generationDeltaTime = cam.getGenerationDeltaTime();         
        CamParameters camParameters = cam.getCamParameters();

        buffer.put((byte) header.getMessageID().value);
        buffer.putInt((int)header.getStationID().value);
        buffer.putInt((int) generationDeltaTime.value);
        byte containerMask = 0;
        buffer.put(containerMask);

        int headerLength = 2*1+2*4;

        /* BasicContainer */
        BasicContainer basicContainer = camParameters.getBasicContainer();
        buffer.putInt((int) basicContainer.getStationType().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getLatitude().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getLongitude().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorConfidence().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMinorConfidence().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getPositionConfidenceEllipse().getSemiMajorOrientation().value);
        buffer.putInt((int) basicContainer.getReferencePosition().getAltitude().getAltitudeValue().value);
        
        /* HighFrequencyContainer */
        HighFrequencyContainer highFrequencyContainer = camParameters.getHighFrequencyContainer();
        BasicVehicleContainerHighFrequency basicVehicleContainerHighFrequency = highFrequencyContainer.getBasicVehicleContainerHighFrequency();

        buffer.putInt((int) basicVehicleContainerHighFrequency.getHeading().getHeadingValue().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getHeading().getHeadingConfidence().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getSpeed().getSpeedValue().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getSpeed().getSpeedConfidence().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getVehicleLength().getVehicleLengthValue().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getVehicleWidth().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationValue().value());
        buffer.putInt((int) basicVehicleContainerHighFrequency.getLongitudinalAcceleration().getLongitudinalAccelerationConfidence().value());
        buffer.putInt((int) basicVehicleContainerHighFrequency.getYawRate().getYawRateValue().value);
        buffer.putInt((int) basicVehicleContainerHighFrequency.getYawRate().getYawRateConfidence().value());


        /* LowFrequencyContainer */
        LowFrequencyContainer lowFrequencyContainer = null;
        if(camParameters.hasLowFrequencyContainer()){
            containerMask += (1<<7);
            lowFrequencyContainer = camParameters.getLowFrequencyContainer();
            BasicVehicleContainerLowFrequency basicVehicleContainerLowFrequency = lowFrequencyContainer.getBasicVehicleContainerLowFrequency();
            buffer.putInt((int) basicVehicleContainerLowFrequency.getVehicleRole().value());
        }else buffer.putInt(0);

        //Replace container mask
        buffer.put(headerLength - 1, containerMask);
    }

    public Cam createCam(int stationId,
                         int genDeltaTimeMillis,
                         byte containerMask,
                         int stationType,
                         int latitude,
                         int longitude,                         
                         int semiMajorAxisConfidence,
                         int semiMinorAxisConfidence,
                         int semiMajorOrientation,
                         int altitude,
                         int heading,
                         int headingConfidence,
                         int speed,
                         int speedConfidence,
                         int vehicleLength,
                         int vehicleWidth,
                         int longitudinalAcceleration,
                         int longitudinalAccelerationConfidence,
                         int yawRate,
                         int yawRateConfidence,
                         int vehicleRole){                         

        LowFrequencyContainer lowFrequencyContainer = (containerMask & (1<<7)) != 0 ?
            new LowFrequencyContainer(
                                      new BasicVehicleContainerLowFrequency(
                                                                            VehicleRole.fromCode(vehicleRole),
                                                                            //TODO: Implement ExteriorLights in LMS?
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
                                       .vehicleLength(new VehicleLength(new VehicleLengthValue(vehicleLength),
                                                                        VehicleLengthConfidenceIndication.unavailable))
                                       .vehicleWidth(new VehicleWidth(vehicleWidth))
                                       .longitudinalAcceleration(new LongitudinalAcceleration(new LongitudinalAccelerationValue(longitudinalAcceleration),
                                                                                              new AccelerationConfidence(longitudinalAccelerationConfidence)))
                                       .yawRate(new YawRate(new YawRateValue(yawRate),
                                                            //TODO: This code is slow. Cache YawRateConfidence.values() if it's a problem.
                                                            YawRateConfidence.values()[yawRateConfidence]))
                                       .create()
                                       );
            return new Cam(
                           new ItsPduHeader(new ProtocolVersion(1),
                                            new MessageId(MessageId.cam),
                                            new StationID(stationId)),
                    new CoopAwareness(
                            new GenerationDeltaTime(genDeltaTimeMillis * GenerationDeltaTime.oneMilliSec),
                            new CamParameters(basicContainer,
                                              highFrequencyContainer,
                                              lowFrequencyContainer,
                                              specialVehicleContainer)));
    }


    /* Unpack a local DENM and call the createDenm method with the
     * unpacked values. */
    public Denm simulinkToDenm(byte[] receivedData){
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);

        try{
            byte messageId = buffer.get();
            if(messageId != MessageId.denm){
                logger.error("Incorrect local DENM received: " + receivedData);
                return null;
            }
            
            return createDenm(buffer.getInt(),  /* stationID */
                              buffer.getInt(),  /* GenerationDeltaTime */
                              buffer.get(),     /* containerMask */
                              buffer.get(),     /* managementMask */
                              buffer.getInt(),  /* detectionTime */
                              buffer.getInt(),  /* referenceTime */
                              buffer.getInt(),  /* termination */
                              buffer.getInt(),  /* latitude */
                              buffer.getInt(),  /* longitude */
                              buffer.getInt(),  /* semiMajorConfidence */
                              buffer.getInt(),  /* semiMinorConfidence */
                              buffer.getInt(),  /* semiMajorOrientation */
                              buffer.getInt(),  /* altitude */
                              buffer.getInt(),  /* relevanceDistance */
                              buffer.getInt(),  /* relevanceTrafficDirection */
                              buffer.getInt(),  /* ValidityDuration */
                              buffer.getInt(),  /* transmissionInterval */
                              buffer.getInt(),  /* stationType */
                              buffer.get(),     /* situationMask */
                              buffer.getInt(),  /* informationQuality */
                              buffer.getInt(),  /* causeCode */
                              buffer.getInt(),  /* subCauseCode */
                              buffer.getInt(),  /* linkedCauseCode */
                              buffer.getInt(),  /* linkedSubCauseCode */
                              buffer.get(),     /* alacarteMask */
                              buffer.getInt(),  /* lanePosition*/
                              buffer.getInt(),  /* temperature */
                              buffer.getInt()); /* positioningSolutionType */
            
        }catch(BufferUnderflowException e){
            logger.error("Failed to create DENM from Simulink message: " + e);
            return null;
        }
    }

    /* Unpack a DENM and create a local DENM for sending to Simulink.
     */
    public void denmToSimulink(Denm denmPacket, byte[] packetBuffer){
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);

        DecentralizedEnvironmentalNotificationMessage denm = denmPacket.getDenm();
        ItsPduHeader header = denmPacket.getHeader();
        ManagementContainer managementContainer = denm.getManagement();        
        buffer.put((byte) header.getMessageID().value);
        buffer.putInt((int) header.getStationID().value);
        buffer.putInt((int) managementContainer.getReferenceTime().value % 65536);

        
        byte containerMask = 0;
        buffer.put(containerMask);

        int headerLength = 1+2*4+1;

        /* ManagementContainer */
        int managementLength = 1 + 14*4;
        byte managementMask = 0;
        buffer.put(managementMask);        
        
        //buffer.putInt((int) managementContainer.getActionID().getOriginatingStationID().value);       
        buffer.putInt((int) managementContainer.getDetectionTime().value / 65536);
        buffer.putInt((int) managementContainer.getReferenceTime().value / 65536);

        if(managementContainer.hasTermination()){
            managementMask += (1<<7);            
            buffer.putInt((int) managementContainer.getTermination().value());
        }else buffer.putInt(0);        
        
        buffer.putInt((int) managementContainer.getEventPosition().getLatitude().value);
        buffer.putInt((int) managementContainer.getEventPosition().getLongitude().value);
        buffer.putInt((int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMajorConfidence().value);
        buffer.putInt((int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMinorConfidence().value);
        buffer.putInt((int) managementContainer.getEventPosition().getPositionConfidenceEllipse().getSemiMajorOrientation().value);
        buffer.putInt((int) managementContainer.getEventPosition().getAltitude().getAltitudeValue().value);

        if(managementContainer.hasRelevanceDistance()){
            managementMask += (1<<6);            
            buffer.putInt((int) managementContainer.getRelevanceDistance().value());
        }else buffer.putInt(0);

        if(managementContainer.hasRelevanceTrafficDirection()){
            managementMask += (1<<5);            
            buffer.putInt((int) managementContainer.getRelevanceTrafficDirection().value());
        }else buffer.putInt(0);

        if(managementContainer.hasValidityDuration()){
            managementMask += (1<<4);            
            buffer.putInt((int) managementContainer.getValidityDuration().value);
        }else buffer.putInt(0);
        
        if(managementContainer.hasTransmissionInterval()){
            managementMask += (1<<3);            
            buffer.putInt((int) managementContainer.getTransmissionInterval().value);
        }else buffer.putInt(0);        
        
        buffer.putInt((int) managementContainer.getStationType().value);

        //Need to update the mask since it has been changed
        buffer.put(headerLength, managementMask);        

        /* SituationContainer */
        SituationContainer situationContainer = null;
        int situationLength = 1 + 5*4;
        if(denm.hasSituation()){
            containerMask += (1<<7);
            situationContainer = denm.getSituation();
            byte situationMask = 0;
            buffer.put(situationMask);

            buffer.putInt((int) situationContainer.getInformationQuality().value);
            buffer.putInt((int) situationContainer.getEventType().getCauseCode().value);
            buffer.putInt((int) situationContainer.getEventType().getSubCauseCode().value);

            if(situationContainer.hasLinkedCause()){
                situationMask += (1<<7);
                buffer.putInt((int) situationContainer.getLinkedCause().getCauseCode().value);
                buffer.putInt((int) situationContainer.getLinkedCause().getSubCauseCode().value);
            }else buffer.put(new byte[2*4]);

            buffer.put(headerLength + managementLength, situationMask);
        }else buffer.put(new byte[situationLength]);                

        /* Not used for GCDC16 */
        int locationLength = 0;
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
                buffer.putInt((int) locationContainer.getEventSpeed().getSpeedValue().value);
                buffer.putInt((int) locationContainer.getEventSpeed().getSpeedConfidence().value);
            }else buffer.put(new byte[2*4]);
            
            if(locationContainer.hasEventSpeed()){                
                locationMask += (1<<6);                
                buffer.putInt((int) locationContainer.getEventPositionHeading().getHeadingValue().value);
                buffer.putInt((int) locationContainer.getEventPositionHeading().getHeadingConfidence().value);                
            }else buffer.put(new byte[2*4]);

            if(locationContainer.hasEventSpeed()){                
                locationMask += (1<<5);                
                buffer.putInt((int) locationContainer.getRoadType().value());                          
            }else buffer.putInt(0);

            //Need to update the mask since it has been changed
            buffer.put(headerLength + managementLength + situationLength, locationMask);
        }else buffer.put(new byte[locationLength]);
        */

        /* AlacarteContainer */
        AlacarteContainer alacarteContainer = null;
        int alacarteLength = 1 + 3*4;
        if(denm.hasAlacarte()){
            containerMask += (1<<5);            
            alacarteContainer = denm.getAlacarte();
            byte alacarteMask = 0;
            buffer.put(alacarteMask);            

            if(alacarteContainer.hasLanePosition()){
                alacarteMask += (1<<7);
                buffer.putInt((int) alacarteContainer.getLanePosition().value);                
            }else buffer.putInt(0);

            if(alacarteContainer.hasExternalTemperature()){
                alacarteMask += (1<<5);
                buffer.putInt((int) alacarteContainer.getExternalTemperature().value);                
            }else buffer.putInt(0);

            if(alacarteContainer.hasPositioningSolution()){
                alacarteMask += (1<<3);
                buffer.putInt((int) alacarteContainer.getPositioningSolution().value());
            }else buffer.putInt(0);

            //Need to update the mask since it has been changed
            buffer.put(headerLength + managementLength + situationLength + locationLength, alacarteMask);
        }else buffer.put(new byte[alacarteLength]);

        buffer.put(headerLength - 1, containerMask);
    }

    private int denm_sequence_number = 0;
    public Denm createDenm(int stationId,
                           int generationDeltaTime,
                           byte containerMask,
                           byte managementMask,
                           int detectionTime,
                           int referenceTime,
                           int termination,
                           int latitude,
                           int longitude,
                           int semiMajorConfidence,
                           int semiMinorConfidence,
                           int semiMajorOrientation,
                           int altitude,
                           int relevanceDistance,
                           int relevanceTrafficDirection,
                           int validityDuration,
                           int transmissionInterval,
                           int stationType,
                           int situationMask,
                           int informationQuality,
                           int causeCode,
                           int subCauseCode,
                           int linkedCauseCode,
                           int linkedSubCauseCode,
                           int alacarteMask,
                           int lanePosition,
                           int temperature,
                           int positioningSolutionType){

        /* Management container */
        ManagementContainer managementContainer =
            ManagementContainer.builder()
            .actionID(new ActionID(new StationID(stationId), new SequenceNumber(denm_sequence_number++)))
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

        Denm denm = new Denm(
                             new ItsPduHeader(new ProtocolVersion(1),
                                              new MessageId(MessageId.denm),
                                              new StationID(stationId)),
                             decentralizedEnvironmentalNotificationMessage);

        logger.debug("Created DENM: " + denm);
        return denm;
    }

    public IgameCooperativeLaneChangeMessage simulinkToIclcm(byte[] packet){
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        try{
            byte messageId = buffer.get();
            return createIclcm(buffer.getInt(), //stationId
                               buffer.get(),    //containerMask
                               //HW Container
                               buffer.getInt(), //rearAxleLocation
                               buffer.getInt(), //controllerType
                               buffer.getInt(), //responseTimeconstant
                               buffer.getInt(), //responseTimeDelay
                               buffer.getInt(), //targetLongAcc
                               buffer.getInt(), //timeHeadway
                               buffer.getInt(), //cruiseSpeed
                               //LF Container
                               buffer.get(),    //lfMask
                               buffer.getInt(), //participantsReady
                               buffer.getInt(), //startPlatoon
                               buffer.getInt(), //endOfScenario
                               //MIO Container
                               buffer.getInt(), //mioId
                               buffer.getInt(), //mioRange
                               buffer.getInt(), //mioBearing
                               buffer.getInt(), //mioRangeRate
                               //Lane container
                               buffer.getInt(), //lane
                               //Pair ID container
                               buffer.getInt(), //forwardId
                               buffer.getInt(), //backwardId
                               //Merge container
                               buffer.getInt(), //mergeRequest
                               buffer.getInt(), //mergeSafeToMerge
                               buffer.getInt(), //mergeFlag
                               buffer.getInt(), //mergeFlagTail
                               buffer.getInt(), //mergeFlagHead
                               //Intersection Container
                               buffer.getInt(), //platoonId
                               buffer.getInt(), //distanceTravelledCz
                               buffer.getInt(), //intention
                               buffer.getInt());//counter
        }catch(BufferUnderflowException e){
            logger.error("Failed to create iCLCM from Simulink message: " + e);
            return null;
        }
    }

    public void iclcmToSimulink(IgameCooperativeLaneChangeMessage iCLCM, byte[] packetBuffer){
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        IgameCooperativeLaneChangeMessageBody iclcm = iCLCM.getIclm();
        ItsPduHeader header = iCLCM.getHeader();
        buffer.put((byte) header.getMessageID().value);
        buffer.putInt((int) header.getStationID().value);
        IclmParameters iclmParameters = iclcm.getIclmParameters();
        byte containerMask = 0;
        buffer.put(containerMask);        
        int headerLength = 2+4;
        

        /* VehicleContainerHighFrequency */
        int highFrequencyLength = 7*4;
        VehicleContainerHighFrequency vehicleContainerHighFrequency = iclmParameters.getVehicleContainerHighFrequency();
        buffer.putInt((int) vehicleContainerHighFrequency.getVehicleRearAxleLocation().value);        
        buffer.putInt((int) vehicleContainerHighFrequency.getControllerType().value);        
        buffer.putInt((int) vehicleContainerHighFrequency.getVehicleResponseTime().getVehicleResponseTimeConstant().value);
        buffer.putInt((int) vehicleContainerHighFrequency.getVehicleResponseTime().getVehicleResponseTimeDelay().value);
        buffer.putInt((int) vehicleContainerHighFrequency.getTargetLongitudinalAcceleration().value);        
        buffer.putInt((int) vehicleContainerHighFrequency.getTimeHeadway().value);
        buffer.putInt((int) vehicleContainerHighFrequency.getCruisespeed().value);        

        /* VehicleContainerLowFrequency */
        int lowFrequencyLength = 1 + 3*4;        
        VehicleContainerLowFrequency lowFrequencyContainer = null;
        if(iclmParameters.hasLowFrequencyContainer()){
            containerMask += (1<<7);
            lowFrequencyContainer = iclmParameters.getLowFrequencyContainer();
            byte lowFrequencyMask = 0;            
            buffer.put(lowFrequencyMask);

            if(lowFrequencyContainer.hasParticipantsReady()){
                lowFrequencyMask += (1<<7);                
                buffer.putInt((int) lowFrequencyContainer.getParticipantsReady().value);                
            }else buffer.putInt(0);            

            if(lowFrequencyContainer.hasStartPlatoon()){
                lowFrequencyMask += (1<<6);                                
		buffer.putInt((int) lowFrequencyContainer.getStartPlatoon().value);                
            }else buffer.putInt(0);            

            if(lowFrequencyContainer.hasEndOfScenario()){
                lowFrequencyMask += (1<<5);                                
		buffer.putInt((int) lowFrequencyContainer.getEndOfScenario().value);                
            }else buffer.putInt(0);

            buffer.put(headerLength + highFrequencyLength, lowFrequencyMask);            
        }else buffer.put(new byte[lowFrequencyLength]);
        
        /* MostImportantObjectContainer */
        int mioLength = 4*4;
        MostImportantObjectContainer mostImportantObjectContainer = iclmParameters.getMostImportantObjectContainer();
        buffer.putInt((int) mostImportantObjectContainer.getMioID().value);        
        buffer.putInt((int) mostImportantObjectContainer.getMioRange().value);        
        buffer.putInt((int) mostImportantObjectContainer.getMioBearing().value());        
        buffer.putInt((int) mostImportantObjectContainer.getMioRangeRate().value());        

        /* LaneObject */
        int laneLength = 1*4;
        LaneObject laneObject = iclmParameters.getLaneObject();
        buffer.putInt((int) laneObject.getLane().value());        

        /* PairIdObject */
        int pairIdLength = 2*4;
        PairIdObject pairIdObject = iclmParameters.getPairIdObject();
        buffer.putInt((int) pairIdObject.getForwardID().value);
        buffer.putInt((int) pairIdObject.getBackwardID().value);

        /* MergeObject */
        int mergeLength =5*4;
        MergeObject mergeObject = iclmParameters.getMergeObject();
        buffer.putInt((int) mergeObject.getMergeRequest().value);        
        buffer.putInt((int) mergeObject.getMergeSafeToMerge().value);        
        buffer.putInt((int) mergeObject.getMergeFlag().value);        
        buffer.putInt((int) mergeObject.getMergeFlagTail().value);        
        buffer.putInt((int) mergeObject.getMergeFlagHead().value);        

        /* ScenarioObject */
        int scenarioLength = 4*4;
        ScenarioObject scenarioObject = iclmParameters.getScenarioObject();
        buffer.putInt((int) scenarioObject.getPlatoonID().value);        
        buffer.putInt((int) scenarioObject.getDistanceTravelledCZ().value);        
        buffer.putInt((int) scenarioObject.getIntention().value);        
        buffer.putInt((int) scenarioObject.getCounterIntersection().value);

        buffer.put(headerLength - 1, containerMask);
    }

    public IgameCooperativeLaneChangeMessage createIclcm(int stationId,
                                                         byte containerMask,
                                                         //HW Container
                                                         int rearAxleLocation,
                                                         int controllerType,
                                                         int responseTimeConstant,
                                                         int responseTimeDelay,
                                                         int targetLongAcc,
                                                         int timeHeadway,
                                                         int cruiseSpeed,
                                                         //LF Container
                                                         byte lfMask, 
                                                         int participantsReady,
                                                         int startPlatoon,
                                                         int endOfScenario,
                                                         //MIO Container
                                                         int mioId, 
                                                         int mioRange, 
                                                         int mioBearing, 
                                                         int mioRangeRate, 
                                                         //Lane Container
                                                         int lane,
                                                         //Pair ID Container
                                                         int forwardId, 
                                                         int backwardId, 
                                                         //Merge Container
                                                         int mergeRequest,
                                                         int mergeSafeToMerge,
                                                         int mergeFlag,
                                                         int mergeFlagTail,
                                                         int mergeFlagHead,
                                                         //Intersection Container
                                                         int platoonId,
                                                         int distanceTravelledCz,
                                                         int intention,
                                                         int counter){

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
            .participantsReady((lfMask & (1<<7)) != 0 ? new ParticipantsReady(participantsReady) : null)
            .startPlatoon((lfMask & (1<<6)) != 0 ? new StartPlatoon(startPlatoon) : null)
            .endOfScenario((lfMask & (1<<5)) != 0 ? new EndOfScenario(endOfScenario) : null)
            .create()
            : null;

        MostImportantObjectContainer mostImportantObjectContainer =
            new MostImportantObjectContainer(new StationID(mioId),
                                             new MioRange(mioRange),
                                             new MioBearing(mioBearing),
                                             new MioRangeRate(mioRangeRate));       

        LaneObject laneObject =
            new LaneObject(new Lane(lane));

        PairIdObject pairIdObject =
            new PairIdObject(new StationID(forwardId),
                             new StationID(backwardId),
                             new AcknowledgeFlag());

        MergeObject mergeObject =
            new MergeObject(new MergeRequest(mergeRequest),
                            new MergeSafeToMerge(mergeSafeToMerge),
                            new MergeFlag(mergeFlag),
                            new MergeFlagTail(mergeFlagTail),
                            new MergeFlagHead(mergeFlagHead));

        ScenarioObject scenarioObject =            
            new ScenarioObject(new PlatoonID(platoonId),
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
                                                                                 
        IgameCooperativeLaneChangeMessage igameCooperativeLaneChangeMessage =
            new IgameCooperativeLaneChangeMessage(new ItsPduHeader(new ProtocolVersion(1),
                                                                   new MessageId(net.gcdc.camdenm.Iclcm.MessageID_iCLCM),
                                                                   new StationID(stationId)),
                                                  igameCooperativeLaneChangeMessageBody);

        return igameCooperativeLaneChangeMessage;
    }

    private Runnable receiveFromSimulinkLoop = new Runnable() {
        byte[] buffer = new byte[MAX_UDP_LENGTH];
        private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        @Override public void run() {
            try {
                while (true) {
                    logger.debug("Waiting for packet from vehicle control...");
                    rcvSocket.receive(packet);
                    byte[] receivedData = Arrays.copyOfRange(packet.getData(),
                                                             packet.getOffset(),
                                                             packet.getOffset() + packet.getLength());
                    assert (receivedData.length == packet.getLength());

                    /* First byte is the MessageId */
                    switch(receivedData[0]){                        
                    case MessageId.cam: {
                        logger.debug("CREATING CAM");
                        Cam cam = simulinkToCam(receivedData);
                        send(cam);
                        break;
                    }

                    case MessageId.denm: {
                        logger.debug("CREATING DENM");
                        Denm denm = simulinkToDenm(receivedData);                  

                        /* TODO: How does GeoNetworking addressing work in
                         * GCDC16? For now let's just broadcast
                         * everything in a large radius.
                         */
                        send(denm, Geobroadcast.geobroadcast(Area.circle(vehiclePositionProvider.getPosition(), Double.MAX_VALUE)));
                        break;
                    }
                        
                    case net.gcdc.camdenm.Iclcm.MessageID_iCLCM: {
                        logger.debug("CREATING iCLCM");
                        IgameCooperativeLaneChangeMessage iclcm = simulinkToIclcm(receivedData);
                        send(iclcm);
                        break;
                    }
                        
                    default:
                        //fallthrough
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to receive packet from Simulink, terminating", e);
                System.exit(1);
            }
        }
        };


    /* TODO: Clean up the sending packets to Simulink part.
     */
    private Runnable sendToSimulinkLoop = new Runnable() {
            byte[] buffer = new byte[MAX_UDP_LENGTH];
            private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            //packet.setAddress(SIMULINK_ADDRESS.getAddress());
            
            @Override public void run() {
                try{
                    InetAddress address = InetAddress.getByName("127.0.0.1");
                    packet.setAddress(address);
                }catch(UnknownHostException e){
                    logger.error("Failed to create packet for sending to Simulink, terminating", e);
                    System.exit(1);
                }

                try {
                    while(true){
                        BtpPacket btpPacket = btpSocket.receive();
                        switch (btpPacket.destinationPort()) {
                        case PORT_CAM: {
                            Cam cam;
                            try {
                                cam = UperEncoder.decode(btpPacket.payload(), Cam.class);
                                camToSimulink(cam, buffer);
                            

                                packet.setPort(simulink_cam_port);
                                try {
                                    rcvSocket.send(packet);
                                } catch (IOException e) {
                                    logger.warn("Failed to send CAM to Simulink", e);
                                }
                            } catch (IllegalArgumentException | UnsupportedOperationException | BufferOverflowException e) {
                                logger.warn("Can't decode cam", e);
                            }
                            break;
                        }

                        case PORT_DENM: {
                            Denm denm;
                            try {
                                denm = UperEncoder.decode(btpPacket.payload(), Denm.class);
                                denmToSimulink(denm, buffer);

                                packet.setPort(simulink_denm_port);
                                try {
                                    rcvSocket.send(packet);
                                } catch (IOException e) {
                                    logger.warn("Failed to send DENM to Simulink", e);
                                }
                            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                                logger.warn("Can't decode denm", e);
                            }
                            break;                          
                        }

                        case PORT_ICLCM: {
                            IgameCooperativeLaneChangeMessage iclcm;
                            try {
                                iclcm = UperEncoder.decode(btpPacket.payload(), IgameCooperativeLaneChangeMessage.class);
                                iclcmToSimulink(iclcm, buffer);

                                packet.setPort(simulink_iclcm_port);
                                try {
                                    rcvSocket.send(packet);                                
                                } catch(IOException e) {
                                    logger.warn("Failed to send iCLCM to Simulink", e);
                                }
                            } catch(IllegalArgumentException | UnsupportedOperationException e){
                                logger.warn("Can't decode iclcm", e);
                            }
                            break;
                        }

                        default:
                            //fallthrough
                        }
                    }
                } catch (InterruptedException e) {
                    logger.warn("BTP socket receive was interrupted", e);
                }
            }
        };

    public void send(Cam cam) {
        byte[] bytes;
        try {
            bytes = UperEncoder.encode(cam);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            logger.warn("Failed to encode CAM {}, ignoring", cam, e);
            return;
        }
        BtpPacket packet = BtpPacket.singleHop(bytes, PORT_CAM, CAM_LIFETIME_SECONDS);
        try {
            btpSocket.send(packet);
        } catch (IOException e) {
            logger.warn("failed to send cam", e);
        }
    }

    private void send(Denm denm, Geobroadcast destination) {
        byte[] bytes;
        try {
            bytes = UperEncoder.encode(denm);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            logger.error("Failed to encode DENM", e);
            return;
        }
        BtpPacket packet = BtpPacket.customDestination(bytes, PORT_DENM, destination);
        try {
            btpSocket.send(packet);
        } catch (IOException e) {
            logger.warn("failed to send denm", e);
        }
    }

    private void send(IgameCooperativeLaneChangeMessage iclcm){
        byte[] bytes;
        try {
            bytes = UperEncoder.encode(iclcm);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            logger.error("Failed to encode iCLCM", e);
            return;
        }
        BtpPacket packet = BtpPacket.singleHop(bytes, PORT_ICLCM, iCLCM_LIFETIME_SECONDS);
        try {
            btpSocket.send(packet);
        } catch (IOException e) {
            logger.warn("failed to send iclcm", e);
        }        
    }



    public static class SocketAddressFromString {  // Public, otherwise JewelCLI can't access it!
        private final InetSocketAddress address;

        public SocketAddressFromString(final String addressStr) {
            String[] hostAndPort = addressStr.split(":");
            if (hostAndPort.length != 2) { throw new ArgumentValidationException(
                                                                                 "Expected host:port, got " + addressStr); }
            String hostname = hostAndPort[0];
            int port = Integer.parseInt(hostAndPort[1]);
            this.address = new InetSocketAddress(hostname, port);
        }

        public InetSocketAddress asInetSocketAddress() {
            return address;
        }
    }

    private static interface CliOptions{
        @Option int getPortRcvFromSimulink();
        @Option SocketAddressFromString getSimulinkAddress();

        @Option int getLocalPortForUdpLinkLayer();
        @Option SocketAddressFromString getRemoteAddressForUdpLinkLayer();

        @Option MacAddress getMacAddress();

        /*




        @Option(helpRequest = true) boolean getHelp();

        @Option boolean hasEthernetHeader();

        @Option MacAddress getMacAddress();

        boolean isMacAddress();
        */
    }

    /* PositionProvider is used by the beaconing service and for
     * creating the Geobroadcast address used for DENM messages.
     */
    public static class VehiclePositionProvider implements PositionProvider{
        public Address address;        
        public Position position;
        public boolean isPositionConfident;
        public double speedMetersPerSecond;
        public double headingDegreesFromNorth;

        //TODO: DEPRECATED. Replaced by proper address.
        //Optional<Address> emptyAddress = Optional.empty();

        VehiclePositionProvider(Address address){
            this.address = address;
            this.position = new Position(0, 0); //TODO: Replace with
                                                //sane starting
                                                //values.
            this.isPositionConfident = false;
            this.speedMetersPerSecond = 0;
            this.headingDegreesFromNorth = 0;
        }

        //TODO: Is the formatting of lat/long the same as in the CAM message?
        public void updatePosition(int latitude, int longitude){
            this.position = new Position((double) latitude, (double) longitude);
        }

        public Position getPosition(){
            return position;
        }

        public LongPositionVector getLatestPosition(){
            return new LongPositionVector(address,
                                          Instant.now(),
                                          position,
                                          isPositionConfident,
                                          speedMetersPerSecond,
                                          headingDegreesFromNorth);
        }
    }

    public VehicleAdapter(int portRcvFromSimulink, StationConfig config,
                          LinkLayer linkLayer, PositionProvider position,
                          MacAddress macAddress) throws SocketException {
        rcvSocket = new DatagramSocket(portRcvFromSimulink);
        station = new GeonetStation(config, linkLayer, position, macAddress);
        new Thread(station).start();
        station.startBecon();
        btpSocket = BtpSocket.on(station);
        executor.submit(receiveFromSimulinkLoop);
        executor.submit(sendToSimulinkLoop);
    }
    
    //TODO: Load options from config file instead of from arguments
    public static void main(String[] args) throws IOException {
        logger.info("Starting vehicle adapter...");

        //Parse CLI options
        CliOptions opts = CliFactory.parseArguments(CliOptions.class, args);
        //boolean hasEthernetHeader = opts.hasEthernetHeader();

        /*
        if(!hasEthernetHeader && ! opts.isMacAddress()){
            logger.error("Can't have MAC address with no ethernet header support!");
            System.exit(1);
        }
        */

        StationConfig config = new StationConfig();
        LinkLayer linkLayer =
            new LinkLayerUdpToEthernet(opts.getLocalPortForUdpLinkLayer(),
                                       opts.getRemoteAddressForUdpLinkLayer().asInetSocketAddress(),
                                       true);
        
        //MacAddress senderMac = opts.isMacAddress() ? opts.getMacAddress() : new MacAddress(0);
        MacAddress senderMac = opts.getMacAddress();
        
        //StationType is both a class in CoopITS and an ENUM in geonetworking
        Address address = new Address(true, //isManual
                                      net.gcdc.geonetworking.StationType.values()[5], //5 for passenger car
                                      46, //countryCode
                                      senderMac.value()); //lowLevelAddress
        
        vehiclePositionProvider = new VehiclePositionProvider(address);

        //simulink_cam_port = opts.getSimulinkAddress().asInetSocketAddress().getPort();
        //SIMULINK_ADDRESS = opts.getSimulinkAddress.asInetSocketAddress();


        VehicleAdapter va = new VehicleAdapter(opts.getPortRcvFromSimulink(), config, linkLayer,
                                               vehiclePositionProvider, senderMac);
        /*
        @option int getSimulinkReceivePort();
        @option SocketAddressFromString getUdpLinkLayerAddress();
        @option int getSimulinkSendPort();
        @option int getStationID();
        @option MacAddress getMacAddress();

        VehicleAdapter va = new VehicleAdapter(opts.getSimulinkReceivePort(), config, linklayer,
                                               vehiclePositionProvider,
                                               senderMac);
        */
    }

}
