package net.gcdc.uppertester;

public class GnEventIndication implements Response {
    byte   messageType = 0x55;
    short  packetLength;
    @SizeFromField("packetLength")
    byte[] packet;

    public GnEventIndication() {
        this(new byte[] {});
    }

    public GnEventIndication(byte[] packet) {
        this.packet = packet.clone();
        this.packetLength = (short) this.packet.length;
    }

}
