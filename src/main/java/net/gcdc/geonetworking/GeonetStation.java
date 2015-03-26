package net.gcdc.geonetworking;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.gcdc.geonetworking.Destination.Geobroadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;


/* Java and ETSI both use Big Endian. */
public class GeonetStation implements Runnable, AutoCloseable {

    private StationConfig                         config;
    private LinkLayer                             linkLayer;
    private PositionProvider                      positionProvider;
    private final LinkedBlockingQueue<GeonetData> queueUpward = new LinkedBlockingQueue<>();
    private final Collection<GeonetDataListener>  listeners = new ArrayList<>();
    private int                                   nextFreeSequenceNumber = 0;
    private final MacAddress senderMac;

    private LocationTable locationTable;
    private final static Logger logger = LoggerFactory.getLogger(GeonetStation.class);

    public final static short GN_ETHER_TYPE = (short) 0x8947;
    private final static MacAddress BROADCAST_MAC = new MacAddress(0xff_ff_ff_ff_ff_ffL);
    private final static MacAddress EMPTY_MAC = new MacAddress(0);
    private final static int ETHER_HEADER_LENGTH = 14;

    public GeonetStation(StationConfig config, LinkLayer linkLayer, PositionProvider positionProvider) {
        this(config, linkLayer, positionProvider, EMPTY_MAC);
    }

    public GeonetStation(StationConfig config, LinkLayer linkLayer, PositionProvider positionProvider, MacAddress senderMac) {
        this.config = config;
        if (senderMac.value() != 0) {
            config.itsGnLoacalGnAddr = new Address(false, StationType.Passenger_Car, 752, senderMac.value()).value();
        }
        this.linkLayer = linkLayer;
        this.positionProvider = positionProvider;
        this.senderMac = senderMac;
        this.locationTable = new LocationTable(new ConfigProvider() {
            @Override public StationConfig config() { return GeonetStation.this.config;}
        });
        logger.info("Initialized station with GN address {} and MAC address {}", config.itsGnLoacalGnAddr, this.senderMac);
    }

    private short sequenceNumber() {
        short result = (short) nextFreeSequenceNumber;
        nextFreeSequenceNumber++;
        if (nextFreeSequenceNumber > (1 << 16) - 1) { nextFreeSequenceNumber = 0; }
        return result;
    }

    private BasicHeader basicHeader(GeonetData data) {
        return new BasicHeader(
                (byte) config.itsGnProtocolVersion,
                BasicHeader.NextHeader.COMMON_HEADER,
                BasicHeader.Lifetime.fromSeconds(data.destination.maxLifetimeSeconds().orElse(
                        (double) config.itsGnDefaultPacketLifetime)),
                data.destination.remainingHopLimit().orElse((byte) config.itsGnDefaultHopLimit));
    }

    private CommonHeader commonHeader(GeonetData data) {
        return new CommonHeader(
                data.protocol,
                data.destination.typeAndSubtype(),
                data.trafficClass.orElse(TrafficClass.fromByte(config.itsGnDefaultTrafficClass)),
                config.itsGnIsMobile == 1,
                (short) data.payload.length,
                data.destination.maxHopLimit().orElse((byte) config.itsGnDefaultHopLimit));
    }

    /** Adds a listener for GeonetData indications (received messages from link layer).
     *
     * Listeners do not disable queue-based solution, so someone have to empty that queue using
     * {@link #receive()} to avoid out-of-memory problems.
     **/
    public void addGeonetDataListener(GeonetDataListener listener) {
        listeners.add(listener);
    }

    public void removeGeonetDataListener(GeonetDataListener listener) {
        listeners.remove(listener);
    }

