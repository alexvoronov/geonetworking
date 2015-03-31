package net.gcdc.uppertester;

public class DenmTrigger {

    byte messageType = 0x10;
    byte flags;
    @Size(6)
    long detectionTime;
    @Size(3)
    int validityDuration;
    @Size(3)
    int repetitionDuration;
    byte infoQuality;
    byte Cause;
    byte subcause;
    byte relevanceDistance;
    byte relevanceTrafficDirection;
    short transmissionInterval;
    short repetitionInterval;
    byte alacarteLength;
    @SizeFromField("alacarteLength")
    byte[] alacarte;

}
