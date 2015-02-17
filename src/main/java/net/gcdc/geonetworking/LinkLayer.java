package net.gcdc.geonetworking;

import java.io.IOException;

/** Data Link Layer interface.
 *
 * This is an oversimplification that does not allow specifying LL_ADDR (MAC-address of the
 * receiver). It does not provide a way to ask for local MAC address either for use in GN_ADDR.
 * */
public interface LinkLayer extends AutoCloseable {
    public byte[] receive() throws IOException, InterruptedException;
    public void send(byte[] payload) throws IOException;
}
