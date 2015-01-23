package net.gcdc.geonetworking;

import java.io.IOException;

public interface LinkLayer extends AutoCloseable {
    public byte[] receive() throws IOException, InterruptedException;
    public void send(byte[] payload) throws IOException;
}
