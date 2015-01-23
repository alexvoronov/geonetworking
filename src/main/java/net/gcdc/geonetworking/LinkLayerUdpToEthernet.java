package net.gcdc.geonetworking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class LinkLayerUdpToEthernet implements LinkLayer, AutoCloseable {

    final SocketAddress remoteAddress;

    final DatagramSocket socket;
    final int BUFFER_LENGTH = 65535;
    final byte[] buffer = new byte[BUFFER_LENGTH];
    final DatagramPacket receptionPacket = new DatagramPacket(buffer, BUFFER_LENGTH);

    public LinkLayerUdpToEthernet(int localPort, SocketAddress remoteAddress)
            throws SocketException {
        this.socket = new DatagramSocket(localPort);
        this.remoteAddress = remoteAddress;
    }

    @Override
    public byte[] receive() throws IOException, SocketTimeoutException {
        socket.receive(receptionPacket);
        return Arrays.copyOfRange(receptionPacket.getData(), 0, receptionPacket.getLength());
    }

    @Override
    public void send(byte[] payload) throws IOException {
        socket.send(new DatagramPacket(payload, payload.length, remoteAddress));
    }

    @Override
    public void close() {
        socket.close();
    }

}
