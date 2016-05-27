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
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.gpsdclient.GpsdClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

public class BtpUdpClient {

    private final static Logger logger = LoggerFactory.getLogger(BtpUdpClient.class);

    private final static String usage =
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

        runSenderAndReceiver(localUdp2EthPort, remoteUdp2EthAddress, localDataPort, remoteDataAddress, hasEthernetHeader, positionProvider, btpDestinationPort, macAddress);
    }

    public static void runSenderAndReceiver(
            final int localUdp2EthPort,
            final SocketAddress remoteUdp2EthAddress,
            final int localDataPort,
            final InetSocketAddress remoteDataAddress,
            final boolean hasEthernetHeader,
            final PositionProvider positionProvider,
            final short btpDestinationPort,
            final MacAddress macAddress
            ) throws SocketException {

        LinkLayer linkLayer = new LinkLayerUdpToEthernet(localUdp2EthPort, remoteUdp2EthAddress, hasEthernetHeader);

        StationConfig config = new StationConfig();
        GeonetStation station = new GeonetStation(config, linkLayer, positionProvider, macAddress);
        new Thread(station).start();  // This is ugly API, sorry...
        station.startBecon();
        final BtpSocket socket = BtpSocket.on(station);

        final Runnable sender = new Runnable() {
            @Override public void run() {

                int length = 4096;
                byte[] buffer = new byte[length];
                DatagramPacket udpPacket = new DatagramPacket(buffer, length);

                try (DatagramSocket udpSocket = new DatagramSocket(localDataPort);){
                    while (true) {
                        udpSocket.receive(udpPacket);
                        // We use only destination port, not destination port info.
                        //short btpDestinationPort = (short) (256 * (0xff & udpPacket.getData()[0]) + udpPacket.getData()[1]);
                        short btpDestinationPort = (short)( ((udpPacket.getData()[0]&0xFF)<<8) | (udpPacket.getData()[1]&0xFF) );
                        byte[] btpPayload = Arrays.copyOfRange(udpPacket.getData(), 2, udpPacket.getLength());
                        logger.info("Sending BTP message of size {} to BTP port {}", btpPayload.length, btpDestinationPort);
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
                try (DatagramSocket udpSocket = new DatagramSocket();){
                    while(true) {
                        try {
                            BtpPacket packet = socket.receive();
                            System.arraycopy(packet.payload(), 0, buffer, 2, packet.payload().length);
                            buffer[0] = (byte) (packet.destinationPort() / 256);
                            buffer[1] = (byte) (packet.destinationPort() % 256);
                            udpPacket.setLength(packet.payload().length + 2);
                            udpSocket.send(udpPacket);
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

        new Thread(sender).start();
        new Thread(receiver).start();
    }
}
