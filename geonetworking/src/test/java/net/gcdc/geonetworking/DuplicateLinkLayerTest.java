package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Test;

public class DuplicateLinkLayerTest {

    @Test
    public void test() throws IOException, InterruptedException {
        DuplicatorLinkLayer d = new DuplicatorLinkLayer();
        LinkLayer l1 = d.get();
        LinkLayer l2 = d.get();
        byte[] data = new byte[] {0x10, 0x13, 0x7F};
        l1.send(data);
        assertArrayEquals(data, l2.receive());
        assertArrayEquals(data, l1.receive());
    }

}
