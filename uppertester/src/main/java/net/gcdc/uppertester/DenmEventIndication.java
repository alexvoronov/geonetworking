package net.gcdc.uppertester;

public class DenmEventIndication implements Response {
    byte messageType = 0x17;
    short denmPduLength;
    @SizeFromField("denmPduLength")
    byte[] denmPdu;

    public DenmEventIndication() { this(new byte[] {}); }

    public DenmEventIndication(byte[] denmPdu) {
        this.denmPdu = denmPdu.clone();
        this.denmPduLength = (short)this.denmPdu.length;
    }
}
