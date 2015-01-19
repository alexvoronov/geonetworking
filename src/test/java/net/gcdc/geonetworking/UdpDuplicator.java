package net.gcdc.geonetworking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class UdpDuplicator {

    private class Client implements Runnable {
        public final int localPort;
        public final SocketAddress remoteAddress;
        public final DatagramSocket socket;
        public Client(int localPort, SocketAddress remoteAddress) throws SocketException {
            this.localPort = localPort;
            this.remoteAddress = remoteAddress;
            this.socket =  new DatagramSocket(this.localPort);
        }
        public void send(byte[] payload) throws IOException {
            socket.send(new DatagramPacket(payload, payload.length, remoteAddress));
        }

        @Override
        public void run() {
            int length = 3000;
            byte[] buffer = new byte[length];
            DatagramPacket packet = new DatagramPacket(buffer, length);
            try {
                socket.receive(packet);
                byte[] payload = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                sendAll(payload);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private final ArrayList<Client> clients = new ArrayList<>();

    public UdpDuplicator() {
    }

    public void sendAll(byte[] payload) throws IOException {
        for (Client c : clients) {
            c.send(payload);
        }
    }

    public void add(int localPort, SocketAddress remoteAddress) throws SocketException {
        Client c = new Client(localPort, remoteAddress);
        new Thread(c).start();
        clients.add(c);
    }
}
