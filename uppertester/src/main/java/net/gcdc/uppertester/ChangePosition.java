package net.gcdc.uppertester;

public class ChangePosition {
    byte messageType = 0x02;
    int deltaLatitude;  // Latitude offset (multiples of 0,1 microdegree)
    int deltaLongitude;  // Longitude offset (multiples of 0,1 microdegree)
    byte deltaElevation;  // Elevation offset (meter)

    @Override public String toString() {
        return "ChangePosition [messageType=" + messageType + ", deltaLatitude=" + deltaLatitude
                + ", deltaLongitude=" + deltaLongitude + ", deltaElevation=" + deltaElevation + "]";
    }
}
