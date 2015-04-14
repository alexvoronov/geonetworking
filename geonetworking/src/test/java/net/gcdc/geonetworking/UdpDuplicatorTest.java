package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.gcdc.UdpDuplicator;

import org.junit.Test;

public class UdpDuplicatorTest {

    @Test(timeout=3000)
    public void test() throws IOException, InterruptedException {
        UdpDuplicator d = new UdpDuplicator();
        final int duplicatorPort = 4001;
        final int clientPort = 4000;
        d.add(duplicatorPort, new InetSocketAddress(InetAddress.getLoopbackAddress(), clientPort));
        final DatagramSocket socket = new DatagramSocket(clientPort);
        final byte[] data = new byte[] { 0x10, 0x13, 0x7F };
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        Thread receiver = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    final DatagramPacket packet = new DatagramPacket(new byte[3], 3);
                    socket.receive(packet);
                    org.junit.Assert.assertArrayEquals(data, Arrays.copyOf(packet.getData(), packet.getLength()));
                    socket.receive(packet);
                    assertArrayEquals(data, Arrays.copyOf(packet.getData(), packet.getLength()));
                    //org.junit.Assert.fail("foo");
                } catch (final Throwable e) {
                    exceptions.add(e);
                }
            }
        });
        receiver.start();
        final DatagramPacket packet = new DatagramPacket(data, data.length,
                new InetSocketAddress(InetAddress.getLocalHost(), duplicatorPort));
        socket.send(packet);
        socket.send(packet);
        receiver.join();
        socket.close();
        assertTrue("failed with exception(s)" + exceptions, exceptions.isEmpty());
    }
}
