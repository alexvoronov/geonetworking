package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.Month;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

/**
 * Long Position Vector containing detailed station information.
 *
 * Usually used to describe sender position.
 *
 * Long Position Vector contains Geonetworking Address, Timestamp, Position, PAI, Speed and Heading.
 *
 * Timestamp.
 * time in milliseconds at which the latitude and longitude of the ITS-S were
 * acquired
 * by the GeoAdhoc router. The time is encoded as:
 * TST = TST(TAI) mod 2^32
 * where TST(TAI) is the number of elapsed TAI milliseconds since 2004-01-01
 * 00:00:00.000 UTC.
 * http://leapsecond.com/java/gpsclock.htm
 * TAI just adds 35 leap seconds to UTC.
 * 2^32 milliseconds is about 49 days.
 *
 * PAI.
 * Position accuracy indicator of the GeoAdhoc router reference position.
 * Set to 1 if the semiMajorConfidence of the PosConfidenceEllipse as specified
 * in ETSI TS 102 894-2 [i.7] is smaller than the GN protocol constant
 * itsGnPaiInterval / 2.
 * Set to 0 otherwise.
 *
 * Speed.
 * Later it has to be encoded as a 15 bit signed integer, where each unit is 0.01 m/s,
 * which gives maximum speed of (2^15 - 1) * 0.01 = 327.67 m/s = 1179.61 km/h.
 *
 * Heading.
 * Later it has to be encoded as an unsigned units of 0.1 degree from North.
 */
public class LongPositionVector {

    private final Optional<Address>  address;
    private final Instant            timestamp;
    private final Position           position;
    private final boolean            isPositionConfident;
    private final double             speedMetersPerSecond;
    private final double             headingDegreesFromNorth;

    public Optional<Address> address()                 { return address; }
    public Instant           timestamp()               { return timestamp; }
    public Position          position()                { return position; }
    public boolean           isPositionConfident()     { return isPositionConfident; }
    public double            speedMetersPerSecond()    { return speedMetersPerSecond; }
    public double            headingDegreesFromNorth() { return headingDegreesFromNorth; }


    //private long taiMillisSince2004Mod32;

    public LongPositionVector(
            Address  address,
            Instant  timestamp,
            Position position,
            boolean  isPositionConfident,
            double   speedMetersPerSecond,
            double   headingDegreesFromNorth)
    {
        this(Optional.of(address),
            timestamp,
            position,
            isPositionConfident,
            speedMetersPerSecond,
            headingDegreesFromNorth);
    }

    public LongPositionVector(
            Optional<Address>  address,
            Instant  timestamp,
            Position position,
            boolean  isPositionConfident,
            double   speedMetersPerSecond,
            double   headingDegreesFromNorth)
    {
        this.address                 = address;
        this.timestamp               = timestamp;
        this.position                = position;
        this.isPositionConfident     = isPositionConfident;
        this.speedMetersPerSecond    = speedMetersPerSecond;
        this.headingDegreesFromNorth = headingDegreesFromNorth;
    }

    /** Long Position Vector length in bytes. */
    public static final int LENGTH = 24;


    private final static double SPEED_STORE_SCALE   = 0.01;  // 0.01 meters per second.
    private final static double HEADING_STORE_SCALE = 0.1;   // 0.1 degrees from north.

    private int speedAsStoreUnit(double speedMetersPerSecond) {
        return (int) Math.round(speedMetersPerSecond / SPEED_STORE_SCALE);
    }

    private short headingAsStoreUnit(double headingDegreesFromNorth) {
        return (short) Math.round(headingDegreesFromNorth / HEADING_STORE_SCALE);
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        if (address.isPresent()) {
            address.get().putTo(buffer);
        } else {
            throw new IllegalStateException("Address not initialized in Long Position Vector.");
        }
        buffer.putInt((int)instantToTaiMillisSince2004Mod32(timestamp));
        position.putTo(buffer);
        // Bit 15 is position accuracy indication isPositionAccurate
        // Bits 0-14 are speed. Negative numbers have all 1-s in the BIG-END, so if speed is
        // negative, we need to remove the 1 from the bit 15.
        short speedMask = 0b0111_1111_1111_1111;
        int speedRoundedCentimetersPerSecond = speedAsStoreUnit(speedMetersPerSecond);
        if (Math.abs(speedRoundedCentimetersPerSecond) >= Math.pow(2, 15)) {
            throw new IllegalStateException("Speed is too high and requires longer that 14 bits (" +
                    speedMetersPerSecond  + " m/s, max is " + ((Math.pow(2, 15) - 1) * 0.01) + ")");
        }
        short confidenceAndSpeed = (short) (( (isPositionConfident ? 1 : 0) << 15 ) |
                (speedMask & (short) speedRoundedCentimetersPerSecond));
        buffer.putShort(confidenceAndSpeed);
        buffer.putShort(headingAsStoreUnit(headingDegreesFromNorth));
        return buffer;
    }

