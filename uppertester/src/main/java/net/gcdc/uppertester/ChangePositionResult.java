package net.gcdc.uppertester;

public class ChangePositionResult implements Response {
    byte messageType = 0x03;
    byte result;

    public ChangePositionResult() {}

    public ChangePositionResult(byte result) {
        this.result = result;
    }

}
