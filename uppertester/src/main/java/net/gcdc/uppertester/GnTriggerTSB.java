package net.gcdc.uppertester;

public class GnTriggerTSB {
    byte messageType = 0x54;
    byte numHops;
    short lifetime;
    byte trafficClass;
    short payloadLength;
    byte[] payload;
}
