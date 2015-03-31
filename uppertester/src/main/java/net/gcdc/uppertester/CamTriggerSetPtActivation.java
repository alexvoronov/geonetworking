package net.gcdc.uppertester;

public class CamTriggerSetPtActivation {
    byte messageType = 0x3c;
    byte ptActiavtionType;
    byte ptActivationDataLength;
    @SizeFromField("ptActivationDataLength")
    byte[] ptActivationData;
}
