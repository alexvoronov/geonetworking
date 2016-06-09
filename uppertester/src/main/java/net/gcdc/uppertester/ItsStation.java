package net.gcdc.uppertester;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.AccelerationControl;
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
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.LowFrequencyContainer;
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
import net.gcdc.camdenm.CoopIts.SpecialTransportContainer;
import net.gcdc.camdenm.CoopIts.SpecialTransportType;
import net.gcdc.camdenm.CoopIts.SpecialVehicleContainer;
import net.gcdc.camdenm.CoopIts.Speed;
import net.gcdc.camdenm.CoopIts.SpeedConfidence;
import net.gcdc.camdenm.CoopIts.SpeedValue;
import net.gcdc.camdenm.CoopIts.StationType;
import net.gcdc.camdenm.CoopIts.VehicleRole;
import net.gcdc.camdenm.CoopIts.YawRate;
import net.gcdc.camdenm.CoopIts.YawRateConfidence;
import net.gcdc.camdenm.CoopIts.YawRateValue;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.Area;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination;
import net.gcdc.geonetworking.Destination.Geobroadcast;
import net.gcdc.geonetworking.GeonetData;
import net.gcdc.geonetworking.GeonetDataListener;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.TrafficClass;
import net.gcdc.geonetworking.UpperProtocolType;
import net.gcdc.geonetworking.gpsdclient.GpsdClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.InvalidOptionSpecificationException;
import com.lexicalscope.jewel.cli.Option;

/**
 * Upper Tester Application that wraps Interface Under Test, here the 'net.gcdc.geonetworking' library.
 *
 * Upper Tester Application gets commands via UDP from Test System.
 * Test System is driven by TTCN-3 tools.
 *
 * The main method expects that LinkLayer part (e.g. utoepy) of System Under Test is started
 * separately.
 *
 */
public class ItsStation implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(ItsStation.class);

    private int defaultUpperTesterPort = 5000;

    private final DatagramSocket rcvSocket;
    private final GeonetStation station;
    private final BtpSocket btpSocket;

    private final Vehicle vehicle;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(5);

    private final static double MICRODEGREE = 1E-6;
    private final static byte REPLY_SUCCESS = 0x01;
    private final static byte REPLY_FAILURE = 0x00;

    private final static short PORT_CAM  = 2001;
    private final static short PORT_DENM = 2002;
