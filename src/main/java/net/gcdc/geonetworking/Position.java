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
 * NOTE: GPSd can return position that is (NaN, NaN).
 *
 * 32 bit signed integer in [1/10 micro-degree] units.
 */
public class Position {
    @Override
    public String toString() {
        return "Pos[" + lattitudeDegrees + ", " + longitudeDegrees + "]";
    }

    final static double MICRODEGREE = 1E-6;
    final static double STORE_UNIT = 0.1 * MICRODEGREE;


    private final double lattitudeDegrees;
    private final double longitudeDegrees;

    public Position(double lattitudeDegrees, double longitudeDegrees) {
        this.lattitudeDegrees = lattitudeDegrees;
        this.longitudeDegrees = longitudeDegrees;
    }

    public static final int LENGTH = 8;

    public double lattitudeDegrees() { return lattitudeDegrees; }
    public double longitudeDegrees() { return longitudeDegrees; }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + asStoreUnit(lattitudeDegrees);
        result = prime * result + asStoreUnit(longitudeDegrees);
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
        Position other = (Position) obj;
        if (asStoreUnit(lattitudeDegrees) != asStoreUnit(other.lattitudeDegrees))
            return false;
        if (asStoreUnit(longitudeDegrees) != asStoreUnit(other.longitudeDegrees))
            return false;
        return true;
    }

    public double distanceInMetersTo(Position other) {
        return distanceMeters(this, other);
    }

    public double bearingInDegreesTowards(Position other) {
        return bearingDegrees(this, other);
    }

    public static double distanceMeters(Position pos1, Position pos2) {
        return distanceMeters(
                pos1.lattitudeDegrees(), pos1.longitudeDegrees(),
                pos2.lattitudeDegrees(), pos2.longitudeDegrees());
    }

    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // In meters. In miles: 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLng / 2), 2) *
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }

    public static double bearingDegrees(Position pos1, Position pos2) {
        return bearingDegrees(
                pos1.lattitudeDegrees(), pos1.longitudeDegrees(),
                pos2.lattitudeDegrees(), pos2.longitudeDegrees());
    }

    public static double bearingDegrees(double lat1, double lng1, double lat2, double lng2){
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double lngDiffRad = Math.toRadians(lng2-lng1);
        double y = Math.sin(lngDiffRad)*Math.cos(latRad2);
        double x = Math.cos(latRad1) * Math.sin(latRad2) -
                Math.sin(latRad1) * Math.cos(latRad2) * Math.cos(lngDiffRad);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }


}
