package net.gcdc.uppertester;

public class GnTriggerGeoBroadcast {
    byte messageType = 0x51;
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
