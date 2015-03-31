package net.gcdc.uppertester;

public class CamTriggerChangeCurvature {
    byte messageType = 0x30;
    short curvature;  // Signed integer. Curvature change from -30 000 to 30 001
}
