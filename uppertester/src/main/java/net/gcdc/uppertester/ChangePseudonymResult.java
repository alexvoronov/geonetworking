package net.gcdc.uppertester;

public class ChangePseudonymResult implements Response {
    byte messageType = 0x05;
    byte result;

    public ChangePseudonymResult(byte result) {
        this.result = result;
    }

    public ChangePseudonymResult() {}
}
