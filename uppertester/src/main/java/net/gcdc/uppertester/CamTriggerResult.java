package net.gcdc.uppertester;

public class CamTriggerResult implements Response {
    byte messageType = 0x21;
    byte result;

    public CamTriggerResult(byte result) {
        this.result = result;
    }

    public CamTriggerResult() {}
}
