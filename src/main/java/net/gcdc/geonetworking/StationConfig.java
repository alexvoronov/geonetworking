package net.gcdc.geonetworking;

import java.util.Random;

/**
 * Geonetworking ITS station configuration parameters.
 *
 * TODO: read from file or from ASN.1.
 *
 * @author Alex Voronov
 */
public class StationConfig {

    public long itsGnLoacalGnAddr = new Random().nextLong();

    // Version of the GeoNetworking protocol set in the GeoNetworking protocol headers.
    public final int itsGnProtocolVersion = 0;  // Version for EN 302 636-4-1 (V1.2.1).

    // Default packet lifetime [s].
    public int itsGnDefaultPacketLifetimeSeconds = 60;

    // Default hop limit indicating the maximum number of hops a packet travels.
    public int itsGnDefaultHopLimit = 10;

    // Traffic class that represents Facility-layer requirements on packet transport.
    // Here - Forwarding: Default traffic class.
    public byte itsGnDefaultTrafficClass = 0x00;

    // Indicates whether ITS-S is stationary or mobile. Stationary = 0, Mobile = 1.
    public int itsGnIsMobile = 1;
}
