package net.gcdc.geonetworking;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Java and ETSI both use Big Endian. */
public class GeonetStation implements Runnable, AutoCloseable {

    private StationConfig                         config;
    private LinkLayer                             linkLayer;
    private PositionProvider positionProvider;
    private final LinkedBlockingQueue<GeonetData> queueUpward = new LinkedBlockingQueue<>();
    private short lastUsedSequenceNumber = 0;

    public GeonetStation(StationConfig config, LinkLayer linkLayer, PositionProvider positionProvider) {
        this.config = config;
        this.linkLayer = linkLayer;
        this.positionProvider = positionProvider;
    }

    private short sequenceNumber() {
        lastUsedSequenceNumber =
            (short) (lastUsedSequenceNumber >= Short.MAX_VALUE ? 0 : lastUsedSequenceNumber + 1);
        return lastUsedSequenceNumber;
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
                                        // information
                                        // in ITS-G5 (ETSI TS 102 636-4-2).

                ByteBuffer llPayload = ByteBuffer.allocate(40 + data.payload.length);
                basicHeader.putTo(llPayload);    // octets 0-3
                commonHeader.putTo(llPayload);   // octets 4-11
                positionVector.putTo(llPayload); // octets 12-35
                llPayload.putInt(mediaInfo);     // octets 36-39
                llPayload.put(data.payload);

                sendToLowerLayer(llPayload);
                break;
            }
            case GEOBROADCAST_CIRCLE:
            case GEOBROADCAST_ELLIPSE:
            case GEOBROADCAST_RECTANGLE:
            case GEOANYCAST_CIRCLE:
            case GEOANYCAST_ELLIPSE:
            case GEOANYCAST_RECTANGLE: {
                ByteBuffer llPayload = ByteBuffer.allocate(55 + data.payload.length);
                basicHeader.putTo(llPayload);               // Octets  0- 3
                commonHeader.putTo(llPayload);              // Octets  4-11
                llPayload.putShort(sequenceNumber());       // Octets 12-13
                llPayload.putShort((short)0);  // Reserved. // Octets 14-15
                positionVector.putTo(llPayload);            // Octets 16-39
                ((Destination.Geobroadcast)data.destination).area().putTo(llPayload);  // Octets 40-53
                llPayload.putShort((short)0);  // Reserved. // Octets 54-55
                llPayload.put(data.payload);

                // TODO: run forwarding algorithm to determine the next hop.
                sendToLowerLayer(llPayload);
                break;
            }
            case ANY:
            case BEACON:
                ByteBuffer llPayload = ByteBuffer.allocate(36);
                basicHeader.putTo(llPayload);    // octets 0-3
                commonHeader.putTo(llPayload);   // octets 4-11
                positionVector.putTo(llPayload); // octets 12-35

                sendToLowerLayer(llPayload);
                break;
            case GEOUNICAST:
            case LOCATION_SERVICE_REPLY:
            case LOCATION_SERVICE_REQUEST:
            case MULTI_HOP:  // Topologically Scoped Broadcast (TSB)
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
            BasicHeader  basicHeader  = BasicHeader.getFrom(buffer);
            if (basicHeader.version() != config.itsGnProtocolVersion) { return; }  // Ignore packet.
            CommonHeader commonHeader = CommonHeader.getFrom(buffer);

            switch (commonHeader.typeAndSubtype()) {
                case SINGLE_HOP: {
                    LongPositionVector senderLpv = LongPositionVector.getFrom(buffer);
                    int reserved = buffer.getInt();  // 32 bit media-dependent info.
                    byte[] upperPayload = new byte[commonHeader.payloadLength()];
                    buffer.slice().get(upperPayload, 0, commonHeader.payloadLength());
                    GeonetData indication = new GeonetData(
                            commonHeader.nextHeader(),
                            Destination.singleHop().withMaxLifetimeSeconds(basicHeader.lifetime().asSeconds()),
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

                    // TODO: add duplicate packet detection.
                    if (area.contains(position())) {
                        Destination destination = Destination.geobroadcast(area)
                                .withMaxLifetimeSeconds(basicHeader.lifetime().asSeconds())
                                .withRemainingHopLimit((byte)(basicHeader.remainingHopLimit() - 1))
                                .withMaxHopLimit(commonHeader.maximumHopLimit());
                        GeonetData indication = new GeonetData(
                                commonHeader.nextHeader(),
                                destination,
                                Optional.of(commonHeader.trafficClass()),
                                Optional.of(senderLpv),
                                upperPayload
                                );
                        sendToUpperLayer(indication);
                    }
                    // TODO: Forward if necessary.
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
        } catch (BufferUnderflowException ex) {
            Logger.getGlobal().log(Level.WARNING, "Can't parse the packet, ignoring.");
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
    }

    /** Interface to upper layer (BTP, Transport Layer) */
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
        beaconScheduler.shutdownNow();
        try {
            linkLayer.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception in LinkLayer close()", e);
            e.printStackTrace();
        }
    }

    private final ScheduledExecutorService beaconScheduler =
            Executors.newSingleThreadScheduledExecutor();

    private void sendBeacon() {
        Optional<TrafficClass> emptyTrafficClass = Optional.empty();    // #send will use default.
        Optional<LongPositionVector> emptyPosition = Optional.empty();  // #send fills in current.
        GeonetData data = new GeonetData(UpperProtocolType.ANY, Destination.beacon(),
                emptyTrafficClass, emptyPosition, new byte[] {});
        try {
            send(data);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "Exception in sending beacon", e);
            e.printStackTrace();
        }
    }

    public void startBecon() {
        scheduleNextBeacon();
    }

    private void scheduleNextBeacon() {
        final Runnable sendAndScheduleNext = new Runnable() {
            @Override public void run() { sendBeacon(); scheduleNextBeacon(); };
        };

        final long delay = config.itsGnBeaconServiceRetransmitTimer +
                new Random().nextInt(config.itsGnBeaconServiceMaxJitter);

        beaconScheduler.schedule(sendAndScheduleNext, delay, TimeUnit.MILLISECONDS);
    }
}
