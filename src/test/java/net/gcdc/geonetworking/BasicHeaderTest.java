package net.gcdc.geonetworking;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

public class BasicHeaderTest {

    private double roundTripError(double seconds) {
        return Math.abs(BasicHeader.Lifetime.fromSeconds(seconds).asSeconds() - seconds);
    }

    @Test
    public void lifetimeRoundtripPrecision() {
        double seconds = 3.5;
        assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        assertThat(roundTripError(seconds), is(not(lessThan(0.10 * seconds))));
        seconds = 0.1; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 0.5; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 1.0; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 2.0; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 5.0; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 7.5; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 13; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 80; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 120; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 500; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
        seconds = 1000; assertThat(roundTripError(seconds), is(lessThan(0.25 * seconds)));
    }

    @Test
    public void headerRoundTrip() {
        BasicHeader h1 = new BasicHeader((byte)0x00,
                BasicHeader.NextHeader.COMMON_HEADER,
                BasicHeader.Lifetime.fromSeconds(5.0),
                (byte) 3);
        ByteBuffer buffer = ByteBuffer.allocate(BasicHeader.LENGTH);
        h1.putTo(buffer);
        assertEquals(buffer.position(), BasicHeader.LENGTH);
        buffer.flip();
        BasicHeader h2 = BasicHeader.getFrom(buffer);
        assertEquals(h1.version(), h2.version());
        assertEquals(h1.nextHeader(), h2.nextHeader());
        assertThat(Math.abs(h1.lifetime().asSeconds() - h2.lifetime().asSeconds()), is(lessThan(0.25 * h1.lifetime().asSeconds())));
        assertEquals(h1.remainingHopLimit(), h2.remainingHopLimit());
    }
}
