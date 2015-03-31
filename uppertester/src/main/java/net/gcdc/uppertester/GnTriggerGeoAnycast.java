package net.gcdc.uppertester;

public class GnTriggerGeoAnycast {
    byte messageType = 0x52;
    byte shape;
    short lifetime;
    byte trafficClass;
    byte reserved1;
    byte reserved2;
    byte reserved3;
    int latitude;
    int longitude;
    short distanceA;
    short distanceB;
    short angle;
    short payloadLength;
    byte[] payload;
}
