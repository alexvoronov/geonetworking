package net.gcdc.vehicle;

import java.io.IOException;
import java.nio.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.IllegalArgumentException;

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

    public final static int MAX_UDP_LENGTH = 300;
    public final static int LOCAL_CAM_LENGTH = 81;
    public final static int LOCAL_DENM_LENGTH = 0;
    public final static int LOCAL_ICLCM_LENGTH = 0;;


    /* Default port values */
    public static int simulink_cam_port = 5000;
    public static int simulink_denm_port = simulink_cam_port + 1;
    public static int simulink_iclcm_port = simulink_denm_port + 1;

    public static InetAddress simulink_address;

    public static final ExecutorService executor = Executors.newCachedThreadPool();

    public static VehiclePositionProvider vehiclePositionProvider;

    /* Receive local CAM/DENM/iCLCM from Simulink, parse them and
     * create the proper messages, and send them to the link layer. */
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
                    logger.debug("Received packet from vehicle control! ID: " + receivedData[0] + " Data: " + receivedData);
                    /* Comment in to display raw message data.
                    System.out.printf("RAW MESSAGE DATA: ");
                    for(int i = 0;i < receivedData.length;i++) System.out.printf("%02X ", receivedData[i]);
                    System.out.println("");
                    */

                    /* First byte is the MessageId */
                    switch(receivedData[0]){                        
                    case MessageId.cam: {
                        logger.debug("Received CAM from vehicle control.");
                        try{
                            LocalCam localCam = new LocalCam(receivedData);
                            Cam cam = localCam.asCam();
                            send(cam);
                        }catch(IllegalArgumentException e){
                            logger.error("Irrecoverable error when creating CAM. Ignoring message.", e);
                        }
                        break;
                    }

                    case MessageId.denm: {
                        logger.debug("Received DENM from vehicle control.");
                        try{
                            LocalDenm localDenm = new LocalDenm(receivedData);
                            Denm denm = localDenm.asDenm();                        

                            /* TODO: How does GeoNetworking addressing work in
                             * GCDC16? For now let's just broadcast
                             * everything in a large radius.
                             */
                            send(denm, Geobroadcast.geobroadcast(Area.circle(vehiclePositionProvider.getPosition(), Double.MAX_VALUE)));
                        }catch(IllegalArgumentException e){
                            logger.error("Irrecoverable error when creating DENM. Ignoring message.", e);
                        }
                        break;
                    }
                        
                    case net.gcdc.camdenm.Iclcm.MessageID_iCLCM: {
                        logger.debug("Received iCLCM from vehicle control.");
                        try{
                            LocalIclcm localIclcm = new LocalIclcm(receivedData);
                            IgameCooperativeLaneChangeMessage iclcm = localIclcm.asIclcm();
                            send(iclcm);
                        }catch(IllegalArgumentException e){
                            logger.error("Irrecoverable error when creating iCLCM. Ignoring message.", e);
                        }
                        break;
                    }
                        
                    default:
                        logger.warn("Received incorrectly formated message! ID: {} Data: {}", 
                                receivedData[0], receivedData);
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to receive packet from Simulink, terminating", e);
                System.exit(1);
            }
        }
        };

    /* Receive incoming CAM/DENM/iCLCM to Simulink, convert them to
     * their local representation, and send them to Simulink over UDP. */
    private Runnable sendToSimulinkLoop = new Runnable() {
            /* TODO: Don't allocate new memory for every iteration. */
            byte[] buffer = new byte[MAX_UDP_LENGTH];
            private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            @Override public void run() {
                packet.setAddress(simulink_address);

                try {
                    while(true){
                        BtpPacket btpPacket = btpSocket.receive();
                        switch (btpPacket.destinationPort()) {
                        case PORT_CAM: {
                            logger.info("Forwarding CAM to vehicle control.");
                            Cam cam;
                            try {
                                cam = UperEncoder.decode(btpPacket.payload(), Cam.class);
                                /*
                                for(int i = 0;i < btpPacket.payload().length;i++) System.out.printf("%02X ", btpPacket.payload()[i]);
                                System.out.println("");
                                */

                                LocalCam localCam = new LocalCam(cam);
                                buffer = localCam.asByteArray();
                                packet.setData(buffer, 0, buffer.length);                                

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
                            logger.info("Forwarding DENM to vehicle control.");
                            Denm denm;
                            try {
                                denm = UperEncoder.decode(btpPacket.payload(), Denm.class);
                                /*
                                for(int i = 0;i < btpPacket.payload().length;i++) System.out.printf("%02X ", btpPacket.payload()[i]);
                                System.out.println("");
                                */
                                
                                LocalDenm localDenm = new LocalDenm(denm);
                                buffer = localDenm.asByteArray();
                                packet.setData(buffer, 0, buffer.length);

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
                            logger.info("Forwarding iCLCM to vehicle control.");
                            IgameCooperativeLaneChangeMessage iclcm;
                            try {
                                iclcm = UperEncoder.decode(btpPacket.payload(), IgameCooperativeLaneChangeMessage.class);
                                LocalIclcm localIclcm = new LocalIclcm(iclcm);
                                buffer = localIclcm.asByteArray();
                                packet.setData(buffer, 0, buffer.length);

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
            logger.error("Failed to encode DENM {}, ignoring", denm, e);
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
            logger.error("Failed to encode iCLCM {}, ignoring", iclcm, e);
            return;
        }
        BtpPacket packet = BtpPacket.singleHop(bytes, PORT_ICLCM, iCLCM_LIFETIME_SECONDS);
        try {
            btpSocket.send(packet);
        } catch (IOException e) {
            logger.warn("Failed to send iclcm", e);
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
        /* Port to receive messages from Simulink on */       
        @Option int getPortRcvFromSimulink();

        /* Ports to send CAM, DENM, iCLCM messages on. These ports can
         * be the same or different. */
        @Option int getPortSendCam();
        @Option int getPortSendDenm();
        @Option int getPortSendIclcm();

        /* IP of Simulink */
        //@Option SocketAddressFromString getSimulinkAddress();
        @Option String getSimulinkAddress();

        /* The local port and remote address for the link layer. The
         * link layer can either run on the same machine or a separate
         * one. */
        @Option int getLocalPortForUdpLinkLayer();
        @Option SocketAddressFromString getRemoteAddressForUdpLinkLayer();

        /* Mac address to use when broadcasting. */
        @Option MacAddress getMacAddress();

        /* Country code */
        @Option int getCountryCode();
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

        VehiclePositionProvider(Address address){
            this.address = address;
            this.position = new Position(0, 0);
            this.isPositionConfident = false;
            this.speedMetersPerSecond = 0;
            this.headingDegreesFromNorth = 0;
        }

        //TODO: Is the formatting of lat/long the same as in the CAM message?
        public void updatePosition(int latitude, int longitude){
            this.position = new Position((double) latitude, (double) longitude);
            logger.debug("VehiclePositionProvider position updated: {}", this.position);
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
    
    public static void main(String[] args) throws IOException {
        logger.info("Starting vehicle adapter...");

        //Parse CLI options
        CliOptions opts = CliFactory.parseArguments(CliOptions.class, args);

        StationConfig config = new StationConfig();
        LinkLayer linkLayer =
            new LinkLayerUdpToEthernet(opts.getLocalPortForUdpLinkLayer(),
                                       opts.getRemoteAddressForUdpLinkLayer().asInetSocketAddress(),
                                       true);
        simulink_address = InetAddress.getByName(opts.getSimulinkAddress());
        
        MacAddress senderMac = opts.getMacAddress();
        
        Address address = new Address(true, //isManual
                                      net.gcdc.geonetworking.StationType.values()[5], //5 for passenger car
                                      opts.getCountryCode(), //countryCode
                                      senderMac.value()); //lowLevelAddress
        
        vehiclePositionProvider = new VehiclePositionProvider(address);

        simulink_cam_port = opts.getPortSendCam();
        simulink_denm_port = opts.getPortSendDenm();
        simulink_iclcm_port = opts.getPortSendIclcm();

        /* Create the vehicle adapter. */
        VehicleAdapter va = new VehicleAdapter(opts.getPortRcvFromSimulink(), config, linkLayer, vehiclePositionProvider, senderMac);
    }
}
