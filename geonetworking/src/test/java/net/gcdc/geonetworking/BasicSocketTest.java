package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.threeten.bp.Instant;


public class BasicSocketTest {

    @Test(timeout=3000)
    public void test() throws IOException, InterruptedException {
        DuplicatorLinkLayer d = new DuplicatorLinkLayer();
        LinkLayer l1 = d.get();
        LinkLayer l2 = d.get();

        StationConfig config1 = new StationConfig();
        StationConfig config2 = new StationConfig();

        final Optional<Address> emptyAddress = Optional.empty();

        PositionProvider pos = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        new Position(57.7169943, 12.0200253), false, 22, 0);
            }
        };

        BtpSocket socket1 = BtpSocket.on(config1, l1, pos);
        BtpSocket socket2 = BtpSocket.on(config2, l2, pos);

        byte[] data = new byte[] {0x10, 0x13, 0x7F};
        int port = 2000;
        BtpPacket packet1 = BtpPacket.singleHop(data, (short) port);

        socket1.send(packet1);

        BtpPacket packet2 = socket2.receive();

        assertEquals("ports", packet1.destinationPort(), packet2.destinationPort());
        assertArrayEquals("data and sender payload", data, packet1.payload());
        assertArrayEquals("data and receiver payload", data, packet2.payload());


        socket1.send(packet1);

        BtpPacket packet3 = socket2.receive();

        assertEquals("ports", packet1.destinationPort(), packet3.destinationPort());
        assertArrayEquals("data and sender payload", data, packet1.payload());
        assertArrayEquals("data and receiver payload", data, packet3.payload());

    }

}