//    private final static short PORT_MAP  = 2003;
//    private final static short PORT_SPAT = 2004;

    private final static long CAM_INTERVAL_MIN_MS = 100;
    private final static long CAM_INTERVAL_MAX_MS = 1000;
    private final static long CAM_LOW_FREQ_INTERVAL_MS = 500;
    private final static long CAM_INITIAL_DELAY_MS = 20;  // At startup.
    private final static int HIGH_DYNAMICS_CAM_COUNT = 4;
    public final static double CAM_LIFETIME_SECONDS = 0.9;
   
    private final UpdatablePositionProvider position;
    
    public ItsStation(StationConfig config, LinkLayer linkLayer, UpdatablePositionProvider position, int udpPort, MacAddress macAddress, boolean withCAM) {
        this.position = position;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(udpPort);
        } catch (SocketException e) {
            logger.error("Unable to open socket for UT communication", e);
            System.exit(1);
        } finally {
            rcvSocket = socket;
        }
        station = new GeonetStation(config, linkLayer, position, macAddress);
        new Thread(station).start();
        station.startBecon();
        btpSocket = BtpSocket.on(station);
        station.addGeonetDataListener(gnListener);
        executor.submit(btpListener);
        vehicle = new Vehicle(position);
        if (withCAM) {
            scheduledExecutor.scheduleAtFixedRate(camSender, CAM_INITIAL_DELAY_MS, CAM_INTERVAL_MIN_MS, TimeUnit.MILLISECONDS);
        }
    }

    private InetAddress defaultTsAddress;

    private final GeonetDataListener gnListener = new GeonetDataListener() {
        @Override public void onGeonetDataReceived(GeonetData indication) {
            try {
                sendReply(new GnEventIndication(indication.payload), defaultTsAddress, defaultUpperTesterPort);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to send unsolicited GN indication to the TestSystem", e);
            }
        }
    };

    private final Runnable btpListener = new Runnable() {
        @Override public void run() {
            while(true) {
                try {
                    BtpPacket btpPacket = btpSocket.receive();
                    logger.debug("Sending BTP event indication");
                    sendReply(new BtpEventIndication(btpPacket.payload()), defaultTsAddress, defaultUpperTesterPort);
                    switch (btpPacket.destinationPort()) {
                        case PORT_CAM: onCamReceived(btpPacket.payload()); break;
                        case PORT_DENM: onDenmReceived(btpPacket.payload()); break;
                        default: logger.info("Message for unsupported BTP port {}", btpPacket.destinationPort());
                    }

                } catch (InterruptedException | IllegalArgumentException e) {
                    logger.warn("Failed to receive+send unsolicited BTP indication to the TestSystem",
                        e);
                }
            }
        }
    };

    private final Runnable camSender = new Runnable() {
        Instant lastSend = null;
        Instant lastSendLowFreq = null;
        LongPositionVector lastSendPos;

        private int highDynamicsCamToSend = 0;

        @Override public void run() {
            if (lastSend == null) {
                LongPositionVector lpv = position.getLatestPosition();
                Cam cam = vehicle.getCam(true, 0, lpv);
                logger.info("Sending first CAM");
                send(cam);
                lastSend = Instant.now();
                lastSendLowFreq = Instant.now();
                lastSendPos = lpv;
            } else {
                long deltaTime = Duration.between(lastSend, Instant.now()).toMillis();
                long lowFreqDeltaTime = Duration.between(lastSendLowFreq, Instant.now()).toMillis();
                LongPositionVector lpv = position.getLatestPosition();
                if (farEnough(lastSendPos, lpv)) {
                    highDynamicsCamToSend = HIGH_DYNAMICS_CAM_COUNT;
                }
                if (deltaTime >= (CAM_INTERVAL_MAX_MS - 2 * CAM_INTERVAL_MIN_MS) ||
                        highDynamicsCamToSend > 0) {
                    final boolean withLowFreq = lowFreqDeltaTime >= CAM_LOW_FREQ_INTERVAL_MS;
                    Cam cam = vehicle.getCam(withLowFreq, (int) deltaTime, lpv);
                    logger.info("Sending CAM" + (withLowFreq ? " with Low Freq container" : ""));
                    send(cam);
                    lastSend = Instant.now();
                    lastSendPos = lpv;
                    if (withLowFreq) { lastSendLowFreq = lastSend; }
                    if (highDynamicsCamToSend > 0) { highDynamicsCamToSend--; }
                } else {
                    //logger.debug("Skip CAM");
                }
            }
        }

        private boolean farEnough(LongPositionVector oldPos, LongPositionVector newPos) {
            return Math.abs(oldPos.headingDegreesFromNorth() - newPos.headingDegreesFromNorth()) > 4 ||
                    oldPos.position().distanceInMetersTo(newPos.position()) > 4 ||
                    Math.abs(oldPos.speedMetersPerSecond() - newPos.speedMetersPerSecond()) > 0.5;
        }

        private void send(Cam cam) {
            try {
                BtpPacket packet = BtpPacket.singleHop(UperEncoder.encode(cam), PORT_CAM, CAM_LIFETIME_SECONDS);
                btpSocket.send(packet);
            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                logger.error("Failed to encode CAM", e);
            } catch (IOException e) {
                logger.error("Failed to send CAM", e);
            }
        }
    };

    private void send(Denm denm, Geobroadcast destination) {
        logger.info("attempt to send DENM");
        try {
            byte[] bytes = UperEncoder.encode(denm);
            logger.info("bytes encoded ", bytes);
            BtpPacket packet = BtpPacket.customDestination(bytes, PORT_DENM, destination);
            logger.info("sending DENM as BTP packet {} ", packet);
            btpSocket.send(packet);
            logger.debug("btp packet sent with no exception ", packet);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            logger.error("Failed to encode DENM", e);
        } catch (IOException e) {
            logger.error("Failed to send DENM", e);
        }

    }


    public static interface UpdatablePositionProvider extends PositionProvider {
        public void move(double deltaLatDegrees, double deltaLonDegrees, double deltaElevation);
        public void setSpeed(double speedMetersPerSecond);
        public void setHeading(double headingDegreesFromNorth);
    }

    private static class StaticUpdatablePositionProvider implements UpdatablePositionProvider {

        private Position currentPosition;

        private double speedMetersPerSecond = 0;
        private double headingDegreesFromNorth = 0;

        public StaticUpdatablePositionProvider(Position initialPosition) {
            currentPosition = initialPosition;
        }

        @Override
        public void move(
                final double deltaLatDegrees,
                final double deltaLonDegrees,
                final double deltaElevation) {
            currentPosition = new Position(
                    currentPosition.lattitudeDegrees() + deltaLatDegrees,
                    currentPosition.longitudeDegrees() + deltaLonDegrees);
        }

        @Override
        public void setSpeed(double speedMetersPerSecond) {
            this.speedMetersPerSecond = speedMetersPerSecond;
        }

        @Override
        public void setHeading(double headingDegreesFromNorth) {
            this.headingDegreesFromNorth = headingDegreesFromNorth;
        }

        @Override public LongPositionVector getLatestPosition() {
            Optional<Address> emptyAddress = Optional.empty();
            return new LongPositionVector(emptyAddress,
                    Instant.now(),
                    currentPosition,
                    true,
                    speedMetersPerSecond,
                    headingDegreesFromNorth);
        }
    }

    private static class UpdatableGpsdPositionProvider extends GpsdClient implements UpdatablePositionProvider {

        private Position currentPosition = null;

        private double speedMetersPerSecond = 0;
        private double headingDegreesFromNorth = 0;

        public UpdatableGpsdPositionProvider(InetSocketAddress address) throws IOException {
            super(address);
            logger.debug("gpsd client initialized");
        }

        @Override
        public void move(
                final double deltaLatDegrees,
                final double deltaLonDegrees,
                final double deltaElevation) {
            currentPosition = new Position(
                    getLatestPosition().position().lattitudeDegrees() + deltaLatDegrees,
                    getLatestPosition().position().longitudeDegrees() + deltaLonDegrees);
        }

        @Override
        public void setSpeed(double speedMetersPerSecond) {
            this.speedMetersPerSecond = speedMetersPerSecond;
        }

        @Override
        public void setHeading(double headingDegreesFromNorth) {
            this.headingDegreesFromNorth = headingDegreesFromNorth;
        }

        /** Returns GPSd position until the first move is called. after which it never asks gpsd. */
        @Override public LongPositionVector getLatestPosition() {
            logger.trace("position provider request to gpsd client with current position {}", currentPosition);
            Optional<Address> emptyAddress = Optional.empty();
            return (currentPosition == null) ? super.getLatestPosition() :
                new LongPositionVector(emptyAddress,
                    Instant.now(),
                    currentPosition,
                    true,
                    speedMetersPerSecond,
                    headingDegreesFromNorth);
        }
    }



    

    private void sendReply(Response message, InetAddress tsAddress, int tsPort) {
        logger.info((tsAddress == null ? "Skip " : "") + "Sending message to TS: " + message);
        if (tsAddress != null) {
            byte[] messageAsBytes;
            try {
                messageAsBytes = Parser.toBytes(message);
                DatagramPacket replyPacket = new DatagramPacket(messageAsBytes, messageAsBytes.length,
                        tsAddress, tsPort);
                rcvSocket.send(replyPacket);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.warn("Failed to encode {}", message, e);
            } catch (IOException e) {
                logger.warn("Failed to send reply packet to UT");
            }
        }
    }

    private void onCamReceived(byte[] cam) {
        logger.info("Got CAM");
        try {
            sendReply(new CamEventIndication(cam), defaultTsAddress, defaultUpperTesterPort);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to send CAM event indication", e);
        }
    }

    private void onDenmReceived(byte[] denm) {
        logger.info("Got DENM");
        try {
            sendReply(new DenmEventIndication(denm), defaultTsAddress, defaultUpperTesterPort);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to send DENM event indication", e);
        }
    }

    public void run() {

        byte[] buffer = new byte[65535];
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        while (true) {

            logger.debug("Waiting for packet");
            try {
                rcvSocket.receive(receivePacket);
            } catch (IOException e1) {
                logger.error("Failed to receive packet from Upper Tester", e1);
                System.exit(1);
            }

            Object message;
            try {
                message = new Parser()
                        .parse2(Arrays.copyOfRange(buffer, 0, receivePacket.getLength()));
                InetAddress tsAddress = receivePacket.getAddress();
                int tsPort = receivePacket.getPort();
                Response response = processMessage(message, tsAddress, tsPort);
                if (response != null) {
                    sendReply(response, tsAddress, tsPort);
                }
            } catch (InstantiationException | IllegalAccessException e1) {
                logger.warn("Failed to parse {}", buffer);
            } catch (IOException e) {
                logger.error("Failed to process Upper Tester trigger", e);
            }
        }
    }

    public Response processMessage(Object message, InetAddress tsAddress, int tsPort) throws IOException {
            // All messages in one big SWITCH-like statement:
            // Part 1: Common Upper Tester Primitives.
            if (message instanceof Initialize) {
                logger.info("Set default UDP port to {}", defaultUpperTesterPort);
                defaultUpperTesterPort = tsPort;
                defaultTsAddress = tsAddress;
                return new InitializeResult((byte) 0x01);
            } else if (message instanceof ChangePosition) {
                ChangePosition typedMessage = (ChangePosition) message;
                position.move(
                        typedMessage.deltaLatitude * 0.1 * MICRODEGREE,
                        typedMessage.deltaLongitude * 0.1 * MICRODEGREE,
                        typedMessage.deltaElevation);
                return (new ChangePositionResult(REPLY_SUCCESS));
            } else if (message instanceof ChangePseudonym) {
                return(new ChangePseudonymResult(REPLY_FAILURE));  // We don't have pseudonyms.
            } // ...to be continued after the comments:
              // Part 2: CAM Upper Tester Primitives (not implemented yet)
            else if (message instanceof CamTriggerChangeCurvature) {
                CamTriggerChangeCurvature typedMessage = (CamTriggerChangeCurvature) message;
                vehicle.curvature += typedMessage.curvature;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerChangeSpeed) {
                CamTriggerChangeSpeed typedMessage = (CamTriggerChangeSpeed) message;
                logger.debug("change speed received by {} cm/s", typedMessage.speedVariation);
                position.setSpeed(position.getLatestPosition().speedMetersPerSecond() + 0.01 * typedMessage.speedVariation);
                logger.debug("speed now {} m/s", position.getLatestPosition().speedMetersPerSecond());
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetAccelerationControlStatus) {
                CamTriggerSetAccelerationControlStatus typedMessage = (CamTriggerSetAccelerationControlStatus) message;
                vehicle.accelerationControlStatus = typedMessage.flags;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetExteriorLightsStatus) {
                CamTriggerSetExteriorLightsStatus typedMessage = (CamTriggerSetExteriorLightsStatus) message;
                vehicle.exteriorLightsStatus = typedMessage.flags;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerChangeHeading) {
                CamTriggerChangeHeading typedMessage = (CamTriggerChangeHeading) message;
                position.setHeading(position.getLatestPosition().headingDegreesFromNorth() + 0.1 * typedMessage.direction);
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetDriveDirection) {
                CamTriggerSetDriveDirection typedMessage = (CamTriggerSetDriveDirection) message;
                vehicle.driveDirection = typedMessage.direction;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerChangeYawRate) {
                CamTriggerChangeYawRate typedMessage = (CamTriggerChangeYawRate) message;
                vehicle.yawRate += typedMessage.yawRate;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetStationType) {
                CamTriggerSetStationType typedMessage = (CamTriggerSetStationType) message;
                vehicle.stationType = typedMessage.stationType;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetVehicleRole) {
                CamTriggerSetVehicleRole typedMessage = (CamTriggerSetVehicleRole) message;
                vehicle.vehicleRole = typedMessage.vehicleRole;
                logger.debug("Set vehicle role to {}", vehicle.vehicleRole);
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetEmbarkationStatus) {
                CamTriggerSetEmbarkationStatus typedMessage = (CamTriggerSetEmbarkationStatus) message;
                vehicle.embarkationStatus = typedMessage.embarkationStatus != 0;  // 0=false, 255=true
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetPtActivation) {
                CamTriggerSetPtActivation typedMessage = (CamTriggerSetPtActivation) message;
                vehicle.ptActivationType = typedMessage.ptActiavtionType;
                vehicle.ptActivationData = typedMessage.ptActivationData.clone();
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetDangerousGoods) {
                CamTriggerSetDangerousGoods typedMessage = (CamTriggerSetDangerousGoods) message;
                vehicle.dangerousGoods = typedMessage.dangerousGood;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetDangerousGoodsExt) {
                CamTriggerSetDangerousGoodsExt typedMessage = (CamTriggerSetDangerousGoodsExt) message;
                vehicle.dangerousGoodExt = typedMessage.dangerousGoodExt;
                return(new CamTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof CamTriggerSetLightBarSiren) {
                CamTriggerSetLightBarSiren typedMessage = (CamTriggerSetLightBarSiren) message;
                vehicle.lightBarSiren = typedMessage.flags;
                return(new CamTriggerResult(REPLY_SUCCESS));
            }  // ...continued after comment
              // Part 3: DENM Upper Tester Primitives (not implemented yet)
              // Part 4: GeoNetworking Upper Tester Primitives
            else if (message instanceof GnTriggerGeoAnycast) {
                GnTriggerGeoAnycast typedMessage = (GnTriggerGeoAnycast) message;
                return(new GnTriggerResult(sendAreaCast(
                        typedMessage.shape,
                        typedMessage.lifetime,
                        typedMessage.trafficClass,
                        typedMessage.latitude,
                        typedMessage.longitude,
                        typedMessage.distanceA,
                        typedMessage.distanceB,
                        typedMessage.angle,
                        typedMessage.payload,
                        true)));
            } else if (message instanceof GnTriggerGeoBroadcast) {
                GnTriggerGeoBroadcast typedMessage = (GnTriggerGeoBroadcast) message;
                return (new GnTriggerResult(sendAreaCast(
                        typedMessage.shape,
                        typedMessage.lifetime,
                        typedMessage.trafficClass,
                        typedMessage.latitude,
                        typedMessage.longitude,
                        typedMessage.distanceA,
                        typedMessage.distanceB,
                        typedMessage.angle,
                        typedMessage.payload,
                        false)));
            } else if (message instanceof GnTriggerSHB) {
                GnTriggerSHB typedMessage = (GnTriggerSHB) message;
                Optional<LongPositionVector> sender = Optional.empty();
                GeonetData data = new GeonetData(
                        UpperProtocolType.BTP_B,
                        Destination.singleHop(),
                        Optional.of(TrafficClass.fromByte(typedMessage.trafficClass)),
                        sender,
                        typedMessage.payload);
                station.send(data);
                return(new GnTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof GnTriggerTSB) {
                return(new GnTriggerResult(REPLY_FAILURE));
            } else if (message instanceof GnTriggerGeoUnicast) {
                return(new GnTriggerResult(REPLY_FAILURE));
            } // ...to be continued after the comments:
              // Part 5: IPv6OverGeoNetworking Upper Tester Primitives (not supported)
              // Part 6: BTP Upper Tester Primitives
            else if (message instanceof BtpTriggerA) {
                BtpTriggerA typedMessage = (BtpTriggerA) message;
                btpSocket.send(BtpPacket.singleHopEmptyA(typedMessage.destinationPort,
                        typedMessage.sourcePort));
                return(new BtpTriggerResult(REPLY_SUCCESS));
            } else if (message instanceof BtpTriggerB) {
                BtpTriggerB typedMessage = (BtpTriggerB) message;
                btpSocket.send(BtpPacket.singleHopEmptyB(typedMessage.destinationPort,
                        typedMessage.destinationPortInfo));
                return(new BtpTriggerResult(REPLY_SUCCESS));
            } else {
                logger.warn("Unhandled message of type " + message.getClass());
                return null;
            }
        }


    private byte sendAreaCast(
            byte shape,
            short lifetime,
            byte trafficClass,
            int latE7,
            int lonE7,
            short distanceA,
            short distanceB,
            short angle,
            byte[] payload,
            boolean isAnycast) {

        Position center = new Position(latE7 * 0.1 * MICRODEGREE, lonE7 * 0.1 * MICRODEGREE);

        Area area =
                shape == 0 ? Area.circle(center, distanceA) :  // in Circle distanceA is radius and distanceB is zero.
                        shape == 1 ? Area.rectangle(center, distanceA, distanceB, angle) :
                                shape == 2 ? Area.ellipse(center, distanceA, distanceB, angle) :
                                        null;
        if (area == null) {
            logger.error("Unrecognized shape id: " + shape);
            return REPLY_FAILURE;
        }

        Destination destination = isAnycast ?
                Destination.geoanycast(area).withMaxLifetimeSeconds(0.001 * lifetime) :
                Destination.geobroadcast(area).withMaxLifetimeSeconds(0.001 * lifetime);

        Optional<LongPositionVector> sender = Optional.empty();

        GeonetData data = new GeonetData(
                UpperProtocolType.BTP_B,
                destination,
                Optional.of(TrafficClass.fromByte(trafficClass)),
                sender,
                payload);

        try {
            station.send(data);
        } catch (IOException e) {
            logger.error("Failed to send Areacast", e);
        }

        return REPLY_SUCCESS;
    }

    @Override public void close() {
        rcvSocket.close();
        station.removeGeonetDataListener(gnListener);
        station.close();
        btpSocket.close();
        executor.shutdownNow();
        scheduledExecutor.shutdownNow();
    }


    public static class Vehicle {
        int curvature;
        byte accelerationControlStatus;
        byte exteriorLightsStatus;
        byte driveDirection;
        int yawRate;
        byte stationType;
        byte vehicleRole = 2;
        boolean embarkationStatus;
        byte dangerousGoods;
        byte dangerousGoodExt;
        byte lightBarSiren;
        byte ptActivationType;
        byte[] ptActivationData;

        public Vehicle(PositionProvider positionProvider) {
        }

        public Denm getDenm() {
            return new Denm();
        }

        public Cam getCam(boolean withLowFreq, int genDeltaTimeMillis, LongPositionVector lpv) {
            LowFrequencyContainer lowFrequencyContainer = withLowFreq ?
                    new LowFrequencyContainer(
                            new BasicVehicleContainerLowFrequency(
                                    VehicleRole.fromCode(vehicleRole),
                                    ExteriorLights.builder()
                                        .lowBeamHeadlightsOn   ((exteriorLightsStatus & (1<<7)) != 0)  // check, maybe in the other order!
                                        .highBeamHeadlightsOn  ((exteriorLightsStatus & (1<<6)) != 0)
                                        .leftTurnSignalOn      ((exteriorLightsStatus & (1<<5)) != 0)
                                        .rightTurnSignalOn     ((exteriorLightsStatus & (1<<4)) != 0)
                                        .daytimeRunningLightsOn((exteriorLightsStatus & (1<<3)) != 0)
                                        .reverseLightOn        ((exteriorLightsStatus & (1<<2)) != 0)
                                        .fogLightOn            ((exteriorLightsStatus & (1<<1)) != 0)
                                        .parkingLightsOn       ((exteriorLightsStatus & (1<<0)) != 0)
                                        .create(),
                                    new PathHistory()
                             )
                ) :
                null;
            SpecialVehicleContainer specialVehicleContainer = null;
            if (withLowFreq && vehicleRole != 0) {
                switch (vehicleRole) {
                    case 1:
                        logger.debug("Public Transport CAM!");
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new PublicTransportContainer(
                                        embarkationStatus,
                                        new PtActivation(
                                                new PtActivationType(ptActivationType),
                                                new PtActivationData(ptActivationData))));
                        break;
                    case 2:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new SpecialTransportContainer(
                                        new SpecialTransportType(),
                                        new LightBarSirenInUse(
                                                (lightBarSiren & (1<<7)) != 0,
                                                (lightBarSiren & (1<<6)) != 0)));
                        break;
                    case 3:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new DangerousGoodsContainer(DangerousGoodsBasic.fromCode(dangerousGoods)));
                        break;
                    case 4:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new RoadWorksContainerBasic(
                                        new LightBarSirenInUse(
                                                (lightBarSiren & (1<<7)) != 0,
                                                (lightBarSiren & (1<<6)) != 0)));
                        break;
                    case 5:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new RescueContainer(new LightBarSirenInUse(
                                                (lightBarSiren & (1<<7)) != 0,
                                                (lightBarSiren & (1<<6)) != 0)));
                        break;
                    case 6:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new EmergencyContainer(new LightBarSirenInUse(
                                                (lightBarSiren & (1<<7)) != 0,
                                                (lightBarSiren & (1<<6)) != 0)));
                        break;
                    case 7:
                        specialVehicleContainer = new SpecialVehicleContainer(
                                new SafetyCarContainer(new LightBarSirenInUse(
                                                (lightBarSiren & (1<<7)) != 0,
                                                (lightBarSiren & (1<<6)) != 0)));
                        break;
                    default:
                        specialVehicleContainer = null;
                }
            }
            return new Cam(
                    new ItsPduHeader(new MessageId(MessageId.cam)),
                    new CoopAwareness(
                            new GenerationDeltaTime(genDeltaTimeMillis * GenerationDeltaTime.oneMilliSec),
                            new CamParameters(
                                    new BasicContainer(
                                            new StationType(stationType),
                                            new ReferencePosition(
                                                    new Latitude((int)(Latitude.oneMicrodegreeNorth * lpv.position().lattitudeDegrees() / 1.0E-6)),
                                                    new Longitude((int)(Longitude.oneMicrodegreeEast * lpv.position().longitudeDegrees() / 1.0E-6)),
                                                    new PosConfidenceEllipse(
                                                            new SemiAxisLength(SemiAxisLength.unavailable),
                                                            new SemiAxisLength(),
                                                            new HeadingValue(HeadingValue.unavailable)),
                                                    new Altitude(
                                                            new AltitudeValue(AltitudeValue.unavailable),
                                                            AltitudeConfidence.unavailable))),
                                    new HighFrequencyContainer(
                                            BasicVehicleContainerHighFrequency.builder()
                                                .heading(new Heading(
                                                        new HeadingValue((int)(lpv.headingDegreesFromNorth() * 10)),
                                                        new HeadingConfidence(HeadingConfidence.unavailable)))
                                                .speed(lpv.speedMetersPerSecond() < 163.82 ? new Speed(
                                                        new SpeedValue((int)(SpeedValue.oneCentimeterPerSec * lpv.speedMetersPerSecond() * 100.0)),
                                                        new SpeedConfidence(SpeedConfidence.unavailable)):
                                                            new Speed(new SpeedValue(SpeedValue.unavailable), new SpeedConfidence(SpeedConfidence.outOfRange)))
                                                .driveDirection(DriveDirection.fromCode(driveDirection))
                                                .curvature(new Curvature(
                                                        new CurvatureValue(curvature),
                                                        CurvatureConfidence.unavailable))
                                                .yawRate(new YawRate(new YawRateValue(yawRate), YawRateConfidence.unavailable))
                                                .accelerationControl(AccelerationControl.builder()
                                                    .brakePedalEngaged(      (accelerationControlStatus & (1<<7)) != 0)  // is order correct?
                                                    .gasPedalEngaged(        (accelerationControlStatus & (1<<6)) != 0)
                                                    .emergencyBrakeEngaged(  (accelerationControlStatus & (1<<5)) != 0)
                                                    .collisionWarningEngaged((accelerationControlStatus & (1<<4)) != 0)
                                                    .accEngaged(             (accelerationControlStatus & (1<<3)) != 0)
                                                    .cruiseControlEngaged(   (accelerationControlStatus & (1<<2)) != 0)
                                                    .speedLimiterEngaged(    (accelerationControlStatus & (1<<1)) != 0)
                                                    .create())
                                                .create()
                                   ),
                                   lowFrequencyContainer,
                                   specialVehicleContainer)));
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

    private static interface CliOptions {

        @Option(defaultValue="57.0") double getLat();

        @Option(defaultValue="13.0") double getLon();

        @Option SocketAddressFromString getRemoteAddressForUdpLinkLayer();

        @Option int getLocalPortForUdpLinkLayer();

        @Option int getUpperTesterUdpPort();

        @Option(helpRequest = true) boolean getHelp();

        @Option boolean hasEthernetHeader();

        @Option SocketAddressFromString getGpsdServerAddress();

        boolean isGpsdServerAddress();

        @Option MacAddress getMacAddress();

        boolean isMacAddress();

        @Option boolean withoutCAM();

    }

    private Geobroadcast dst_cfg_02() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees() - 0.0001, p.longitudeDegrees() - 0.0001),
                200,
                100,
                90));
    }

    private Geobroadcast dst_cfg_04() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees(), p.longitudeDegrees()),
                100,
                20,
                0));
    }

    private Geobroadcast dst_cfg_08() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees() + 0.0002690677, p.longitudeDegrees() + 0.0013956455),
                100,
                20,
                10));
    }

    private Geobroadcast dst_cfg_08b() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees() + 0.0002690677, p.longitudeDegrees() + 0.0013956455),
                100,
                20,
                10));
    }

    private Geobroadcast dst_cfg_08c() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees() + 0.0002690677, p.longitudeDegrees() + 0.0013956455),
                100,
                20,
                10));
    }

    private Geobroadcast dst_cfg_09b() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.ellipse(
                new Position(p.lattitudeDegrees() + 0.0002690677, p.longitudeDegrees() + 0.0013956455),
                100,
                20,
                10));
    }

    private Geobroadcast dst_cfg_10() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.rectangle(
                new Position(p.lattitudeDegrees() - 0.0001, p.longitudeDegrees() - 0.0001),
                200,
                100,
                90));
    }

    private Geobroadcast dst_cfg_11() {
        Position p = position.getLatestPosition().position();
        return Destination.geobroadcast(Area.rectangle(
                new Position(p.lattitudeDegrees(), p.longitudeDegrees()),
                100,
                20,
                0));
    }


    public static void main(final String[] args) throws InstantiationException, IllegalAccessException, IOException {
        try {
            CliOptions opts = CliFactory.parseArguments(CliOptions.class, args);
            StationConfig config = new StationConfig();
            boolean hasEthernetHeader = opts.hasEthernetHeader();
            if (!hasEthernetHeader && opts.isMacAddress()) {
                logger.error("Can't have Mac address with no ethernet header support!");
                System.exit(1);
            }
            //MacAddress senderMac = new MacAddress(0x00_0C_42_69_9f_aeL);
            //MacAddress senderMac = new MacAddress(0x00_0C_42_69_9f_ccL);
            MacAddress senderMac = opts.isMacAddress() ? opts.getMacAddress() : new MacAddress(0);
            LinkLayer linkLayer = new LinkLayerUdpToEthernet(
                    opts.getLocalPortForUdpLinkLayer(),
                    opts.getRemoteAddressForUdpLinkLayer().asInetSocketAddress(),
                    hasEthernetHeader);
            final UpdatablePositionProvider positionProvider =
                    opts.isGpsdServerAddress() ?
                        new UpdatableGpsdPositionProvider(opts.getGpsdServerAddress().asInetSocketAddress()) :
                        new StaticUpdatablePositionProvider(new Position(opts.getLat(), opts.getLon()));

            final boolean includeCAMs = ! opts.withoutCAM();
            try (ItsStation sut = new ItsStation(config, linkLayer, positionProvider,
                        opts.getUpperTesterUdpPort(), senderMac, includeCAMs)) {

                // Send some DENMs:
                int initialDelay = 10, period = 5;
                sut.scheduledExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override public void run() { sut.send(new Denm(), sut.dst_cfg_08b()); }
                }, initialDelay, period, TimeUnit.SECONDS);

                sut.run();  // Infinite loop.
            }
        } catch (ArgumentValidationException e) {
            logger.error("Invalid CLI argument", e);
            System.exit(1);
        } catch (InvalidOptionSpecificationException e) {
            logger.error("CLI specificaton is not valid", e);
            System.exit(1);
        }
    }
}