    public static LongPositionVector getFrom(ByteBuffer buffer) {
        Address  address         = Address.getFrom(buffer);
        Instant  timestamp       = millisMod32ToInstant(buffer.getInt());
        Position position        = Position.getFrom(buffer);
        short confidenceAndSpeed = buffer.getShort();
        // Bit 15 is a position accuracy indicator.
        boolean  isPositionConfident = ((confidenceAndSpeed & 0xFFFF) >> 15) == 1;  // & 0xFFFF to deal with signed short to int without sign.
        // Bits 0-14 are signed units of speed, in 0.01 meters per second.
        short speedMask     = 0b0111_1111_1111_1111;
        short speed15bit    = (short) (confidenceAndSpeed & speedMask);
        // Since speed is encoded in two-complement, the last bit (14) can be used as a sign bit.
        short speedSignMask = 0b0100_0000_0000_0000;
        boolean isNegativeSpeed = (speed15bit & speedSignMask) != 0;
        // Positive: shortened 15-bit and normal 16-bit integers are the same.
        // Negative: shortened 15-bit and normal 16-bit integers are different only in the last bit.
        short speed = (short) (speed15bit | (isNegativeSpeed ? 1<<15 : 0));
        // Speed was encoded as 0.01 meters per second.
        double   speedMetersPerSecond = speed * SPEED_STORE_SCALE;
        // Heading was encoded as an unsigned units of 0.1 degree from North.
        double   headingDegreesFromNorth = buffer.getShort() * HEADING_STORE_SCALE;
        return new LongPositionVector(
            address,
            timestamp,
            position,
            isPositionConfident,
            speedMetersPerSecond,
            headingDegreesFromNorth
            );
    }

    private static final long LEAP_SECONDS_SINCE_2004 = 3;  // Let's assume we're always in 2014.

    /** Returns TAI milliseconds mod 2^32 for the given date.
     *
     * Since java int is signed 32 bit integer, return long instead.
     * It is the same on byte level, but just to avoid confusing people with negative values here.
     *
     *
     * From http://stjarnhimlen.se/comp/time.html:
     *
     * TAI (Temps Atomique International or International Atomic Time) is
     * defined as the weighted average of the time kept by about 200
     * atomic clocks in over 50 national laboratories worldwide.
     * TAI-UT1 was approximately 0 on 1958 Jan 1.
     * (TAI is ahead of UTC by 35 seconds as of 2014.)
     *
     * GPS time = TAI - 19 seconds.  GPS time matched UTC from 1980-01-01
     * to 1981-07-01.  No leap seconds are inserted into GPS time, thus
     * GPS time is 13 seconds ahead of UTC on 2000-01-01.  The GPS epoch
     * is 00:00 (midnight) UTC on 1980-01-06.
     * The difference between GPS Time and UTC changes in increments of
     * seconds each time a leap second is added to UTC time scale.
     */
    public static long instantToTaiMillisSince2004Mod32(Instant instantX) {
        OffsetDateTime gnEpochStart =
                OffsetDateTime.of(LocalDateTime.of(2004, Month.JANUARY, 1, 0, 0), ZoneOffset.UTC);
        long millis2004 = gnEpochStart.toInstant().toEpochMilli();
        long millisAtX = instantX.toEpochMilli();
        long taiMillis = (millisAtX + LEAP_SECONDS_SINCE_2004*1000) - millis2004;
        return taiMillis % (1L << 32);
    }

    /** Returns the nearest to now instant that will have given amount of TAI millis since 2004. */
    public static Instant millisMod32ToInstant(int intMillisX) {
        long millisX = Long.parseLong(Integer.toBinaryString(intMillisX), 2);  // unsigned int...
        Instant now = Instant.now();
        long millisNow = instantToTaiMillisSince2004Mod32(now);
        long delta = millisNow - millisX;
        // Small positive delta is what we expect.
        // Small negative delta is fine too, it would mean that the packet came from the future,
        // which can be explained by our clock being a little behind.
        // Huge negative delta is from previous mod32, and should be changed to small positive.
        // Huge positive delta might come from a packet a little from the future and next mod32,
        // we want instead a small negative delta.
        if (delta < -(1L << 31)) { delta += (1L << 32); }
        if (delta >  (1L << 31)) { delta -= (1L << 32); }
        Instant instantX = now.minusMillis(delta);
        return instantX;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + headingAsStoreUnit(headingDegreesFromNorth);
        result = prime * result + (isPositionConfident ? 1231 : 1237);
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + speedAsStoreUnit(speedMetersPerSecond);
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LongPositionVector other = (LongPositionVector) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (headingAsStoreUnit(headingDegreesFromNorth) != headingAsStoreUnit(other.headingDegreesFromNorth))
            return false;
        if (isPositionConfident != other.isPositionConfident)
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        if (speedAsStoreUnit(speedMetersPerSecond) != speedAsStoreUnit(other.speedMetersPerSecond))
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    public LongPositionVector withAddress(Address address) {
        return new LongPositionVector(
                address,
                timestamp,
                position,
                isPositionConfident,
                speedMetersPerSecond,
                headingDegreesFromNorth
                );
    }
}