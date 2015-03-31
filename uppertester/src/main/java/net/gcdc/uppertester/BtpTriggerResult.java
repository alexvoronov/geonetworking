package net.gcdc.uppertester;

public class BtpTriggerResult implements Response {
    byte messageType = 0x61;
    byte result;

    public BtpTriggerResult(byte result) {
        this.result = result;
    }

    public BtpTriggerResult() {}
}
