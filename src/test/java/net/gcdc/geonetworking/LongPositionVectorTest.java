package net.gcdc.geonetworking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.threeten.bp.Instant;

public class LongPositionVectorTest {

    @Test
    public void test() {
        LongPositionVector v1 = new LongPositionVector(
                new Address(new java.util.Random().nextLong()),
                Instant.now(),
                new Position(13, 17),
                true,
                22,
                15
                );
        roundTrip(v1);
    }

    @Test
    public void test2() {
        roundTrip(new LongPositionVector(
                new Address(new java.util.Random().nextLong()),
                Instant.now(),
                new Position(13, 17),
                false,
                22,
                15
                ));
    }

    public void roundTrip(LongPositionVector v1) {
        ByteBuffer buffer = ByteBuffer.allocate(LongPositionVector.LENGTH);
        v1.putTo(buffer);
        assertEquals(buffer.position(), LongPositionVector.LENGTH);
        buffer.flip();
        LongPositionVector v2 = LongPositionVector.getFrom(buffer);
        assertEquals(v1.address(), v2.address());
        assertEquals("timestamp", v1.timestamp(), v2.timestamp());
        assertEquals("position", v1.position(), v2.position());
        assertEquals("pai", v1.isPositionConfident(), v2.isPositionConfident());
        assertEquals("speed", v1.speedMetersPerSecond(), v2.speedMetersPerSecond(), 0.1);
        assertEquals("heading", v1.headingDegreesFromNorth(), v2.headingDegreesFromNorth(), 0.1);
        assertArrayEquals("binary", buffer.array(), v2.putTo(ByteBuffer.allocate(LongPositionVector.LENGTH)).array());
    }
}
