package net.gcdc.geonetworking;


/**
 * Geonetworking ITS station configuration parameters.
 *
 * TODO: read from file or from ASN.1.
 *
 * @author Alex Voronov
 */
public class StationConfig {

    private static  final byte ZERO_BYTE = 0x00;

    private   long itsGnLoacalGnAddr;

    /** Version of the GeoNetworking protocol set in the GeoNetworking protocol headers. */
    private   int itsGnProtocolVersion;  // Version for EN 302 636-4-1 (V1.2.1).

    /** Default packet lifetime in seconds. */
    private  int itsGnDefaultPacketLifetime;

    /** Default hop limit indicating the maximum number of hops a packet travels. */
    private  int itsGnDefaultHopLimit;

    /** Traffic class that represents Facility-layer requirements on packet transport.
     *  Here - Forwarding: Default traffic class */
    private  byte itsGnDefaultTrafficClass;

    /** Indicates whether ITS-S is stationary or mobile. Stationary = 0, Mobile = 1. */
    private  int itsGnIsMobile = 1;

    /** Beacon service: Duration of Beacon retransmit timer in ms. */
    private  int itsGnBeaconServiceRetransmitTimer;

    /** Beacon service: Maximum Beacon Jitter in ms. */
    private  int itsGnBeaconServiceMaxJitter;

    /** Default theoretical maximum communication range in meters. */
    private  int itsGnDefaultMaxCommunicationRange;

    /** Minimum duration a GeoBroadcast packet shall be buffered in the CBF packet buffer in ms. */
    private  int itsGnGeoBroadcastCbfMinTime;

    /** Maximum duration a GeoBroadcast packet shall be buffered in the CBF packet buffer in ms. */
    private  int itsGnGeoBroadcastCbfMaxTime;

    /** Lifetime of location table entry in seconds */
    private  int itsGnLifetimeLocTE;

    /** Default GeoBroadcast forwarding algorithm: 0 unspecified, 1 simple, 2 cbf, 3 advanced. */
    private  int itsGnGeoBroadcastForwardingAlgorithm;

    public long getItsGnLoacalGnAddr() {
        return itsGnLoacalGnAddr;
    }

    public StationConfig() {
        setItsGnLocalGnAddr(Address.random(false, StationType.Passenger_Car, 752).value());
        setItsGnProtocolVersion(0);
        setItsGnDefaultPacketLifetime(60);
        setItsGnDefaultHopLimit(10);
        setItsGnDefaultTrafficClass(ZERO_BYTE);
        setItsGnIsMobile(1);
        setItsGnBeaconServiceRetransmitTimer(3000);
        setItsGnBeaconServiceMaxJitter(getItsGnBeaconServiceRetransmitTimer()/4);
        setItsGnDefaultMaxCommunicationRange(1000);
        setItsGnGeoBroadcastCbfMinTime(1);
        setItsGnGeoBroadcastCbfMaxTime(100);
        setItsGnLifetimeLocTE(20);
        setItsGnGeoBroadcastForwardingAlgorithm(0);
    }



    public void setItsGnLocalGnAddr(long itsGnLoacalGnAddr) {
        this.itsGnLoacalGnAddr = itsGnLoacalGnAddr;
    }

    public int getItsGnProtocolVersion() {
        return itsGnProtocolVersion;
    }

    public void setItsGnProtocolVersion(int itsGnProtocolVersion) {
        this.itsGnProtocolVersion = itsGnProtocolVersion;
    }

    public int getItsGnDefaultPacketLifetime() {
        return itsGnDefaultPacketLifetime;
    }

    public void setItsGnDefaultPacketLifetime(int itsGnDefaultPacketLifetime) {
        this.itsGnDefaultPacketLifetime = itsGnDefaultPacketLifetime;
    }

    public int getItsGnDefaultHopLimit() {
        return itsGnDefaultHopLimit;
    }

    public void setItsGnDefaultHopLimit(int itsGnDefaultHopLimit) {
        this.itsGnDefaultHopLimit = itsGnDefaultHopLimit;
    }

    public byte getItsGnDefaultTrafficClass() {
        return itsGnDefaultTrafficClass;
    }

    public void setItsGnDefaultTrafficClass(byte itsGnDefaultTrafficClass) {
        this.itsGnDefaultTrafficClass = itsGnDefaultTrafficClass;
    }

    public int getItsGnIsMobile() {
        return itsGnIsMobile;
    }

    public void setItsGnIsMobile(int itsGnIsMobile) {
        this.itsGnIsMobile = itsGnIsMobile;
    }

    public int getItsGnBeaconServiceRetransmitTimer() {
        return itsGnBeaconServiceRetransmitTimer;
    }

    public void setItsGnBeaconServiceRetransmitTimer(int itsGnBeaconServiceRetransmitTimer) {
        this.itsGnBeaconServiceRetransmitTimer = itsGnBeaconServiceRetransmitTimer;
    }

    public int getItsGnBeaconServiceMaxJitter() {
        return itsGnBeaconServiceMaxJitter;
    }

    public void setItsGnBeaconServiceMaxJitter(int itsGnBeaconServiceMaxJitter) {
        this.itsGnBeaconServiceMaxJitter = itsGnBeaconServiceMaxJitter;
    }

    public int getItsGnDefaultMaxCommunicationRange() {
        return itsGnDefaultMaxCommunicationRange;
    }

    public void setItsGnDefaultMaxCommunicationRange(int itsGnDefaultMaxCommunicationRange) {
        this.itsGnDefaultMaxCommunicationRange = itsGnDefaultMaxCommunicationRange;
    }

    public int getItsGnGeoBroadcastCbfMinTime() {
        return itsGnGeoBroadcastCbfMinTime;
    }

    public void setItsGnGeoBroadcastCbfMinTime(int itsGnGeoBroadcastCbfMinTime) {
        this.itsGnGeoBroadcastCbfMinTime = itsGnGeoBroadcastCbfMinTime;
    }

    public int getItsGnGeoBroadcastCbfMaxTime() {
        return itsGnGeoBroadcastCbfMaxTime;
    }

    public void setItsGnGeoBroadcastCbfMaxTime(int itsGnGeoBroadcastCbfMaxTime) {
        this.itsGnGeoBroadcastCbfMaxTime = itsGnGeoBroadcastCbfMaxTime;
    }

    public int getItsGnLifetimeLocTE() {
        return itsGnLifetimeLocTE;
    }

    public void setItsGnLifetimeLocTE(int itsGnLifetimeLocTE) {
        this.itsGnLifetimeLocTE = itsGnLifetimeLocTE;
    }

    public int getItsGnGeoBroadcastForwardingAlgorithm() {
        return itsGnGeoBroadcastForwardingAlgorithm;
    }

    public void setItsGnGeoBroadcastForwardingAlgorithm(int itsGnGeoBroadcastForwardingAlgorithm) {
        this.itsGnGeoBroadcastForwardingAlgorithm = itsGnGeoBroadcastForwardingAlgorithm;
    }
}
