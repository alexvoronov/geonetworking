package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

import org.threeten.bp.Instant;

public class ShortPositionVector {
    private final Address   address;
    private final Instant   timestamp;
    private final Position  position;
    /** Length in bytes. */
    public static final int LENGTH = 20;
    
    public ShortPositionVector(
            Address  address,
            Instant  timestamp,
            Position position)
    {
        this.address   = address;
        this.timestamp = timestamp;
        this.position  = position;
    }

    public Address  address()   { return address; }
    public Instant  timestamp() { return timestamp; }
    public Position position()  { return position; }

    public static ShortPositionVector getFrom(ByteBuffer buffer) {
        Address  address   = Address.getFrom(buffer);
        Instant  timestamp = LongPositionVector.millisMod32ToInstant(buffer.getInt());
        Position position  = Position.getFrom(buffer);

        return new ShortPositionVector(address, timestamp, position);
    }
}
