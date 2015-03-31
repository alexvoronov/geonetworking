package net.gcdc.uppertester;

public class GnTriggerGeoUnicast {
    byte messageType = 0x50;
    long destinationGnAddress;
    short lifetime;
    byte trafficClass;
    short payloadLength;
    byte[] payload;
}
