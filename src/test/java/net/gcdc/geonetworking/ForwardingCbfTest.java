package net.gcdc.geonetworking;

import java.io.IOException;

import org.junit.Test;
import org.threeten.bp.Instant;


public class ForwardingCbfTest {

    @Test(timeout=300)
    public void test() throws IOException, InterruptedException {
        final Position slottsberget   = new Position(57.702878, 11.927723);
        final Position damen          = new Position(57.703864, 11.945876);
        final Position sannegordshamn = new Position(57.708769, 11.927088);
        final Position semcon         = new Position(57.709526, 11.943996);
        final Position teater         = new Position(57.705714, 11.934608);
        final Position jarntorget     = new Position(57.699786, 11.953191);
        final Position kuggen         = new Position(57.706700, 11.938955);

        DuplicatorLinkLayer d = new DuplicatorLinkLayer();
        LinkLayer l1 = d.get();
        LinkLayer l2 = d.get();
        LinkLayer l3 = d.get();

        StationConfig config1 = new StationConfig();
        config1.itsGnBeaconServiceRetransmitTimer = 500;
        config1.itsGnBeaconServiceMaxJitter = 500;

        StationConfig config2 = new StationConfig();
        config2.itsGnBeaconServiceRetransmitTimer = 500;
        config2.itsGnBeaconServiceMaxJitter = 500;
        config2.itsGnGeoBroadcastCbfMaxTime = 5;

        StationConfig config3 = new StationConfig();
        config3.itsGnBeaconServiceRetransmitTimer = 500;
        config3.itsGnBeaconServiceMaxJitter = 500;
        config3.itsGnGeoBroadcastCbfMinTime = 99;

        final Optional<Address> emptyAddress = Optional.empty();

        PositionProvider posSlottsberget = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        slottsberget, false, 0, 0);
            }
        };

        PositionProvider posKuggen = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        kuggen, false, 0, 0);
            }
        };

        PositionProvider posSemcon = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        semcon, false, 0, 0);
            }
        };

        @SuppressWarnings("unused")
        BtpSocket socket1 = BtpSocket.on(config1, l1, posSlottsberget);
        BtpSocket socket2 = BtpSocket.on(config1, l2, posKuggen);
        //BtpSocket socket3 = BtpSocket.on(config1, l3, posSemcon);

        byte[] payload = new byte[] {10, 20, 30};
        short destinationPort = 4001;

        Destination destination = Destination.geobroadcast(
                Area.rectangle(kuggen, 1.5 * Area.distanceMeters(kuggen, semcon), 0.7 * Area.distanceMeters(kuggen, semcon), 50));

//        BtpPacket packet = BtpPacket.customDestination(payload, destinationPort, destination);
//        socket1.send(packet);

//        byte[] packet1 = l3.receive();
//        byte[] packet2 = l3.receive();
//        assertEquals(packet1[13], packet2[13]);
//        assertEquals(packet1[5], destination.typeAndSubtype().asByte());
//        assertEquals(packet2[5], destination.typeAndSubtype().asByte());

        //byte[] beacon = l2.receive();

        //assertEquals("packet header type and subtype is not beacon", beacon[5], 0b0001_0000);
    }

}
