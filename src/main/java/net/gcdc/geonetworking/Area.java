package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

/**
 * Area according to ETSI EN 302 931 V1.1.1 "Geographical Area Definition".
 *
 */
public class Area {

    private final Position center;
    private final double   distanceAmeters;
    private final double   distanceBmeters;
    private final double   angleDegreesFromNorth;
    private final Type     type;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (short)angleDegreesFromNorth;
        result = prime * result + (short)distanceAmeters;
        result = prime * result + (short)distanceBmeters;
        result = prime * result + ((center == null) ? 0 : center.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Area other = (Area) obj;
        if ((short)angleDegreesFromNorth != (short)other.angleDegreesFromNorth)
            return false;
        if ((short)distanceAmeters != (short)other.distanceAmeters)
            return false;
        if ((short)distanceBmeters != (short)other.distanceBmeters)
            return false;
        if (center == null) {
            if (other.center != null)
                return false;
        } else if (!center.equals(other.center))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public static enum Type {
        CIRCLE    (0),
        RECTANGLE (1),
        ELLIPSE   (2);

        private final int code;
        private Type(int code) { this.code = code; }
        public  int code()     { return code;      }

        public static Area.Type fromCode(int code) {
            for (Area.Type h: Area.Type.values()) { if (h.code() == code) { return h; } }
            throw new IllegalArgumentException("Can't recognize area type: " + code);
        }
    }


    private Area(Position center, double distanceA, double distanceB, double angleDegreesFromNorth, Type type) {
        this.center    = center;
        this.distanceAmeters = distanceA;
        this.distanceBmeters = distanceB;
        this.angleDegreesFromNorth = angleDegreesFromNorth;
        this.type = type;
    }

    public static Area getFrom(ByteBuffer buffer, Area.Type type) {
        Position center = Position.getFrom(buffer);
        double distanceA = buffer.getShort() & 0xffff;  // Convert to int to remove sign from short.
        double distanceB = buffer.getShort() & 0xffff;
        double angleDegreesFromNorth = buffer.getShort() & 0xffff;
        return new Area(center, distanceA, distanceB, angleDegreesFromNorth, type);
    }

    public ByteBuffer putTo(ByteBuffer buffer) {
        center.putTo(buffer);
        buffer.putShort((short) distanceAmeters);
        buffer.putShort((short) distanceBmeters);
        buffer.putShort((short) angleDegreesFromNorth);
        return buffer;
    }

    public Area.Type type() {
        return type;
    }

    public boolean contains(Position position) {
        return f(position) >= 0;
    }

    /**  Characteristic function of a geographical area.
     *
     * The function has the following properties:
     * <pre>
     *     = 1 for x = 0 and y = 0 (at the center point)
     *     > 0 inside the geographical area
     *     = 0 at the border of the geographical area
     *     < 0 outside the geographical area
     * </pre>
     * where x, y are the geographical coordinates of a position P in a Cartesian coordinate system
     * with origin in the center of the shape and abscissa parallel to the long side of the shape.
     */
    public double f(Position position) {
        double distance = this.center.distanceInMetersTo(position);
        double bearing = this.center.bearingInDegreesTowards(position);
        double relativeAngle = bearing - angleDegreesFromNorth;
        double x = distance * Math.cos(Math.toRadians(relativeAngle));
        double y = distance * Math.sin(Math.toRadians(relativeAngle));
        double a = distanceAmeters;
        double b = distanceBmeters;
        switch (type) {
            case CIRCLE:
            case ELLIPSE:
                return 1 - Math.pow(x/a, 2) - Math.pow(y/b, 2);
            case RECTANGLE:
                return Math.min(1 - Math.pow(x/a, 2), 1 - Math.pow(y/b, 2));
            default:
                return 0;  // At a border of an unknown shape...
        }
    }



    public static Area circle(Position center, double radius) {
        return new Area(center, radius, radius, 0, Type.CIRCLE);
    }

    /**
     *
     * @param center               position of the center point
     * @param longDistanceMeters   half the long side -- the (longer) distance between the center point and the short side of the rectangle (perpendicular bisector of the short
side);
     * @param shortDistanceMeters  half the short side -- the (shorter) distance between the center point and the long side of the rectangle (perpendicular bisector of the long
side);
     * @param azimuthAngleDegreesFromNorth  azimuth angle of the long side of the rectangle.
     * @return
     */
    public static Area rectangle(
            Position center,
            double longDistanceMeters,
            double shortDistanceMeters,
            double azimuthAngleDegreesFromNorth) {
        return new Area(center, longDistanceMeters, shortDistanceMeters, azimuthAngleDegreesFromNorth,
                Type.RECTANGLE);
    }

    public static Area ellipse(
            Position center,
            double longSemiAxisMeters,
            double shortSemiAxisMeters,
            double azimuthAngleDegreesFromNorth) {
        return new Area(center, longSemiAxisMeters, shortSemiAxisMeters,
                azimuthAngleDegreesFromNorth, Type.ELLIPSE);
    }

}
