package net.gcdc.geonetworking;

public enum StationType {
    Unknown         ( 0),
    Pedestrian      ( 1),
    Cyclist         ( 2),
    Moped           ( 3),
    Motorcycle      ( 4),
    Passenger_Car   ( 5),
    Bus             ( 6),
    Light_Truck     ( 7),
    Heavy_Truck     ( 8),
    Trailer         ( 9),
    Special_Vehicle (10),
    Tram            (11),
    Road_Side_Unit  (15);
    private final int value;
    private StationType(int value) { this.value = value; }
    public  int value()            { return value;       }

    public static StationType fromValue(int value) {
        for (StationType t: StationType.values()) { if (t.value() == value) { return t; } }
        throw new IllegalArgumentException("Unrecognized station type: " + value);
    }
}
