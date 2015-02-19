package net.gcdc.geonetworking;

import java.io.IOException;

/** Data Link Layer interface.
 *
 * If hasEthernetHeader is true, then the payload has an additional 14-byte header: 6-byte
 * destination LL_ADDR (destination MAC address), 6-byte source LL_ADDR (source MAC address), and
 * 2-byte Ethernet II Ethertype (0x8947 for Geonetworking, see
 * http://standards.ieee.org/develop/regauth/ethertype/eth.txt ). The source MAC address for send()
 * can be provided as all zeros, and implementation shall replace such all-zeros source MAC by a
 * real (physical) MAC address.
 *
 * At the moment there is no way to get the real (physical) MAC address for use in GN_ADDR.
 */
public interface LinkLayer extends AutoCloseable {
    public byte[] receive() throws IOException, InterruptedException;
    public void send(byte[] payload) throws IOException;
    public boolean hasEthernetHeader();
}
