package net.gcdc.uppertester;

public class CamEventIndication implements Response {
    byte messageType = 0x23;
    short camPduLength;
    @SizeFromField("camPduLength")
    byte[] camPdu;

    public CamEventIndication() { this(new byte[] {}); }

    public CamEventIndication(byte[] camPdu) {
        this.camPdu = camPdu.clone();
        this.camPduLength = (short) this.camPdu.length;
    }
}
