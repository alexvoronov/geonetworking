package net.gcdc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UdpDuplicatorRunner {

    public static void main(String[] args) throws IOException {
        UdpDuplicator d = new UdpDuplicator();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();
        while (s != null) {
            String[] elems = s.split("\\s+");
            if (elems.length != 2) { break; }
            int localPort = Integer.parseInt(elems[0]);
            String[] hostAndPort = elems[1].split(":");
            if (hostAndPort.length != 2) { break; }
            SocketAddress remoteAddress =
                    new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
            d.add(localPort, remoteAddress);
            s = br.readLine();
        }
    }
}
