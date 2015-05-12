package net.gcdc.vehicle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination.Geobroadcast;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleAdapter {
    private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);

    private final DatagramSocket rcvSocket;
    private final GeonetStation station;
    private final BtpSocket btpSocket;

    private final static short PORT_CAM  = 2001;
    private final static short PORT_DENM = 2002;

    private final static long CAM_INTERVAL_MIN_MS = 100;
    private final static long CAM_INTERVAL_MAX_MS = 1000;
    private final static long CAM_LOW_FREQ_INTERVAL_MS = 500;

    private final static long CAM_INITIAL_DELAY_MS = 20;  // At startup.

    private final static int HIGH_DYNAMICS_CAM_COUNT = 4;

    public final static double CAM_LIFETIME_SECONDS = 0.9;

    public final static int MAX_UDP_LENGTH = 65535;

    public final static int DEFAULT_SIMULINK_UDP_PORT = 5000;

    public static final ExecutorService executor = Executors.newCachedThreadPool();

    private Runnable receiveFromSimulinkLoop = new Runnable() {
        byte[] buffer = new byte[MAX_UDP_LENGTH];
        private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        @Override public void run() {
            try {
                while (true) {
                    rcvSocket.receive(packet);
                    byte[] receivedData = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
                    assert (receivedData.length == packet.getLength());
                    // TODO: unpack data from received
                    // if (isCam) {
                    //   Cam cam = ...;  // TODO: Fill in from receivedData.
                    //   send(cam);
                    // } else if (isDenm) {
                    //   Denm denm = ...;  // TODO: Fill in from receivedData.
                    //   send(denm);
                    // } else if (isIgameMsg) {
                    //   Gcdcm gcdcm = ...;  // TODO: Fill in from receivedData.
                    //   send(gcdcm);
                    // }
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
            try {
                BtpPacket btpPacket = btpSocket.receive();
                switch (btpPacket.destinationPort()) {
                    case PORT_CAM: {
                        Cam cam;
                        try {
                            cam = UperEncoder.decode(btpPacket.payload(), Cam.class);

                            // TODO: Fill in the buffer for packet.

                            packet.setPort(DEFAULT_SIMULINK_UDP_PORT);
                            try {
                                rcvSocket.send(packet);
                            } catch (IOException e) {
                                logger.warn("Failed to send packet to Simulink", e);
                            }
                        } catch (IllegalArgumentException | UnsupportedOperationException e) {
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

    public VehicleAdapter(int portRcvFromSimulink, StationConfig config, LinkLayer linkLayer, PositionProvider position, MacAddress macAddress) throws SocketException {
        rcvSocket = new DatagramSocket(portRcvFromSimulink);
        station = new GeonetStation(config, linkLayer, position, macAddress);
        new Thread(station).start();
        station.startBecon();
        btpSocket = BtpSocket.on(station);
        executor.submit(receiveFromSimulinkLoop);
        executor.submit(sendToSimulinkLoop);
    }

    public static void main(String[] args) {
    }

}
