package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;
import org.threeten.bp.Instant;

public class BasciSocketTestOverUdp {

    @Test
    public void test() throws IOException, InterruptedException {

        int client1 = 4440;
        int server1 = 4441;
        int client2 = 4450;
        int server2 = 4451;
        UdpDuplicator d = new UdpDuplicator();
        d.add(server1, new InetSocketAddress(InetAddress.getLocalHost(), client1));
        d.add(server2, new InetSocketAddress(InetAddress.getLocalHost(), client2));

        LinkLayer l1 = new LinkLayerUdpToEthernet(client1,
                new InetSocketAddress(InetAddress.getLocalHost(), server1));
        LinkLayer l2 = new LinkLayerUdpToEthernet(client2,
                new InetSocketAddress(InetAddress.getLocalHost(), server2));

        StationConfig config1 = new StationConfig();
        StationConfig config2 = new StationConfig();

        final Optional<Address> emptyAddress = Optional.empty();

        PositionProvider pos = new PositionProvider() {
            @Override
            public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        new Position(57.7169943, 12.0200253), false, 22, 0);
            }
        };

        BtpSocket socket1 = BtpSocket.on(config1, l1, pos);
        BtpSocket socket2 = BtpSocket.on(config2, l2, pos);

        byte[] data = new byte[] { 0x10, 0x13, 0x7F };
        int port = 2000;
        BtpPacket packet1 = BtpPacket.singleHop(data, (short) port);

        socket1.send(packet1);

        BtpPacket packet2 = socket2.receive();
        assertEquals("ports", packet1.destinationPort(), packet2.destinationPort());
        assertArrayEquals("data and sender payload", data, packet1.payload());
        assertArrayEquals("data and receiver payload", data, packet2.payload());
    }

}
