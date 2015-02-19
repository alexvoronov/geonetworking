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

import org.threeten.bp.Instant;

public class BtpStdinClient {

    private final static String usage =
            "Usage: java -cp gn.jar StdinClient <local-port> <udp-to-ethernet-remote-address-and-port> <--has-ethernet-header | --no-ethernet-header>";

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println(usage);
            System.exit(1);
        }

        int localPort = Integer.parseInt(args[0]);

        String[] hostAndPort = args[1].split(":");
        if (hostAndPort.length != 2) {
            System.err.println(usage);
            System.exit(1);
        }
        SocketAddress remoteAddress =
                new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));

        boolean hasEthernetHeader = args[2].equalsIgnoreCase("--has-ethernet-header");

        runSenderAndReceiver(localPort, remoteAddress, hasEthernetHeader, System.in, System.out);
    }

    public static void runSenderAndReceiver(
            final int           localPort,
            final SocketAddress remoteAddress,
            final boolean       hasEthernetHeader,
            final InputStream   in,
            final PrintStream   out
            ) throws SocketException {

        LinkLayer linkLayer = new LinkLayerUdpToEthernet(localPort, remoteAddress, hasEthernetHeader);

        PositionProvider positionProvider = new PositionProvider() {
            final Optional<Address> emptyAddress = Optional.empty();
            @Override
            public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(), new Position(13,50),
                        false, 20, 30);
            }
        };

        StationConfig config = new StationConfig();
        final BtpSocket socket = BtpSocket.on(config, linkLayer, positionProvider);

        final Runnable sender = new Runnable() {
            @Override public void run() {
                short destinationPort = (short) 2000;
                String s;
                try (InputStreamReader isr = new InputStreamReader(in);
                        BufferedReader br = new BufferedReader(isr)){
                    s = br.readLine();
                    while(s != null) {
                        socket.send(BtpPacket.singleHop(s.getBytes(), destinationPort));
                        s = br.readLine();
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
