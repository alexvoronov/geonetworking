package net.gcdc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination;
import net.gcdc.geonetworking.GeonetData;
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
import org.threeten.bp.Instant;

public class BtpUdpClient {

    private  static final Logger logger = LoggerFactory.getLogger(BtpUdpClient.class);

    private  static final String usage =
            "Usage: java -cp gn.jar BtpClient --local-udp2eth-port <local-port> --remote-udp2eth-address <udp-to-ethernet-remote-host-and-port> --local-data-port <port> --remote-data-address <host:port> --gpsd-server <host:port> --mac-address <xx:xx:xx:xx:xx:xx>" + "\n" +
    "BTP ports: 2001 (CAM), 2002 (DENM), 2003 (MAP), 2004 (SPAT).";

    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println(usage);
            System.exit(1);
        }

        int localUdp2EthPort = 0;
        InetSocketAddress remoteUdp2EthAddress = null;
        int localDataPort = 0;
        InetSocketAddress remoteDataAddress = null;
        int localDataCamPort = 0;
        InetSocketAddress remoteDataCamAddress = null;
        int localDataIclcmPort = 0;
        InetSocketAddress remoteDataIclcmAddress = null;
        boolean hasEthernetHeader = false;
        PositionProvider positionProvider = null;
        short btpDestinationPort = (short) 2001;  // CAM
        hasEthernetHeader = true;
        MacAddress macAddress = new MacAddress(0);

        for (int arg = 0; arg < args.length; arg++) {
            if (args[arg].startsWith("--local-udp2eth-port")) {
                arg++;
                localUdp2EthPort = Integer.parseInt(args[arg]);
            } else if (args[arg].startsWith("--remote-udp2eth-address")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad udp2eth host:port.\n" + usage); System.exit(1); }
                remoteUdp2EthAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            } else if (args[arg].startsWith("--local-data-port")) {
                arg++;
                localDataPort = Integer.parseInt(args[arg]);
            } else if (args[arg].startsWith("--remote-data-address")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad DATA host:port.\n" + usage); System.exit(1); }
                remoteDataAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            } else if (args[arg].startsWith("--local-data-cam-port")) {
                arg++;
                localDataCamPort = Integer.parseInt(args[arg]);
            } else if (args[arg].startsWith("--remote-data-cam-address")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad DATA host:port.\n" + usage); System.exit(1); }
                remoteDataCamAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            } else if (args[arg].startsWith("--local-data-iclcm-port")) {
                arg++;
                localDataIclcmPort = Integer.parseInt(args[arg]);
            } else if (args[arg].startsWith("--remote-data-iclcm-address")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad DATA host:port.\n" + usage); System.exit(1); }
                remoteDataIclcmAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            } else if (args[arg].startsWith("--position")) {
                arg++;
                String[] latLon = args[arg].split(",");
                if (latLon.length != 2) { System.err.println("Bad lat,lon.\n" + usage); System.exit(1); }
                final double lat = Double.parseDouble(latLon[0]);
                final double lon = Double.parseDouble(latLon[1]);
                final boolean isPositionConfident = true;  // Let's say we know it.
                positionProvider = new PositionProvider() {
                    final Optional<Address> emptyAddress = Optional.empty();
                    @Override public LongPositionVector getLatestPosition() {
                        return new LongPositionVector(emptyAddress, Instant.now(),
                                new Position(lat, lon), isPositionConfident, 0, 0);
                    }
                };
            } else if (args[arg].startsWith("--gpsd-server")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad gpsd host:port.\n" + usage); System.exit(1); }
                positionProvider = new GpsdClient(
                        new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]))).startClient();
            } else if (args[arg].startsWith("--mac-address")) {
                arg++;
                macAddress = new MacAddress(MacAddress.parseFromString(args[arg]));
            } else {
                throw new IllegalArgumentException("Unrecognized argument: " + args[arg]);
            }
        }

        runSenderAndReceiver(localUdp2EthPort, remoteUdp2EthAddress,
                localDataPort, remoteDataAddress,
                localDataCamPort, remoteDataCamAddress,
                localDataIclcmPort, remoteDataIclcmAddress,
                hasEthernetHeader, positionProvider, btpDestinationPort, macAddress);
    }

    public static void runSenderAndReceiver(
            final int localUdp2EthPort,
            final SocketAddress remoteUdp2EthAddress,
            final int localDataPort,
            final InetSocketAddress remoteDataAddress,
            final int localDataCamPort,
            final InetSocketAddress remoteDataCamAddress,
            final int localDataIclcmPort,
            final InetSocketAddress remoteDataIclcmAddress,
            final boolean hasEthernetHeader,
            final PositionProvider positionProvider,
            final short btpDestinationPort,
            final MacAddress macAddress
            ) throws SocketException {

        LinkLayer linkLayer = new LinkLayerUdpToEthernet(localUdp2EthPort, remoteUdp2EthAddress, hasEthernetHeader);

        StationConfig config = new StationConfig();
        final GeonetStation station = new GeonetStation(config, linkLayer, positionProvider, macAddress);
        new Thread(station).start();  // This is ugly API, sorry...
        station.startBecon();
        final BtpSocket socket = BtpSocket.on(station);

        final Runnable senderData = new Runnable() {
            @Override public void run() {

                int length = 4096;
                byte[] buffer = new byte[length];
                DatagramPacket udpPacket = new DatagramPacket(buffer, length);

                try (DatagramSocket udpSocket = new DatagramSocket(localDataPort);){
                    while (true) {
                        udpSocket.receive(udpPacket);
                        byte[] gnPayload = Arrays.copyOfRange(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getOffset() + udpPacket.getLength());
                        logger.info("Sending GN message of size {}", gnPayload.length);
                        Optional<TrafficClass> emptyTrafficClass = Optional.empty();
                        Optional<LongPositionVector> emptySender = Optional.empty();
                        station.send(new GeonetData(
                                UpperProtocolType.BTP_B,
                                Destination.singleHop(),
                                emptyTrafficClass,
                                emptySender,
                                gnPayload
                                ));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        final Runnable senderCam = new Runnable() {
            @Override public void run() {

                int length = 4096;
                byte[] buffer = new byte[length];
                DatagramPacket udpPacket = new DatagramPacket(buffer, length);

                try (DatagramSocket udpSocket = new DatagramSocket(localDataCamPort);){
                    while (true) {
                        udpSocket.receive(udpPacket);
                        short btpDestinationPort = (short) 2001;
                        byte[] btpPayload = Arrays.copyOfRange(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getOffset() + udpPacket.getLength());
                        logger.info("Sending BTP CAM message of size {} to BTP port {}", btpPayload.length, btpDestinationPort);
                        socket.send(BtpPacket.singleHop(btpPayload, btpDestinationPort));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        final Runnable senderIclcm = new Runnable() {
            @Override public void run() {

                int length = 4096;
                byte[] buffer = new byte[length];
                DatagramPacket udpPacket = new DatagramPacket(buffer, length);

                try (DatagramSocket udpSocket = new DatagramSocket(localDataIclcmPort);){
                    while (true) {
                        udpSocket.receive(udpPacket);
                        short btpDestinationPort = (short) 2010;
                        byte[] btpPayload = Arrays.copyOfRange(udpPacket.getData(), udpPacket.getOffset(), udpPacket.getOffset() + udpPacket.getLength());
                        logger.info("Sending BTP i-CLCM message of size {} to BTP port {}", btpPayload.length, btpDestinationPort);
                        socket.send(BtpPacket.singleHop(btpPayload, btpDestinationPort));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable receiver = new Runnable() {
            @Override public void run() {
                int length = 4096;
                byte[] buffer = new byte[length];
                DatagramPacket udpPacket = new DatagramPacket(buffer, length);
                udpPacket.setPort(remoteDataAddress.getPort());
                udpPacket.setAddress(remoteDataAddress.getAddress());

                byte[] bufferCam = new byte[length];
                DatagramPacket udpPacketCam = new DatagramPacket(bufferCam, length);
                if (remoteDataCamAddress != null) {
                    udpPacketCam.setPort(remoteDataCamAddress.getPort());
                    udpPacketCam.setAddress(remoteDataCamAddress.getAddress());
                }

                byte[] bufferIclcm = new byte[length];
                DatagramPacket udpPacketIclcm = new DatagramPacket(bufferIclcm, length);
                if (remoteDataIclcmAddress != null) {
                    udpPacketIclcm.setPort(remoteDataIclcmAddress.getPort());
                    udpPacketIclcm.setAddress(remoteDataIclcmAddress.getAddress());
                }

                try (DatagramSocket udpSocket = new DatagramSocket();){
                    while(true) {
                        try {
                            GeonetData gnData = station.receive();
                            System.arraycopy(gnData.payload, 0, buffer, 0, gnData.payload.length);
                            udpPacket.setLength(gnData.payload.length);
                            udpSocket.send(udpPacket);

                            BtpPacket packet = BtpPacket.fromGeonetData(gnData);
                            if (packet.destinationPort() == 2001 && remoteDataCamAddress != null) {
                                System.arraycopy(packet.payload(), 0, bufferCam, 0, packet.payload().length);
                                udpPacketCam.setLength(packet.payload().length);
                                udpSocket.send(udpPacketCam);

                            } else if (packet.destinationPort() == 2010 && remoteDataIclcmAddress != null) {
                                System.arraycopy(packet.payload(), 0, bufferIclcm, 0, packet.payload().length);
                                udpPacketIclcm.setLength(packet.payload().length);
                                udpSocket.send(udpPacketIclcm);
                            }

                            logger.debug("Received BTP message of size {} to BTP port {}", packet.payload().length, packet.destinationPort());

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(senderData).start();

        if (localDataCamPort != 0) {
            new Thread(senderCam).start();
        }
        if (localDataIclcmPort != 0) {
            new Thread(senderIclcm).start();
        }

        new Thread(receiver).start();
    }
}