    public void send(GeonetData data) throws IOException {

        logger.debug("Sending message, hasEtherHeader: {}, sender mac {}", linkLayer.hasEthernetHeader(), senderMac);

        BasicHeader basicHeader = basicHeader(data);
        CommonHeader commonHeader = commonHeader(data);
        LongPositionVector senderPosition = data.sender.orElse(positionProvider.getLatestPosition());
        LongPositionVector positionVector = senderPosition.address().isPresent() ?
                senderPosition : senderPosition.withAddress(new Address(config.itsGnLoacalGnAddr));

        switch (data.destination.typeAndSubtype()) {
            case SINGLE_HOP: {
                int mediaInfo = 0x0000; // Reserved 32-bit field for media-dependent operations.
                                        // Set to 0 if not used. Can be used for DCC-related
                                        // information in ITS-G5 (ETSI TS 102 636-4-2).

                ByteBuffer llPayload = ByteBuffer.allocate(
                        (linkLayer.hasEthernetHeader() ? ETHER_HEADER_LENGTH : 0) +
                        40 + data.payload.length);
                if (linkLayer.hasEthernetHeader()) {
                    llPayload.put(BROADCAST_MAC.asBytes());
                    llPayload.put(senderMac.asBytes());
                    llPayload.putShort(GN_ETHER_TYPE);
                }
                basicHeader.putTo(llPayload);    // octets 0-3
                commonHeader.putTo(llPayload);   // octets 4-11
                positionVector.putTo(llPayload); // octets 12-35
                llPayload.putInt(mediaInfo);     // octets 36-39
                llPayload.put(data.payload);

                sendToLowerLayer(llPayload);
                beaconService.skipNextBeacon();  // Beacon is redundant with Single Hop Broadcast.
                break;
            }
            case GEOBROADCAST_CIRCLE:
            case GEOBROADCAST_ELLIPSE:
            case GEOBROADCAST_RECTANGLE:
            case GEOANYCAST_CIRCLE:
            case GEOANYCAST_ELLIPSE:
            case GEOANYCAST_RECTANGLE: {
                ByteBuffer llPayload = ByteBuffer.allocate(
                        (linkLayer.hasEthernetHeader() ? ETHER_HEADER_LENGTH : 0) +
                        56 + data.payload.length);
                if (linkLayer.hasEthernetHeader()) {
                    MacAddress dstMac = BROADCAST_MAC;
                    Area area = ((Destination.Geobroadcast)data.destination).area();
                    if (!area.contains(position())) {
                        logger.debug("Area {} don't contains me {}", area, position());
                        Optional<MacAddress> betterDstMac = locationTable.closerThanMeTo(
                            area.center(), position());
                        if (betterDstMac.isPresent()) { dstMac = betterDstMac.get(); }
                    }
                    llPayload.put(dstMac.asBytes());
                    llPayload.put(senderMac.asBytes());
                    llPayload.putShort(GN_ETHER_TYPE);
                }
                basicHeader.putTo(llPayload);               // Octets  0- 3
                commonHeader.putTo(llPayload);              // Octets  4-11
                llPayload.putShort(sequenceNumber());       // Octets 12-13
                llPayload.putShort((short)0);  // Reserved. // Octets 14-15
                positionVector.putTo(llPayload);            // Octets 16-39
                ((Destination.Geobroadcast)data.destination).area().putTo(llPayload);  // Octets 40-53
                llPayload.putShort((short)0);  // Reserved. // Octets 54-55
                llPayload.put(data.payload);

                sendToLowerLayer(llPayload);
                break;
            }
            case BEACON:
                logger.debug("Send, BEACON, hasEtherHeader: {}", linkLayer.hasEthernetHeader());
                ByteBuffer llPayload = ByteBuffer.allocate(
                        (linkLayer.hasEthernetHeader() ? ETHER_HEADER_LENGTH : 0) +
                        36);
                if (linkLayer.hasEthernetHeader()) {
                    llPayload.put(BROADCAST_MAC.asBytes());
                    llPayload.put(senderMac.asBytes());
                    llPayload.putShort(GN_ETHER_TYPE);
                }
                basicHeader.putTo(llPayload);    // octets 0-3
                commonHeader.putTo(llPayload);   // octets 4-11
                positionVector.putTo(llPayload); // octets 12-35

                sendToLowerLayer(llPayload);
                break;
            case GEOUNICAST:
            case LOCATION_SERVICE_REPLY:
            case LOCATION_SERVICE_REQUEST:
            case MULTI_HOP:  // Topologically Scoped Broadcast (TSB)
            case ANY:
            default:
                // Ignore for now.
                logger.info("Ignored sending {}", data.destination.typeAndSubtype().toString());
                break;
        }
    }

