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
    private final static short PORT_GCDCM = 2003;

    /* GCDC requires the non-standard max rate of 25Hz */
    private final static long CAM_INTERVAL_MIN_MS = 40;
    private final static long CAM_INTERVAL_MAX_MS = 1000;

    /* GCDC requires 1Hz for the low frequency container */
    private final static long CAM_LOW_FREQ_INTERVAL_MS = 1000;

    private final static long CAM_INITIAL_DELAY_MS = 20;  // At startup.

    private final static int HIGH_DYNAMICS_CAM_COUNT = 4;

    public final static double CAM_LIFETIME_SECONDS = 0.9;

    public final static int MAX_UDP_LENGTH = 65535;

    //TODO: Remove and use CLI arguments instead
    public final static int DEFAULT_SIMULINK_UDP_PORT = 5000;

    public final static int STATION_ID = 1337;

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
    private int lastLowFreqContainer = 0;
    public Cam simulinkToCam(byte[] receivedData){
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);


        try{
            byte messageId = buffer.get();
            if(messageId != MessageId.cam){
                logger.error("Incorrect local CAM received: " + receivedData);
                return null;
            }
            int genDeltaTimeMillis = buffer.getInt();
            Cam cam = createCam((lastLowFreqContainer - genDeltaTimeMillis) > CAM_LOW_FREQ_INTERVAL_MS,
                             genDeltaTimeMillis,
                             buffer.get(),    /* stationType */
                             buffer.get(),    /* vehicleRole */
                             buffer.getInt(), /* vehicleLength */
                             buffer.getInt(), /* vehicleWidth */
                             buffer.getInt(), /* latitude */
                             buffer.getInt(), /* longitude */
                             buffer.getInt(), /* semiMajorAxisConfidence */
                             buffer.getInt(), /* semiMinorAxisConfidence */
                             buffer.getInt(), /* semiMajorOrientation */
                             buffer.getInt(), /* headingValue */
                             buffer.getInt(), /* altitude */
                             buffer.getInt(), /* heading */
                             buffer.get(),    /* headingConfidence */
                             buffer.getInt(), /* speed */
                             buffer.get(),    /* speedConfidence */
                             buffer.getInt(), /* yawRate */
                             buffer.get(),    /* yawRateConfidence */
                             buffer.getInt(), /* longitudinalAcceleration */
                             buffer.get());   /* longitudinalAccelerationConfidence */

            lastLowFreqContainer = genDeltaTimeMillis;
            return cam;
            
        }catch(BufferOverflowException e){
            logger.error("Failed to create CAM from Simulink message: " + e);
            return null;
        }
    }

    /* Unpack a CAM message and create a Simulink message.
     */
    //TODO: Varying number of containers not supported by local
    //message set yet.
    //TODO: How do we unpack the data from CAM? Either the data
    //needs to be public or we need get methods.
    public void camToSimulink(Cam cam, byte[] packetBuffer) throws BufferOverflowException{
        ByteBuffer buffer = ByteBuffer.wrap(packetBuffer);
        /*
        CoopAwareness coopAwareness = cam.cam;
        CamParameters camParameters = coopAwareness.camParameters;
        BasicContainer basicContainer = cp.basicContainer;
        HighFrequencyContainer highFrequencyContainer = cp.highFrequencyContainer;
        LowFrequencyContainer lowFrequencyContainer = cp.lowFrequencyContainer;
        buffer.putInt(lowFrequencyContainer ? lowFrequencyContainer.basicVehicleContainerLowFrequency.vehicleRole : -1);
        buffer.put(basicContainer.stationType);
        */
    }

    public Cam createCam(boolean withLowFreq,
                         int genDeltaTimeMillis,
                         byte stationType,
                         byte vehicleRole,
                         int vehicleLength,
                         int vehicleWidth,
                         int latitude,
                         int longitude,
                         int semiMajorAxisConfidence,
                         int semiMinorAxisConfidence,
                         int semiMajorOrientation,
                         int headingValue,
                         int altitude,
                         int heading,
                         byte headingConfidence,
                         int speed,
                         byte speedConfidence,
                         int yawRate,
                         byte yawRateConfidence,
                         int longitudinalAcceleration,
                         byte longitudinalAccelerationConfidence){


        LowFrequencyContainer lowFrequencyContainer = withLowFreq ?
            new LowFrequencyContainer(
                                      new BasicVehicleContainerLowFrequency(
                                                                            VehicleRole.fromCode(vehicleRole),
                                                                            null,
                                                                            new PathHistory()
                                                                            ))
            :
            null; 

        //Not used for participating vehicles
        SpecialVehicleContainer specialVehicleContainer = null;

        BasicContainer basicContainer =
            new BasicContainer(
                               new StationType(stationType),
                               new ReferencePosition(
                                                     new Latitude(latitude),
                                                     new Longitude(longitude),
                                                     new PosConfidenceEllipse(
                                                                              new SemiAxisLength(semiMajorAxisConfidence),
                                                                              new SemiAxisLength(semiMinorAxisConfidence),
                                                                              new HeadingValue(semiMajorOrientation)),
                                                     new Altitude(
                                                                  new AltitudeValue(altitude),
                                                                  AltitudeConfidence.unavailable)));
        
        HighFrequencyContainer highFrequencyContainer =
            new HighFrequencyContainer(BasicVehicleContainerHighFrequency.builder()
                                       .heading(new Heading(
                                                            new HeadingValue(heading),
                                                            new HeadingConfidence(headingConfidence)))
                                       .speed(new Speed(
                                                        new SpeedValue(speed),
                                                        new SpeedConfidence(speedConfidence)))
                                       .vehicleLength(new VehicleLength(
                                                                        new VehicleLengthValue(vehicleLength),
                                                                        VehicleLengthConfidenceIndication.unavailable))
                                       .vehicleWidth(new VehicleWidth(vehicleWidth))
                                       .longitudinalAcceleration(new LongitudinalAcceleration(
                                                                                              new LongitudinalAccelerationValue(longitudinalAcceleration),
                                                                                              new AccelerationConfidence(longitudinalAccelerationConfidence)))
                                       .yawRate(new YawRate(
                                                            new YawRateValue(yawRate),
                                                            //TODO: This code is slow. Cache YawRateConfidence.values() if it's a problem.
                                                            YawRateConfidence.values()[yawRateConfidence]))
                                       .create()
                                       );
            return new Cam(
                    new ItsPduHeader(new MessageId(MessageId.cam)),
                    new CoopAwareness(
                            new GenerationDeltaTime(genDeltaTimeMillis * GenerationDeltaTime.oneMilliSec),
                            new CamParameters(
                                              basicContainer,
                                              highFrequencyContainer,
                                              lowFrequencyContainer,
                                              specialVehicleContainer)));
    }

    /* Not implemented yet. */
    public Denm simulinkToDenm(byte[] receivedData){
        ByteBuffer buffer = ByteBuffer.wrap(receivedData);

        try{
            byte messageId = buffer.get();
            if(messageId != MessageId.denm){
                logger.error("Incorrect local DENM received: " + receivedData);
                return null;
            }
            
            return createDenm(buffer.get(),     /* containerMask */
                              buffer.get(),     /* managementMask */
                              buffer.getLong(), /* detectionTime */
                              buffer.getLong(), /* referenceTime */
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
                              buffer.getInt()); /* stationType */                       
            
        }catch(BufferOverflowException e){
            logger.error("Failed to create DENM from Simulink message: " + e);
            return null;
        }
    }

    public byte[] denmToSimulink(){
        return null;
    }

    //TODO: Only management container is implemented.
    private int denm_sequence_number = 0;
    public Denm createDenm(byte containerMask,
                           byte managementMask,
                           long detectionTimeValue,
                           long referenceTimeValue,
                           int terminationValue,
                           int latitudeValue,
                           int longitudeValue,
                           int semiMajorConfidence,
                           int semiMinorConfidence,
                           int semiMajorOrientation,
                           int altitudeValue,
                           int relevanceDistanceValue,
                           int relevanceTrafficDirectionValue,
                           int validityDurationValue,
                           int transmissionIntervalValue,
                           int stationTypeValue){

        boolean withSituationContainer = ((containerMask & (1<<7)) != 0);
        boolean withLocationContainer = ((containerMask & (1<<6)) != 0);
        boolean withAlacarteContainer = ((containerMask & (1<<5)) != 0);

        //TODO: Implement these containers
        SituationContainer situationContainer = null;
        LocationContainer locationContainer = null;
        AlacarteContainer alacarteContainer = null;

        /* Management container */
        //TODO: Move these declarations inside the builder instead?
        TimestampIts detectionTime = new TimestampIts(detectionTimeValue);
        TimestampIts referenceTime = new TimestampIts(referenceTimeValue);
        Termination termination = Termination.values()[terminationValue];
        ReferencePosition eventPosition =
            new ReferencePosition(new Latitude(latitudeValue),
                                  new Longitude(longitudeValue),
                                  new PosConfidenceEllipse(
                                                           new SemiAxisLength(semiMajorConfidence),
                                                           new SemiAxisLength(semiMinorConfidence),
                                                           new HeadingValue(semiMajorOrientation)),
                                  new Altitude(new AltitudeValue(altitudeValue),
                                               AltitudeConfidence.unavailable));
        RelevanceDistance relevanceDistance = RelevanceDistance.values()[relevanceDistanceValue];
        RelevanceTrafficDirection relevanceTrafficDirection = RelevanceTrafficDirection.values()[relevanceTrafficDirectionValue];
        ValidityDuration validityDuration = new ValidityDuration(validityDurationValue);
        TransmissionInterval transmissionInterval = new TransmissionInterval(transmissionIntervalValue);
        StationType stationType = new StationType(stationTypeValue);

        ManagementContainer managementContainer =
            ManagementContainer.builder()
            .actionID(new ActionID(new StationID(STATION_ID), new SequenceNumber(denm_sequence_number++)))
            .detectionTime(detectionTime)
            .referenceTime(referenceTime)
            .termination((managementMask & (1<<7)) != 0 ? termination : null)
            .eventPosition(eventPosition)
            .relevanceDistance((managementMask & (1<<6)) != 0 ? relevanceDistance : null)
            .relevanceTrafficDirection((managementMask & (1<<5)) != 0 ? relevanceTrafficDirection : null)
            .validityDuration((managementMask & (1<<4)) != 0 ? validityDuration : null)
            .transmissionInterval((managementMask & (1<<3)) != 0 ? transmissionInterval : null)
            .stationType(stationType)
            .create();
                                                                               
        DecentralizedEnvironmentalNotificationMessage decentralizedEnvironmentalNotificationMessage =
            new DecentralizedEnvironmentalNotificationMessage(managementContainer,
                                                              situationContainer,
                                                              locationContainer,
                                                              alacarteContainer);

        Denm denm = new Denm(
                             new ItsPduHeader(new MessageId(MessageId.denm)),
                             decentralizedEnvironmentalNotificationMessage);

        logger.debug("Created DENM: " + denm);
        return denm;
    }

    //TODO: GCDCM messages needs to be added to the library
    /*
    public Gcdcm simulinkToGcdcm(byte[] packet){

    }

    public byte[] gcdcmToSimulink(){
        return null;
    }

    public Gcdcm createGcdcm(){

    }
    */

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
                        Cam cam = simulinkToCam(receivedData);
                        send(cam);
                        break;
                    }

                    case MessageId.denm: {
                        Denm denm = simulinkToDenm(receivedData);                        

                        /* TODO: How does GeoNetworking addressing work in
                         * GCDC16? For now let's just broadcast
                         * everything in a 200m radius.
                         */
                        send(denm, Geobroadcast.geobroadcast(Area.circle(vehiclePositionProvider.getPosition(), (double) 200)));
                        break;
                    }

                        /*
                          case MessageId.gcdcm:
                          //TODO: Gcdcm is not included in the library yet.
                          break;
                        */
                        
                    default:
                        //fallthrough
                        logger.debug("Reached default in receiveFromSimulink switch");
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to receive packet from Simulink, terminating", e);
                System.exit(1);
            }
        }
    };

    private Runnable sendToSimulinkLoop = new Runnable() {
        byte[] buffer = new byte[MAX_UDP_LENGTH];
        private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        @Override public void run() {
real             try {
                BtpPacket btpPacket = btpSocket.receive();
                switch (btpPacket.destinationPort()) {
                    case PORT_CAM: {
                        Cam cam;
                        try {
                            cam = UperEncoder.decode(btpPacket.payload(), Cam.class);
                            camToSimulink(cam, buffer);
                            

                            packet.setPort(DEFAULT_SIMULINK_UDP_PORT);
                            try {
                                rcvSocket.send(packet);
                            } catch (IOException e) {
                                logger.warn("Failed to send packet to Simulink", e);
                            }
                        } catch (IllegalArgumentException | UnsupportedOperationException | BufferOverflowException e) {
                            logger.warn("Can't decode cam", e);
                        }
                        break;
                    } case PORT_DENM: {
                        Denm denm;
                        try {
                            denm = UperEncoder.decode(btpPacket.payload(), Denm.class);
                            
                            // TODO: Fill in the buffer for packet.

                            packet.setPort(DEFAULT_SIMULINK_UDP_PORT);
                            try {
                                rcvSocket.send(packet);
                            } catch (IOException e) {
                                logger.warn("Failed to send packet to Simulink", e);
                            }
                        } catch (IllegalArgumentException | UnsupportedOperationException e) {
                            logger.warn("Can't decode denm", e);
                        }
                        break;
                    } default:
                        // Ignore.
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

    /*
    private void send(Gcdcm gcdcm){

    }
    */

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

        @Option SocketAddressFromString getRemoteAddressForUdpLinkLayer();

        @Option int getLocalPortForUdpLinkLayer();

        @Option(helpRequest = true) boolean getHelp();

        @Option boolean hasEthernetHeader();

        @Option MacAddress getMacAddress();

        boolean isMacAddress();
    }

    //DEPRECATED
    public static class DummyPositionProvider implements PositionProvider{
        private final MacAddress senderMac;

        DummyPositionProvider(MacAddress senderMac){
            this.senderMac = senderMac;
        }
        
        public LongPositionVector getLatestPosition(){
            Optional<Address> emptyAddress = Optional.empty();
            /*
            return new LongPositionVector(new Address(true, StationType.passengerCar,
                                                      752, senderMac.value()),
                                          Instant.now(), new Position(0, 0),
                                          true, 0, 0);                                          
            */
            return new LongPositionVector(emptyAddress,
                                          Instant.now(),
                                          new Position(0, 0),
                                          true,
                                          0,
                                          0);
        }
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

        //TODO: Remove once we have a proper address
        Optional<Address> emptyAddress = Optional.empty();

        VehiclePositionProvider(Address address){
            this.address = address;
            this.position = new Position(0, 0);
            this.isPositionConfident = false;
            this.speedMetersPerSecond = 0;
            this.headingDegreesFromNorth = 0;
        }

        //TODO: Update position based on data in CAM messages
        public void updatePosition(int lattitude, int longitude){
            this.position = new Position((double) lattitude, (double) longitude);
        }

        public Position getPosition(){
            return this.position;
        }

        public LongPositionVector getLatestPosition(){
            return new LongPositionVector(emptyAddress,
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
    
    //TODO: Clean up main class and look through the CLI options.
    public static void main(String[] args) throws IOException {
        logger.info("Starting vehicle adapter...");

        //Parse CLI options
        CliOptions opts = CliFactory.parseArguments(CliOptions.class, args);
        boolean hasEthernetHeader = opts.hasEthernetHeader();

        if(!hasEthernetHeader && ! opts.isMacAddress()){
            logger.error("Can't have MAC address with no ethernet header support!");
            System.exit(1);
        }

        StationConfig config = new StationConfig();
        LinkLayer linkLayer =
            new LinkLayerUdpToEthernet(opts.getLocalPortForUdpLinkLayer(),
                                       opts.getRemoteAddressForUdpLinkLayer().asInetSocketAddress(),
                                       opts.hasEthernetHeader());
        
        MacAddress senderMac = opts.isMacAddress() ? opts.getMacAddress() : new MacAddress(0);

        //TODO: Add a proper address
        vehiclePositionProvider = new VehiclePositionProvider(null);
        
        VehicleAdapter va = new VehicleAdapter(opts.getPortRcvFromSimulink(), config, linkLayer,
                                               vehiclePositionProvider, senderMac);
    }

}
