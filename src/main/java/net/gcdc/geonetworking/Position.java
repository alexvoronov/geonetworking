package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

/**
 * Geographical position as defined in ETSI TS 102 890-3.
 *
 * Unfortunately, ETSI TS 102 890-3 is scheduled to be published in 2016.
 * Early draft is available for ETSI members, see
 *
 *     http://webapp.etsi.org/WorkProgram/Report_WorkItem.asp?WKI_ID=35130
 *
 * 32 bit signed integer in [1/10 micro-degree] units.
 */
public class Position {
    @Override
    public String toString() {
        return "Position [lattitudeDegrees=" + lattitudeDegrees + ", longitudeDegrees="
                + longitudeDegrees + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        result = prime * result + asStoreUnit(lattitudeDegrees);
        result = prime * result + asStoreUnit(longitudeDegrees);
        return result;
    }

    final static double MICRODEGREE = 1E-6;
    final static double STORE_UNIT = 0.1 * MICRODEGREE;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (asStoreUnit(lattitudeDegrees) != asStoreUnit(other.lattitudeDegrees))
            return false;
        if (asStoreUnit(longitudeDegrees) != asStoreUnit(other.longitudeDegrees))
            return false;
        return true;
    }

    private final double lattitudeDegrees;
    private final double longitudeDegrees;

    public Position(double lattitudeDegrees, double longitudeDegrees) {
        this.lattitudeDegrees = lattitudeDegrees;
        this.longitudeDegrees = longitudeDegrees;
    }

    public static final int LENGTH = 8;

    private static double fromStoreUnit(int latOrLong) {
        return latOrLong * STORE_UNIT;
    }

    private static int asStoreUnit(double latOrLong) {
        return (int)Math.round(latOrLong / STORE_UNIT);
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        buffer.putInt(asStoreUnit(lattitudeDegrees));
        buffer.putInt(asStoreUnit(longitudeDegrees));
        return buffer;
    }

    public static Position getFrom(ByteBuffer buffer) {
        return new Position(fromStoreUnit(buffer.getInt()), fromStoreUnit(buffer.getInt()));
    }
}
