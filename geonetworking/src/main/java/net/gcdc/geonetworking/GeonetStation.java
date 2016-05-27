package net.gcdc.geonetworking;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

    // Common scheduler for beacon, Duplicate packet detection and Contention-based forwarding.
    // Change to custom-clock scheduler for non-real-time time. (Dependency injection?)
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private boolean isPromiscuous = true;

    // Common function to get the current time.
    private Instant timeInstantNow() {
        return Instant.now();  // Add clock here for non-real-time.
    }

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

        GeonetData completeData = data.withSender(Optional.of(positionVector));

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
                MacAddress dstMac = BROADCAST_MAC;
                if (linkLayer.hasEthernetHeader()) {
                    Area area = ((Destination.Geobroadcast)data.destination).area();
                    if (!area.contains(position())) {
                        logger.debug("Area {} don't contains me {}", area, position());
                        Optional<MacAddress> betterDstMac = locationTable.closerThanMeTo(
                            area.center(), position(), new HashSet<MacAddress>());
                        if (betterDstMac.isPresent()) { dstMac = betterDstMac.get(); }
                    }
                }
                ByteBuffer llPayload = packGeobroadcast(dstMac, senderMac, completeData, sequenceNumber());

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
                buffer.get(llDstAddress);
                // Should be either me or broadcast.
                MacAddress dstMac = MacAddress.fromBytes(llDstAddress);
                if (!isPromiscuous  && !dstMac.equals(senderMac) &&
                        !dstMac.equals(BROADCAST_MAC)) {
                    return;
                }
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
                case MULTI_HOP: {  // TODO: test case.
                    short sequenceNumber = buffer.getShort();
                    buffer.getShort();  // Reserved 16-bit.
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    byte[] upperPayload = new byte[commonHeader.payloadLength()];
                    buffer.slice().get(upperPayload, 0, commonHeader.payloadLength());

                    Destination.TopoScopedBroadcast destination = Destination.toposcopedbroadcast()
                            .withMaxLifetimeSeconds(basicHeader.lifetime().asSeconds())
                            .withMaxHopLimit(commonHeader.maximumHopLimit())
                            .withRemainingHopLimit(basicHeader.remainingHopLimit());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            destination,
                            Optional.of(commonHeader.trafficClass()),
                            Optional.of(senderLpv),
                            upperPayload
                            );
                    if (!isDuplicate(indication, sequenceNumber)) {
                        sendToUpperLayer(indication);
                    }
                    locationTable.updateFromForwardedMessage(senderLpv.address().get(), senderLpv);
                    forwardIfNecessary(indication, sequenceNumber, MacAddress.fromBytes(llSrcAddress));
                    markAsSeen(indication, sequenceNumber);  // Duplicate packet detection.

                    break;
                }
                case GEOBROADCAST_CIRCLE:
                case GEOBROADCAST_ELLIPSE:
                case GEOBROADCAST_RECTANGLE:
                case GEOANYCAST_CIRCLE:
                case GEOANYCAST_ELLIPSE:
                case GEOANYCAST_RECTANGLE:
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
                            .withRemainingHopLimit(basicHeader.remainingHopLimit())
                            .withMaxHopLimit(commonHeader.maximumHopLimit());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            destination,
                            Optional.of(commonHeader.trafficClass()),
                            Optional.of(senderLpv),
                            upperPayload
                            );
                    if (area.contains(position()) && !isDuplicate(indication, sequenceNumber)) {
                        sendToUpperLayer(indication);
                    }
                                        
                    locationTable.updateFromForwardedMessage(senderLpv.address().get(), senderLpv);
                    forwardIfNecessary(indication, sequenceNumber, MacAddress.fromBytes(llSrcAddress));
                    markAsSeen(indication, sequenceNumber);  // Duplicate packet detection.
                    break;
                }
                case BEACON: {
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    locationTable.updateFromDirectMessage(senderLpv.address().get(),
                            MacAddress.fromBytes(llSrcAddress), senderLpv);
                    break;
                }
                case LOCATION_SERVICE_REQUEST:
                case LOCATION_SERVICE_REPLY:
                    // Do nothing.
                    // At the moment we don't maintain Location Table.
                    logger.info("Ignoring Location Service {}",
                            commonHeader.typeAndSubtype().toString());
                    break;
                case GEOUNICAST: {
                    short sequenceNumber = buffer.getShort();
                    buffer.getShort();  // Reserved 16-bit.
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    ShortPositionVector destSpv = ShortPositionVector.getFrom(buffer);
                    byte[] upperPayload = new byte[commonHeader.payloadLength()];
                    buffer.slice().get(upperPayload, 0, commonHeader.payloadLength());

                    Destination.GeoUnicast destination = Destination.geounicast(destSpv.address())
                            .withMaxLifetimeSeconds(basicHeader.lifetime().asSeconds())
                            .withRemainingHopLimit(basicHeader.remainingHopLimit())
                            .withMaxHopLimit(commonHeader.maximumHopLimit());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            destination,
                            Optional.of(commonHeader.trafficClass()),
                            Optional.of(senderLpv),
                            upperPayload
                            );
                    final long myMac = (new Address(config.itsGnLoacalGnAddr)).lowLevelAddress();
                    if (destSpv.address().lowLevelAddress() == myMac
                            && !isDuplicate(indication, sequenceNumber)) {
                        sendToUpperLayer(indication);
                    }
                    locationTable.updateFromForwardedMessage(senderLpv.address().get(), senderLpv);
                    //forwardIfNecessary(indication, sequenceNumber, MacAddress.fromBytes(llSrcAddress));
                    markAsSeen(indication, sequenceNumber);  // Duplicate packet detection.
                    break;
                }
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
    private final Map<PacketId, ScheduledFuture<?>> contentionSet = new ConcurrentHashMap<>();

    private void sendForwardedPacket(GeonetData data, int sequenceNumber, Instant timeAdded, MacAddress dstMac) {
        if (!linkLayer.hasEthernetHeader()) { return; }  // Forwarding does not work without MAC.
        double queuingTimeInSeconds = Duration.between(timeAdded, timeInstantNow()).toMillis() * 0.001;
        double newLifetime = data.destination.maxLifetimeSeconds().get() - queuingTimeInSeconds;
        byte newHops = (byte)(data.destination.remainingHopLimit().get() - 1);
        if (newLifetime <= 0 || newHops < 2) { return; }
        Geobroadcast destination = ((Geobroadcast) data.destination)
                .withMaxHopLimit(newHops).withMaxLifetimeSeconds(newLifetime);
        GeonetData newData = data.withDestination(destination);
        ByteBuffer llPayload = packGeobroadcast(dstMac, senderMac, newData, sequenceNumber);

        try {
            sendToLowerLayer(llPayload);
        } catch (IOException e) {
            logger.error("Exception in sending forwarded packet", e);
        }

    }

    private ByteBuffer packGeobroadcast(MacAddress dstMac, MacAddress srcMac, GeonetData data, int sequenceNumber) {
        ByteBuffer llPayload = ByteBuffer.allocate(
            (linkLayer.hasEthernetHeader() ? ETHER_HEADER_LENGTH : 0) + 56 + data.payload.length);

        if (linkLayer.hasEthernetHeader()) {
            llPayload.put(dstMac.asBytes());
            llPayload.put(senderMac.asBytes());
            llPayload.putShort(GN_ETHER_TYPE);
        }
        basicHeader(data).putTo(llPayload);               // Octets  0- 3
        commonHeader(data).putTo(llPayload);              // Octets  4-11
        llPayload.putShort((short)sequenceNumber);        // Octets 12-13
        llPayload.putShort((short)0);        // Reserved. // Octets 14-15
        data.sender.get().putTo(llPayload);               // Octets 16-39
        ((Geobroadcast)data.destination).area().putTo(llPayload);  // Octets 40-53
        llPayload.putShort((short)0);        // Reserved. // Octets 54-55
        llPayload.put(data.payload);
        return llPayload;
    }


    private void contentionBasedForwarding(final GeonetData data, final short sequenceNumber,
            final MacAddress lastForwarderMac) {
        // Packet can arrive from another forwarder, but will have the same sequence number,
        // the same source position vector (even with timestamp), but different remaining hop limit
        // different lifetime, different source MAC.
        final PacketId packetId = new PacketId(data.sender.get().timestamp(), sequenceNumber,
                data.sender.get().address().get());
        if (contentionSet.containsKey(packetId)) {
            contentionSet.get(packetId).cancel(false);
            contentionSet.remove(packetId);
            return;
        }

        if (isDuplicate(data, sequenceNumber)) {
            logger.debug("Packet has been seen, but is not in the contention set. Discarding.");
            return;
        }

        if(data.destination.remainingHopLimit().orElse((byte) 1) < 2) { return; }

        if (((Geobroadcast)data.destination).area().contains(position())) {
            long   maxTimeout  = config.itsGnGeoBroadcastCbfMaxTime;        // In milliseconds.
            long   minTimeout  = config.itsGnGeoBroadcastCbfMinTime;        // In milliseconds.
            double maxDistance = config.itsGnDefaultMaxCommunicationRange;  // In meters.


            Position lastForwarderPosition = locationTable.getPosition(lastForwarderMac);

            // If the message was forwarded by someone who never sent a beacon or SHB, assume that
            // it is out of range and the distance is maxDistance.
            double lastDistance = lastForwarderPosition == null ? maxDistance :
                    position().distanceInMetersTo(lastForwarderPosition);

            // If distance is 0, then timeout is maxTimeout.
            // If distance is maxDistance or more, then timeout is minTimeout.
            // If distance is between 0 and maxDistance, then timeout is linear interpolation.
            long timeoutMillis = lastDistance >= maxDistance ? minTimeout :
                    (long) (maxTimeout - (maxTimeout - minTimeout) * (lastDistance / maxDistance));

            final Instant schedulingInstant = timeInstantNow();

            ScheduledFuture<?> sendingFuture = scheduler.schedule(
                    new Runnable() { @Override public void run() {
                        sendForwardedPacket(data, sequenceNumber, schedulingInstant, BROADCAST_MAC); }; },
                    timeoutMillis, TimeUnit.MILLISECONDS);

            contentionSet.put(packetId, sendingFuture);

        } else {
            greedyForwarding(data, sequenceNumber, lastForwarderMac);
        }
    }

    private void simpleForwarding(GeonetData indication, int sequenceNumber, MacAddress lastForwarderMac) {
        Area destinationArea = ((Geobroadcast)indication.destination).area();
        if (destinationArea.contains(position())) {  // We're inside - broadcast to all!
            sendForwardedPacket(indication, sequenceNumber, timeInstantNow(), BROADCAST_MAC);
        } else {
            Position lastForwarderPosition = locationTable.getPosition(lastForwarderMac);
            if (lastForwarderPosition != null && destinationArea.contains(lastForwarderPosition)) {
                // Last forwarder is already inside, we drop the packet.
            } else {  // Last forwarder is outside (as we are) or never sent SHB or beacon.
                greedyForwarding(indication, sequenceNumber, lastForwarderMac);
            }
        }
    }

    private void greedyForwarding(GeonetData indication, int sequenceNumber, MacAddress lastForwarderMac) {
        Optional<MacAddress> neighborMac = locationTable.closerThanMeTo(
                ((Geobroadcast)indication.destination).area().center(),
                position(), new HashSet<MacAddress>(Arrays.asList(lastForwarderMac)));
        // Fall back to broadcast if no neighbor is fond. Alternative is to buffer and wait.
        MacAddress dstMac = neighborMac.orElse(BROADCAST_MAC);
        sendForwardedPacket(indication, sequenceNumber, timeInstantNow(), dstMac);
    }

    private void forwardIfNecessary(GeonetData indication, short sequenceNumber, MacAddress lastForwarderMac) {
        // We can't forward if we don't know who was the last forwarder.
        // Packets have only the original sender, so for the last forwarder we need MAC address from LL.
        if (!linkLayer.hasEthernetHeader()) { return; }

        // Do not forward if remaining hop limit (RHL) is too low.
        // TODO: is it 1 or 0 which is too low? Beacons are sent with RHL=1, but they are never
        // forwarded. Does this imply that anything with RHL=1 should not be forwarded?
        if (indication.destination.remainingHopLimit().orElse((byte) config.itsGnDefaultHopLimit)
                <= 1) {
            return;
        }

        // Do not forward GeoAnycast if we are one of the recipients.
        Destination.Geobroadcast destination = (Geobroadcast)indication.destination;
        if (destination.isAnycast() && destination.area().contains(position())) {
            return;
        }

        switch (config.itsGnGeoBroadcastForwardingAlgorithm) {
            case 0:
                logger.debug("Fwd alg set to 0 (unspecified), forwarding disabled");
                break;
            case 1:
                if (!isDuplicate(indication, sequenceNumber)) {
                    simpleForwarding(indication, sequenceNumber, lastForwarderMac);
                }
                break;
            case 2:
                contentionBasedForwarding(indication, sequenceNumber, lastForwarderMac);
                break;
            default:
                logger.error("Unsupported forwarding algorithm: {}. Forwarding disabled.",
                        config.itsGnGeoBroadcastForwardingAlgorithm);
                break;
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

    private final Set<PacketId> seenPackets = new HashSet<>();

    private class PacketId {
        private final Instant timestamp;
        private final Short sequenceNumber;
        private final Address sender;

        public PacketId(Instant timestamp, short sequenceNumber, Address sender) {
            this.timestamp = timestamp;
            this.sequenceNumber = sequenceNumber;
            this.sender = sender;
        }

        private GeonetStation getOuterType() {
            return GeonetStation.this;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((sender == null) ? 0 : sender.hashCode());
            result = prime * result + ((sequenceNumber == null) ? 0 : sequenceNumber.hashCode());
            result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PacketId other = (PacketId) obj;
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (sender == null) {
                if (other.sender != null) return false;
            } else if (!sender.equals(other.sender)) return false;
            if (sequenceNumber == null) {
                if (other.sequenceNumber != null) return false;
            } else if (!sequenceNumber.equals(other.sequenceNumber)) return false;
            if (timestamp == null) {
                if (other.timestamp != null) return false;
            } else if (!timestamp.equals(other.timestamp)) return false;
            return true;
        }
    }

    /**
     * Indicates if a packet is a duplicate.
     *
     *  Version 1.2.1 of standard describes actually if a packet is duplicate or a predecessor of
     *  another packet.
     *
     * Standard first considers timestamps. If the arrived timestamp is before the last seen
     * timestamp from the same address, the packet is a duplicate.
     *
     * Since timestamps has a resolution of one millisecond, there might be several packets sent
     * within one millisecond all with the same timestamp. In that case, sequence numbers are
     * compared.
     *
     * Standard's method drops packets if there are two parallel broadcasts from the same sender.
     * E.g. RoadWorks and SlipperyRoad, sent every second. RW is sent, then SR is sent, but SR was
     * forwarded better and is delivered before RW. Then when RW finally arrives later, it is
     * discarded.
     *
     * We don't want to remove predecessors, so:
     *  - use as a key in the last-seen-set (GN address + seq number + timestamp)
     *  - keep entries in last-seen-set for 105 minutes (time equal to BasicHeader.Lifetime.MaximumLifetime)
     *
     * Without timestamp, seq number wraps at 65535. Max lifetime is 105 minutes.
     * The rate needed to send 65535 packets in 105 minutes is about 1 packet in 95 milliseconds.
     * If there will be broadcasts or unicasts faster than that, seq number alone will not be enough,
     * so we add timestamp too. It's just a moderate computation and memory overhead, we'll keep
     * a lot of GN addresses and sequence numbers anyway.
     *
     */
    private boolean isDuplicate(GeonetData data, short sequenceNumber) {
        final PacketId packetId = new PacketId(data.sender.get().timestamp(), sequenceNumber,
                data.sender.get().address().get());
        return seenPackets.contains(packetId);
    }

    private void markAsSeen(GeonetData data, short sequenceNumber) {
        final PacketId packetId = new PacketId(data.sender.get().timestamp(), sequenceNumber,
                data.sender.get().address().get());
        boolean isDuplicate = seenPackets.contains(packetId);
        if (!isDuplicate) {
            seenPackets.add(packetId);
            long lifetimeMillis = (long) (1000 * data.destination.maxLifetimeSeconds().orElse(
                    BasicHeader.Lifetime.MAX_VALUE.asSeconds()));
            scheduler.schedule(
                new Runnable() { @Override public void run() { seenPackets.remove(packetId); } },
                lifetimeMillis, TimeUnit.MILLISECONDS
            );
        } else {
            logger.debug("Duplicate! From {}, sn {}", data.sender.get().address().get(), sequenceNumber);
        }
    }

    private interface BeaconService {
        void start();
        void stop();
        void skipNextBeacon();
    }

    private final BeaconService beaconService = new BeaconService () {

        private Instant nextBeaconTime = timeInstantNow();
        private boolean isActive = false;  // TODO: Handle synchronization properly.

        @Override public void start() { isActive = true; scheduleNextBeacon(); }

        @Override public void stop()  { isActive = false; }

        @Override public void skipNextBeacon() {
            nextBeaconTime = timeInstantNow().plusMillis(randomDelayMs());
        }

        private GeonetData beaconData() {
            Optional<TrafficClass> emptyTrafficClass = Optional.empty();    // #send will use default.
            Optional<LongPositionVector> emptyPosition = Optional.empty();  // #send fills in current.
            return new GeonetData(UpperProtocolType.ANY, Destination.beacon(),
                    emptyTrafficClass, emptyPosition, new byte[] {});
        }

        private void maybeSendBeacon() {
            if (!timeInstantNow().isBefore(nextBeaconTime)) {
                try {
                    logger.info("Sending beacon");
                    send(beaconData());
                } catch (IOException e) {
                    logger.error("Exception in sending beacon", e);
                }
            }
        }

        private void scheduleNextBeacon() {
            scheduler.schedule(
                new Runnable() { @Override public void run() {
                    if (isActive) { maybeSendBeacon(); scheduleNextBeacon(); } }; },
                randomDelayMs(),
                TimeUnit.MILLISECONDS);
        }

        private long randomDelayMs() {
            return config.itsGnBeaconServiceRetransmitTimer +
                    new Random().nextInt(config.itsGnBeaconServiceMaxJitter);
        }
    };

    public void startBecon() {
        logger.info("Starting BEACON service");
        beaconService.start();
    }

}
