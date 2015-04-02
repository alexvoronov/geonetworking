package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class CommonHeaderTest {

    @Test
    public void test() {
        CommonHeader h1 = new CommonHeader(
                UpperProtocolType.BTP_A,
                DestinationType.SINGLE_HOP,
                TrafficClass.fromByte((byte)0x00),
                true,
                (short)30,
                (byte)5
                );
        ByteBuffer buffer = ByteBuffer.allocate(CommonHeader.LENGTH);
        h1.putTo(buffer);
        assertEquals(buffer.position(), CommonHeader.LENGTH);
        buffer.flip();
        CommonHeader h2 = CommonHeader.getFrom(buffer);
        assertArrayEquals(buffer.array(), h2.putTo(ByteBuffer.allocate(CommonHeader.LENGTH)).array());
        assertEquals(h1.nextHeader(), h2.nextHeader());
        assertEquals(h1.typeAndSubtype(), h2.typeAndSubtype());
        assertEquals(h1.trafficClass().asByte(), h2.trafficClass().asByte());
        assertEquals("isMobile flag", h1.isMobile(), h2.isMobile());
        assertEquals(h1.payloadLength(), h2.payloadLength());
        assertEquals(h1.maximumHopLimit(), h2.maximumHopLimit());
    }

    @Test public void test2() {
        EqualsVerifier.forClass(CommonHeader.class).verify();;
    }


}
