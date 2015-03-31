package net.gcdc.uppertester;

public class InitializeResult implements Response {
    @Override public String toString() {
        return "InitializeResult [messageType=" + messageType + ", result=" + result + "]";
    }

    public InitializeResult() {}

    public InitializeResult(byte result) {
        this.result = result;
    }

    byte messageType = 0x01;
    byte result;
}
