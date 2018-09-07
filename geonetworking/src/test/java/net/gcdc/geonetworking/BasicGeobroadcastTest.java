package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.threeten.bp.Instant;


public class BasicGeobroadcastTest {

    @Test(timeout=3000)
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
        LinkLayer l4 = d.get();

        StationConfig config1 = new StationConfig();
        config1.setItsGnBeaconServiceRetransmitTimer(500);
        config1.setItsGnBeaconServiceMaxJitter(500);

        StationConfig config2 = new StationConfig();
        config2.setItsGnBeaconServiceRetransmitTimer(500);
        config2.setItsGnBeaconServiceMaxJitter(500);
        config2.setItsGnGeoBroadcastCbfMaxTime(5);

        StationConfig config3 = new StationConfig();
        config3.setItsGnBeaconServiceRetransmitTimer(500);
        config3.setItsGnBeaconServiceMaxJitter(500);
        config3.setItsGnGeoBroadcastCbfMinTime(99);

        StationConfig config4 = new StationConfig();

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

        PositionProvider posJarntorget = new PositionProvider() {
            @Override public LongPositionVector getLatestPosition() {
                return new LongPositionVector(emptyAddress, Instant.now(),
                        jarntorget, false, 0, 0);
            }
        };

        final BtpSocket socketAtSlottsberget = BtpSocket.on(config1, l1, posSlottsberget);
        final BtpSocket socketAtKuggen       = BtpSocket.on(config2, l2, posKuggen);
        final BtpSocket socketAtSemcon       = BtpSocket.on(config3, l3, posSemcon);
        final BtpSocket socketAtJarntorget   = BtpSocket.on(config4, l4, posJarntorget);

        final byte[] payload = new byte[] {10, 20, 30};
        final short destinationPort = 4001;

        // Kuggen and Semcon inside, Jarntorget outside.
        final Destination destinationKuggen = Destination.geobroadcast(
                Area.rectangle(kuggen, (int) (1.2 * kuggen.distanceInMetersTo(semcon)),
                    (int) (0.5 * kuggen.distanceInMetersTo(semcon)), 50));

        // Send.
        final BtpPacket packet = BtpPacket.customDestination(payload, destinationPort, destinationKuggen);
        socketAtSlottsberget.send(packet);

        // Receive without timeout.
        final BtpPacket packet2 = socketAtKuggen.receive();
        assertArrayEquals(packet.payload(), packet2.payload());

//        // Receive without timeout.
//        BtpPacket packet3 = socketAtSemcon.receive();
//        assertArrayEquals(packet.payload(), packet3.payload());

        // Receive with timeout.
        final List<Throwable> exceptionsSemcon = Collections.synchronizedList(new ArrayList<Throwable>());
        Thread receiverSemcon = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    BtpPacket packet3 = socketAtSemcon.receive();
                    assertArrayEquals(packet.payload(), packet3.payload());
                } catch (final Throwable e) {
                    exceptionsSemcon.add(e);
                }
            }
        });
        receiverSemcon.start();
        Thread.sleep(10);  // In Milliseconds.
        receiverSemcon.interrupt();
        receiverSemcon.join();
        assertTrue(exceptionsSemcon.isEmpty());


        // Receive with timeout. Too far -- should never receive.
        final List<Throwable> exceptionsJarntorget = Collections.synchronizedList(new ArrayList<Throwable>());
        Thread receiverJarntorget = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    @SuppressWarnings("unused")  // We should never receive it.
                    BtpPacket packet4 = socketAtJarntorget.receive();
                    org.junit.Assert.fail("should have never received");
                } catch (final Throwable e) {
                    exceptionsJarntorget.add(e);
                }
            }
        });
        receiverJarntorget.start();
        Thread.sleep(10);  // In Milliseconds.
        receiverJarntorget.interrupt();
        receiverJarntorget.join();
        assertEquals(exceptionsJarntorget.size(), 1);
        assertEquals(exceptionsJarntorget.get(0).getClass(), InterruptedException.class);
    }

}
