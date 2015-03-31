package net.gcdc.uppertester;

public class DenmUpdate {
    byte messageType = 0x12;
    byte flags;
    int stationId;
    short sequenceNo;
    @Size(6)
    long detectionTime;
    @Size(3)
    int validityDuration;
    byte infoQuality;
    byte cause;
    byte subcause;
    byte relevanceDistance;
    byte relevanceTrafficDirection;
    short transmissionInterval;
    short repetitionInterval;
    byte alacarteLength;
    @SizeFromField("alacarteLength")
    byte[] alacarte;
}
