package net.gcdc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.gpsdclient.GpsdClient;

import org.threeten.bp.Instant;

public class BtpStdinClient {

    private final static String usage =
            "Usage: java -cp gn.jar StdinClient --local-port <local-port> --remote-address <udp-to-ethernet-remote-host-and-port> <--has-ethernet-header | --no-ethernet-header> <--position <lat>,<lon> | --gpsd-server <host>:<port>> --btp-destination-port <port>" + "\n" +
    "BTP ports: 2001 (CAM), 2002 (DENM), 2003 (MAP), 2004 (SPAT).";

    public static void main(String[] args) throws IOException {
        if (args.length < 7) {
            System.err.println(usage);
            System.exit(1);
        }

        int localPort = 0;
        InetSocketAddress remoteAddress = null;
        boolean hasEthernetHeader = false;
        PositionProvider positionProvider = null;
        short btpDestinationPort = (short) 2001;  // CAM

        for (int arg = 0; arg < args.length; arg++) {
            if (args[arg].startsWith("--local-port")) {
                arg++;
                localPort = Integer.parseInt(args[arg]);
            } else if (args[arg].startsWith("--remote-address")) {
                arg++;
                String[] hostPort = args[arg].split(":");
                if (hostPort.length != 2) { System.err.println("Bad utoepy host:port.\n" + usage); System.exit(1); }
                remoteAddress = new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1]));
            } else if (args[arg].startsWith("--has-ethernet-header")) {
                hasEthernetHeader = true;
            } else if (args[arg].startsWith("--no-ethernet-header")) {
                hasEthernetHeader = false;
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
                        new InetSocketAddress(hostPort[0], Integer.parseInt(hostPort[1])));
            } else if (args[arg].startsWith("--btp-destination-port")) {
                arg++;
                btpDestinationPort = Short.parseShort(args[arg]);
            } else {
                throw new IllegalArgumentException("Unrecognized argument: " + args[arg]);
            }
        }

        runSenderAndReceiver(localPort, remoteAddress, hasEthernetHeader, System.in, System.out, positionProvider, btpDestinationPort);
    }

    public static void runSenderAndReceiver(
            final int localPort,
            final SocketAddress remoteAddress,
            final boolean hasEthernetHeader,
            final InputStream in,
            final PrintStream out,
            final PositionProvider positionProvider,
            final short btpDestinationPort
            ) throws SocketException {

        LinkLayer linkLayer = new LinkLayerUdpToEthernet(localPort, remoteAddress, hasEthernetHeader);

        StationConfig config = new StationConfig();
        final BtpSocket socket = BtpSocket.on(config, linkLayer, positionProvider);

        final Runnable sender = new Runnable() {
            @Override public void run() {
                try (InputStreamReader isr = new InputStreamReader(in);
                        BufferedReader br = new BufferedReader(isr)){
                    String dataToSend = br.readLine();  // First line.
                    while(dataToSend != null) {
                        socket.send(BtpPacket.singleHop(dataToSend.getBytes(), btpDestinationPort));
                        dataToSend = br.readLine();  // Consecutive lines.
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable receiver = new Runnable() {
            @Override public void run() {
                while(true) {
                    try {
                        BtpPacket packet = socket.receive();
                        out.println(new String(packet.payload()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        new Thread(sender).start();
        new Thread(receiver).start();
    }
}
