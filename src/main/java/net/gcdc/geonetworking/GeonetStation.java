package net.gcdc.geonetworking;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;


/* Java and ETSI both use Big Endian. */
public class GeonetStation implements Runnable, AutoCloseable {

    private StationConfig                         config;
    private LinkLayer                             linkLayer;
    private PositionProvider                      positionProvider;
    private final LinkedBlockingQueue<GeonetData> queueUpward = new LinkedBlockingQueue<>();
    private final Collection<GeonetDataListener>  listeners = new ArrayList<>();
    private int                                   lastUsedSequenceNumber = 0;

    private short GN_ETHER_TYPE = (short) 0x8947;
    private byte[] BROADCAST_MAC = new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
    private byte[] EMPTY_MAC = new byte[6];  // Initialized to all 0.
    private int ETHER_HEADER_LENGTH = 14;


    public GeonetStation(StationConfig config, LinkLayer linkLayer, PositionProvider positionProvider) {
        this.config = config;
        this.linkLayer = linkLayer;
        this.positionProvider = positionProvider;
    }

    private short sequenceNumber() {
        lastUsedSequenceNumber++;
        if (lastUsedSequenceNumber > (1 << 16) - 1) { lastUsedSequenceNumber = 0; }
        return (short) lastUsedSequenceNumber;
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
                    llPayload.put(BROADCAST_MAC);
                    llPayload.put(EMPTY_MAC);
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
                    llPayload.put(BROADCAST_MAC);  // It is possible to look up Location Table too.
                    llPayload.put(EMPTY_MAC);
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
                ByteBuffer llPayload = ByteBuffer.allocate(
                        (linkLayer.hasEthernetHeader() ? ETHER_HEADER_LENGTH : 0) +
                        36);
                if (linkLayer.hasEthernetHeader()) {
                    llPayload.put(BROADCAST_MAC);
                    llPayload.put(EMPTY_MAC);
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
                break;
        }
    }

