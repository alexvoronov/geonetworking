package net.gcdc.uppertester;

public class GnTriggerResult implements Response {
    byte messageType = 0x41;
    byte result;

    public GnTriggerResult(byte result) {
        this.result = result;
    }

    public GnTriggerResult() {}
}
