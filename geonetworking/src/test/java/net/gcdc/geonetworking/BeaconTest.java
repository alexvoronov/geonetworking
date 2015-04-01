package net.gcdc.geonetworking;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.threeten.bp.Instant;


public class BeaconTest {

    @Test(timeout=3000)
    public void test() throws IOException, InterruptedException {
        DuplicatorLinkLayer d = new DuplicatorLinkLayer();
        LinkLayer l1 = d.get();
        LinkLayer l2 = d.get();

        StationConfig config1 = new StationConfig();
        config1.itsGnBeaconServiceRetransmitTimer = 40;
        config1.itsGnBeaconServiceMaxJitter = 10;

        final Optional<Address> emptyAddress = Optional.empty();

        PositionProvider pos = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        new Position(57.7169943, 12.0200253), false, 22, 0);
            }
        };

        @SuppressWarnings("unused")  // Beacons are sent automatically upon socket creation.
        BtpSocket socket1 = BtpSocket.on(config1, l1, pos);

        byte[] beacon = l2.receive();

        assertEquals("packet header type and subtype is not beacon", beacon[5], 0b0001_0000);
    }

}