    /** Interface to lower layer (Ethernet/ITS-G5/802.11p, Link Layer) */
    private void onReceiveFromLowerLayer(byte[] payload) throws InterruptedException {
        logger.debug("GN Received payload of size {}", payload.length);
        // Do we want to clone payload before we start reading from it?
        ByteBuffer buffer = ByteBuffer.wrap(payload).asReadOnlyBuffer();  // I promise not to write.
        try {
            byte[] llDstAddress = new byte[6];  // Initialized to 0.
            byte[] llSrcAddress = new byte[6];
            if (linkLayer.hasEthernetHeader()) {
                buffer.get(llDstAddress);  // Should be either me or broadcast, but we don't check.
                                           // If net interface is in promiscuous mode, so are we!
                buffer.get(llSrcAddress);
                short ethertype = buffer.getShort();
                if (ethertype != GN_ETHER_TYPE) {
                    logger.warn("Ethertype is not Geonetworking (no filtering in LinkLayer?)");
                    return;
                }
            }

            BasicHeader  basicHeader  = BasicHeader.getFrom(buffer);
            if (basicHeader.version() != config.itsGnProtocolVersion) {
                logger.warn("Unrecognized protocol version: " + basicHeader.version());
                return;
            }
            CommonHeader commonHeader = CommonHeader.getFrom(buffer);

            switch (commonHeader.typeAndSubtype()) {
                case SINGLE_HOP: {
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    @SuppressWarnings("unused")
                    int reserved = buffer.getInt();  // 32 bit media-dependent info.
                    byte[] upperPayload = new byte[commonHeader.payloadLength()];
                    buffer.slice().get(upperPayload, 0, commonHeader.payloadLength());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            Destination.singleHop().withMaxLifetimeSeconds(
                                    basicHeader.lifetime().asSeconds()),
                            Optional.of(commonHeader.trafficClass()),
                            Optional.of(senderLpv),
                            upperPayload
                            );
                    sendToUpperLayer(indication);
                    // If there is no Ethernet header, llSrcAddress is all 0.
                    locationTable.updateFromDirectMessage(senderLpv.address().get(),
                            MacAddress.fromBytes(llSrcAddress), senderLpv);
                    break;
                }
                case GEOBROADCAST_CIRCLE:
                case GEOBROADCAST_ELLIPSE:
                case GEOBROADCAST_RECTANGLE:
                {
                    short sequenceNumber = buffer.getShort();
                    buffer.getShort();  // Reserved 16-bit.
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    Area area = Area.getFrom(buffer, Area.Type.fromCode(commonHeader.typeAndSubtype().subtype()));
                    buffer.getShort();  // Reserved 16-bit.
                    byte[] upperPayload = new byte[commonHeader.payloadLength()];
                    buffer.slice().get(upperPayload, 0, commonHeader.payloadLength());

                    Destination.Geobroadcast destination = Destination.geobroadcast(area)
                            .withMaxLifetimeSeconds(basicHeader.lifetime().asSeconds())
                            .withRemainingHopLimit((basicHeader.remainingHopLimit()))
                            .withMaxHopLimit(commonHeader.maximumHopLimit());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            destination,
                            Optional.of(commonHeader.trafficClass()),
                            Optional.of(senderLpv),
                            upperPayload
                            );
                    if (area.contains(position()) && !isDuplicate(senderLpv, sequenceNumber)) {
                        sendToUpperLayer(indication);
                    }
                    locationTable.updateFromForwardedMessage(senderLpv.address().get(), senderLpv);
                    //forwardIfNecessary(indication, sequenceNumber, MacAddress.fromBytes(llSrcAddress));
                    break;
                }
                case BEACON:
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    locationTable.updateFromDirectMessage(senderLpv.address().get(),
                            MacAddress.fromBytes(llSrcAddress), senderLpv);
                    break;
                case LOCATION_SERVICE_REQUEST:
                case LOCATION_SERVICE_REPLY:
                    // Do nothing.
                    // At the moment we don't maintain Location Table.
                    logger.info("Ignoring Location Service {}",
                            commonHeader.typeAndSubtype().toString());
                    break;
                case GEOANYCAST_CIRCLE:
                case GEOANYCAST_ELLIPSE:
                case GEOANYCAST_RECTANGLE:
                case GEOUNICAST:
                case MULTI_HOP:
                case ANY:
                default:
                    // Ignore for now.
                    logger.info("Ignoring {}", commonHeader.typeAndSubtype().toString());
                    break;
            }
        } catch (BufferUnderflowException | IllegalArgumentException ex) {
            logger.warn("Can't parse the packet, ignoring.", ex);
        }
    }

    // Do we need concurrent?
    private final Map<FingerprintedPacket, ScheduledFuture<?>> contentionSet = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cbfScheduler =
            Executors.newSingleThreadScheduledExecutor();

    // This method duplicates to a large extent send() - should be refactored.
    private void sendForwardedPacket(FingerprintedPacket fpPacket, Instant timeAdded, MacAddress dstMac) {
        if (!linkLayer.hasEthernetHeader()) { return; }  // Forwarding does not work without MAC.
        double queuingTimeInSeconds = Duration.between(timeAdded, Instant.now()).toMillis() * 0.001;
        double newLifetime = fpPacket.indication.destination.maxLifetimeSeconds().get() - queuingTimeInSeconds;
        byte newHops = (byte)(fpPacket.indication.destination.remainingHopLimit().get() - 1);
        if (newLifetime <= 0 || newHops < 2) { return; }
        Geobroadcast destination = ((Geobroadcast) fpPacket.indication.destination)
                .withMaxHopLimit(newHops).withMaxLifetimeSeconds(newLifetime);
        GeonetData newData = new GeonetData(fpPacket.indication.protocol, destination,
                fpPacket.indication.trafficClass, fpPacket.indication.sender,
                fpPacket.indication.payload);
        ByteBuffer llPayload = ByteBuffer.allocate(ETHER_HEADER_LENGTH  + 56 +
                fpPacket.indication.payload.length);
        llPayload.put(dstMac.asBytes());
        llPayload.put(senderMac.asBytes());
        llPayload.putShort(GN_ETHER_TYPE);
        basicHeader(newData).putTo(llPayload);               // Octets  0- 3
        commonHeader(newData).putTo(llPayload);              // Octets  4-11
        llPayload.putShort((short)fpPacket.sequenceNumber);       // Octets 12-13
        llPayload.putShort((short)0);  // Reserved. // Octets 14-15
        fpPacket.indication.sender.get().putTo(llPayload);            // Octets 16-39
        destination.area().putTo(llPayload);  // Octets 40-53
        llPayload.putShort((short)0);  // Reserved. // Octets 54-55
        llPayload.put(fpPacket.indication.payload);

        try {
            sendToLowerLayer(llPayload);
        } catch (IOException e) {
            logger.error("Exception in sending forwarded packet", e);
        }

    }

    private void forwardIfNecessary(GeonetData indication, int sequenceNumber, MacAddress lastForwarderMac) {
        // We can't forward if we don't know who was the last forwarder.
        // Packets have only the original sender, so for the last forwarder we need MAC address from LL.
        if (!linkLayer.hasEthernetHeader()) { return; }

        // TODO: use duplicate packet detection for contentionSet key!
        // Packet can arrive from another forwarder, but will have the same sequence number,
        // the same source position vector (even with timestamp), but different remaining hop limit
        // different lifetime, different source MAC.
        // Also, if there is a newer packet from the same original source, then the old packet
        // can be safely dropped -- FingerprintedPacket can't handle it.
        final FingerprintedPacket fpPacket = new FingerprintedPacket(indication, sequenceNumber);
        if (contentionSet.containsKey(fpPacket)) {
            contentionSet.get(fpPacket).cancel(false);
            contentionSet.remove(fpPacket);
            return;
        }

        if(indication.destination.remainingHopLimit().orElse((byte) 1) < 2) { return; }

        if (((Geobroadcast)indication.destination).area().contains(position())) {
            long   maxTimeout  = config.itsGnGeoBroadcastCbfMaxTime;        // In milliseconds.
            long   minTimeout  = config.itsGnGeoBroadcastCbfMinTime;        // In milliseconds.
            double maxDistance = config.itsGnDefaultMaxCommunicationRange;  // In meters.


            LongPositionVector lpv = locationTable.getPosition(lastForwarderMac);
            if (lpv == null) { return; }  // Message was forwarded by someone who never sent beacon or SHB.

            double lastDistance = position().distanceInMetersTo(lpv.position());

            // If distance is 0, then timeout is maxTimeout.
            // If distance is maxDistance or more, then timeout is minTimeout.
            // If distance is between 0 and maxDistance, then timeout is linear interpolation.
            long timeoutMillis = lastDistance > maxDistance ? minTimeout :
                    (long) (maxTimeout - (maxTimeout - minTimeout) * (lastDistance / maxDistance));

            final Instant schedulingInstant = Instant.now();

            ScheduledFuture<?> sendingFuture = cbfScheduler.schedule(
                    new Runnable() { @Override public void run() {
                        sendForwardedPacket(fpPacket, schedulingInstant, BROADCAST_MAC); }; },
                    timeoutMillis, TimeUnit.MILLISECONDS);

            contentionSet.put(fpPacket, sendingFuture);

        } else {
            // Greedy Line Forwarding.
            Optional<MacAddress> dstMac = locationTable.closerThanMeTo(((Geobroadcast)indication.destination).area().center(),
                    position());
            if (dstMac.isPresent()) {
                sendForwardedPacket(fpPacket, Instant.now(), dstMac.get());
            } else {
                // We don't know where to forward it. Either store it or just drop.
                return;
            }
        }
    }

    public static class FingerprintedPacket {
        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((address() == null) ? 0 : address().hashCode());
            result = prime * result + sequenceNumber;
            result = prime * result + ((timestamp() == null) ? 0 : timestamp().hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            FingerprintedPacket other = (FingerprintedPacket) obj;
            if (address() == null) {
                if (other.address() != null) return false;
            } else if (!address().equals(other.address())) return false;
            if (sequenceNumber != other.sequenceNumber) return false;
            if (timestamp() == null) {
                if (other.timestamp() != null) return false;
            } else if (!timestamp().equals(other.timestamp())) return false;
            return true;
        }
        private final int sequenceNumber;
        private final GeonetData indication;

        private Address address() { return indication.sender.get().address().get(); }
        private Instant timestamp() { return indication.sender.get().timestamp(); }

        public FingerprintedPacket(GeonetData indication, int sequenceNumber) {
            this.indication = indication;
            this.sequenceNumber = sequenceNumber;
        }
    }


    /** Returns the position of this station, from {@link #positionProvider}. */
    public Position position() {
        return positionProvider.getLatestPosition().position();
    }

    /** Interface to lower layer (Ethernet/ITS-G5/802.11p, Link Layer) */
    private void sendToLowerLayer(ByteBuffer payload) throws IOException {
        linkLayer.send(payload.array());
    }

    /** Private interface to send to upper layer (BTP, Transport Layer).
     *
     * This function serves as a 'single point of contact' for GeonetStation functions that need to
     * send data to upper layer.
     *
     * Provides an ability to refactor queue solution to a reactive-style solution later.
     * */
    private void sendToUpperLayer(GeonetData indication) throws InterruptedException {
        queueUpward.put(indication);
        for (GeonetDataListener l : listeners) {
            l.onGeonetDataReceived(indication);
        }
    }

    /** Returns the next Geonetworking Packet addressed to this station.
     * This operation blocks until the next packet is received.
     * Service packets (Beacon, Location Service, forwarded packets) are omitted here.
     *
     * This is an interface to upper layer (BTP, Transport Layer). */
    public GeonetData receive() throws InterruptedException {
        return queueUpward.take();
    }

    @Override
    public void run() {
        while (true) {
            byte[] payload = new byte[] {};
            try {
                payload = linkLayer.receive();
            } catch (IOException e) {
                logger.error("Geonetworking station got an IO exception, shutting down", e);
                break;
            } catch (InterruptedException e) {
                logger.error("Geonetworking station got an InterruptedException in LinkLayer receive, shutting down", e);
                break;
            }
            try {
                onReceiveFromLowerLayer(payload);
            } catch (InterruptedException e) {
                logger.error("Geonetworking station got an InterruptedException in handling the received message, shutting down", e);
                break;
            }
        }
    }

    @Override
    public void close() {
        beaconService.stop();
        try {
            linkLayer.close();
        } catch (Exception e) {
            logger.error("Exception in LinkLayer close()", e);
        }
    }

    private Map<Address, Instant> lastSeenTimestamp = new HashMap<>();
    private Map<Address, Short> lastSeenSequence  = new HashMap<>();

    /**
     * Indicates if a packet is either a duplicate or a predecessor of another packet.
     *
     * The method first considers timestamps. If the arrived timestamp is before the last seen
     * timestamp from the same address, the packet is a duplicate.
     *
     * Since timestamps has a resolution of one millisecond, there might be several packets sent
     * within one millisecond all with the same timestamp. In that case, sequence numbers are
     * compared.
     *
     *
     * Packet seen first time - ok
     * Packet time later than last seen - ok
     * Packet time equals to last seen - check sequence number
     *     number never seen - ok
     *     number greater than last seen - ok
     *     number equal or less - drop
     * Packet time earlier than last seen - drop
     *
     * Drop:
     *     - packet time earlier than last seen
     *     - packet time equal to last seen AND number equal or less than last seen
     * Otherwise ok
     *
     * If we don't want to remove predecessor condition:
     *  - replace lastSeenTimestamp and lastSeenSequence with last-seen-set
     *  - use as a key in the last-seen-set: (GN address + seq number + timestamp)
     *  - keep entries in last-seen-set for 105 minutes (time equal to BasicHeader.Lifetime.MaximumLifetime)
     *
     * Without timestamp, seq number wraps at 65535. Max lifetime is 105 minutes.
     * The rate needed to send 65535 packets in 105 minutes is about 1 packet in 95 milliseconds.
     * If there will be broadcasts or unicasts faster than that, seq number alone will not be enough.
     *
     */
    private boolean isDuplicate(LongPositionVector lpv, short sequenceNumber) {
        Instant lastTs = lastSeenTimestamp.get(lpv.address().get());  // null if element not found.
        if (lastTs != null &&
                (lpv.timestamp().isBefore(lastTs) ||
                    (lpv.timestamp().equals(lastTs) &&
                       !isAfterInSequence(sequenceNumber, lastSeenSequence.get(lpv.address().get()))
                    )
                 )
           ) {
            logger.debug("DUPLICATE DETECTED");
            return true;
        } else {
            lastSeenTimestamp.put(lpv.address().get(), lpv.timestamp());
            lastSeenSequence.put(lpv.address().get(), sequenceNumber);  // Auto-boxing.
            return false;
        }
    }

    /** Indicates if arrived sequence number is after the stored sequence number.
     *
     * Sequence numbers are unsigned 16-bit integers. Java stores 16-bit integers are signed short.
     * Before comparison, 'short' sequence numbers are converted to positive 'int'.
     *
     * Since sequence numbers are mod 2^16, comparison moves all negative differences forward.
     *
     * The positive difference should be less than one half of the range.
     *
     * */
    private boolean isAfterInSequence(short arrived, Short stored) {
        if (stored == null) {
            return true;  // Never seen - must be after.
        }
        int diff = (arrived & 0xffff) - (stored.shortValue() & 0xffff);
        if (diff < 0) { diff += 1 << 16; }
        return diff < (1 << 15);
    }

    /** Duplicate package detection based only on timestamp.
     *
     * Used for Single Hop Broadcast and Beacon, but those messages are never sent as duplicates...
     */
    @SuppressWarnings("unused")  // There is no valid use-case yet.
    private boolean isDuplicate(LongPositionVector lpv) {
        if (lastSeenTimestamp.containsKey(lpv.address().get()) &&
                ! lpv.timestamp().isAfter(lastSeenTimestamp.get(lpv.address().get()))) {
            return true;
        } else {
            lastSeenTimestamp.put(lpv.address().get(), lpv.timestamp());
            return false;
        }
    }

    private interface BeaconService {
        void start();
        void stop();
        void skipNextBeacon();
    }

    private final BeaconService beaconService = new BeaconService () {
        private final ScheduledExecutorService beaconScheduler =
                Executors.newSingleThreadScheduledExecutor();

        private ScheduledFuture<?> beaconFuture;

        @Override public void start() { scheduleNextBeacon(); }

        @Override public void stop()  { beaconScheduler.shutdownNow(); }

        @Override public void skipNextBeacon() { beaconFuture.cancel(false); scheduleNextBeacon(); }

        private GeonetData beaconData() {
            Optional<TrafficClass> emptyTrafficClass = Optional.empty();    // #send will use default.
            Optional<LongPositionVector> emptyPosition = Optional.empty();  // #send fills in current.
            return new GeonetData(UpperProtocolType.ANY, Destination.beacon(),
                    emptyTrafficClass, emptyPosition, new byte[] {});
        }

        private void sendBeacon() {
            try {
                logger.info("Sending beacon");
                send(beaconData());
            } catch (IOException e) {
                logger.error("Exception in sending beacon", e);
            }
        }

        private void scheduleNextBeacon() {
            beaconFuture = beaconScheduler.schedule(
                new Runnable() { @Override public void run() {
                    sendBeacon(); scheduleNextBeacon(); }; },
                config.itsGnBeaconServiceRetransmitTimer +
                    new Random().nextInt(config.itsGnBeaconServiceMaxJitter),
                TimeUnit.MILLISECONDS);
        }
    };

    public void startBecon() {
        logger.info("Starting BEACON service");
        beaconService.start();
    }

}