    /** Interface to lower layer (Ethernet/ITS-G5/802.11p, Link Layer) */
    private void onReceiveFromLowerLayer(byte[] payload) throws InterruptedException {
        // Do we want to clone payload before we start reading from it?
        ByteBuffer buffer = ByteBuffer.wrap(payload).asReadOnlyBuffer();  // I promise not to write.
        try {
            byte[] llDstAddress = new byte[6];  // Initialized to 0.
            byte[] llSrcAddress = new byte[6];
            if (linkLayer.hasEthernetHeader()) {
                buffer.get(llDstAddress);  // Should be either me or broadcast, but we don't check.
                buffer.get(llSrcAddress);
                short ethertype = buffer.getShort();
                if (ethertype != GN_ETHER_TYPE) {
                    throw new IllegalArgumentException("Ethertype is not Geonetworking");
                }
            }

            BasicHeader  basicHeader  = BasicHeader.getFrom(buffer);
            if (basicHeader.version() != config.itsGnProtocolVersion) {
                throw new IllegalArgumentException("Unrecognized protocol version: " +
                        basicHeader.version());
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
                    forwardIfNecessary(payload, destination);
                    break;
                }
                case BEACON:
                case LOCATION_SERVICE_REQUEST:
                case LOCATION_SERVICE_REPLY:
                    // Do nothing.
                    // At the moment we don't maintain Location Table.
                    break;
                case GEOANYCAST_CIRCLE:
                case GEOANYCAST_ELLIPSE:
                case GEOANYCAST_RECTANGLE:
                case GEOUNICAST:
                case MULTI_HOP:
                case ANY:
                default:
                    // Ignore for now.
                    break;
            }
        } catch (BufferUnderflowException | IllegalArgumentException ex) {
            Logger.getGlobal().log(Level.WARNING, "Can't parse the packet, ignoring.");
        }
    }

    private final Map<byte[], ScheduledFuture<?>> contentionSet = new HashMap<>();
    private final ScheduledExecutorService cbfScheduler =
            Executors.newSingleThreadScheduledExecutor();

    private void sendForwardedPacket(byte[] payload, Instant timeAdded) {
        if (!linkLayer.hasEthernetHeader()) { return; }  // Forwarding does not work without MAC.
        ByteBuffer buffer = ByteBuffer.wrap(payload.clone());  // We overwrite the old payload.
        byte[] llDstMac = new byte[6];
        byte[] llSrcMac = new byte[6];
        buffer.get(llDstMac);
        buffer.get(llSrcMac);
        short ethertype = buffer.getShort();
        if (ethertype != GN_ETHER_TYPE) { return; }  // Ignore the packet.
        BasicHeader basicHeader = BasicHeader.getFrom(buffer);
        double queuingTimeInSeconds = Duration.between(timeAdded, Instant.now()).toMillis() * 0.001;
        double newLifetime = basicHeader.lifetime().asSeconds() - queuingTimeInSeconds;
        byte newHops = (byte)(basicHeader.remainingHopLimit() - 1);
        if (newLifetime <= 0 || newHops < 2) { return; }
        BasicHeader newBasicHeader = new BasicHeader(basicHeader.version(),
                basicHeader.nextHeader(), BasicHeader.Lifetime.fromSeconds(newLifetime), newHops);
        buffer.rewind();
        buffer.put(BROADCAST_MAC);  // Destination. TODO: add lookup for greedy and line forwarding.
        buffer.put(EMPTY_MAC);      // Source.
        buffer.putShort(ethertype);
        newBasicHeader.putTo(buffer);
        try {
            sendToLowerLayer(buffer);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception in sending forwarded packet", e);
            e.printStackTrace();
        }

    }

    private void forwardIfNecessary(final byte[] payload, Destination.Geobroadcast gbc) {
        // We can't forward if we don't know who was the last forwarder.
        // Packets have only the original sender, so for the last forwarder we need MAC address from LL.
        if (!linkLayer.hasEthernetHeader()) { return; }

        if (contentionSet.containsKey(payload)) {
            contentionSet.get(payload).cancel(false);
            contentionSet.remove(payload);
            return;
        }

        if(gbc.remainingHopLimit().orElse((byte) 1) < 2) { return; }

        if (gbc.area().contains(position())) {
            long   maxTimeout  = config.itsGnGeoBroadcastCbfMaxTime;        // In milliseconds.
            long   minTimeout  = config.itsGnGeoBroadcastCbfMinTime;        // In milliseconds.
            double maxDistance = config.itsGnDefaultMaxCommunicationRange;  // In meters.

            // Contention-based forwarding algorithm needs distance to the last forwarder.
            // TODO: Look up position of the last forwarder in Location Table using LL-address.
            double lastDistance = new Random().nextInt(config.itsGnDefaultMaxCommunicationRange);

            // If distance is 0, then timeout is maxTimeout.
            // If distance is maxDistance or more, then timeout is minTimeout.
            // If distance is between 0 and maxDistance, then timeout is linear interpolation.
            long timeoutMillis = lastDistance > maxDistance ? minTimeout :
                    (long) (maxTimeout - (maxTimeout - minTimeout) * (lastDistance / maxDistance));

            final Instant timeAdded = Instant.now();

            ScheduledFuture<?> sendingFuture = cbfScheduler.schedule(
                    new Runnable() { @Override public void run() {
                        sendForwardedPacket(payload, timeAdded); }; },
                    timeoutMillis, TimeUnit.MILLISECONDS);

            contentionSet.put(payload, sendingFuture);

        } else {
            // TODO: Greedy Line Forwarding (requires Location Table).
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
            byte[] payload;
            try {
                payload = linkLayer.receive();
                onReceiveFromLowerLayer(payload);
            } catch (IOException | InterruptedException e) {
                System.err.println("Geonetworking station got an IO exception, but it will try to continue its operation.");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        beaconService.stop();
        try {
            linkLayer.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception in LinkLayer close()", e);
            e.printStackTrace();
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
                send(beaconData());
            } catch (IOException e) {
                Logger.getGlobal().log(Level.SEVERE, "Exception in sending beacon", e);
                e.printStackTrace();
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
        beaconService.start();
    }

}
