package net.gcdc.uppertester;

public class BtpEventIndication implements Response {
    byte   messageType = 0x63;
    short  packetLength;
    byte[] packet;

    public BtpEventIndication() {
        this(new byte[] {});
    }

    public BtpEventIndication(byte[] packet) {
        this.packet = packet.clone();
        this.packetLength = (short) this.packet.length;
    }
}
