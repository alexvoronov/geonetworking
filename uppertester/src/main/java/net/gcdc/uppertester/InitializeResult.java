package net.gcdc.uppertester;

public class InitializeResult implements Response {

    byte messageType = 0x01;
    byte result;
    
    public InitializeResult() {}

    public InitializeResult(byte result) {
        this.result = result;
    }
    
	@Override public String toString() {
        return "InitializeResult [messageType=" + messageType + ", result=" + result + "]";
    }
}
