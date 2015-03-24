package net.gcdc.geonetworking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

/** Time-expiring double-key map.
 *
 * Entries in the Location Table expire after itsGnLifetimeLocTE.
 * */
public class LocationTable {
    private final static Logger logger = LoggerFactory.getLogger(LocationTable.class);

    private final ConfigProvider configProvider;

    private final Collection<UpdateListener> listeners = new ArrayList<>();

    // Do these maps have to be synchronized? Collections.synchronizedMap or ConcurrentHashMap?
    private final Map<Address, Entry> gnMap = new ConcurrentHashMap<>();
    private final Map<Address, ScheduledFuture<?>> janitorFutures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public LocationTable(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }


    /** Entry in a Location Table.
     *
     * The flag 'locationServicePending' should set to false if not renewed within
     *  3 × itsGnBeaconServiceRetransmitTimer
     */
    public static class Entry {
        private Address address;
        private MacAddress macAddress;
        private LongPositionVector position;
        private boolean locationServicePending;
        private boolean isNeighbour;
        private int sequenceNumber;
        private Instant timestamp;

        public Address address() { return address; }
        public MacAddress macAddress() { return macAddress; }
        public LongPositionVector position() { return position; }
        public boolean locationServicePending() { return locationServicePending; }
        public boolean isNeighbour() { return isNeighbour; }
        public int sequenceNumber() { return sequenceNumber; }
        public Instant timestamp() { return timestamp; }

        public Entry withMacAddress(MacAddress macAddress) { return new Builder(this).macAddress(macAddress).create(); }
        public Entry withPosition(LongPositionVector position) { return new Builder(this).position(position).create(); }
        public Entry withIsNeighbour(boolean isNeighbour) { return new Builder(this).isNeighbour(isNeighbour).create(); }
        public Entry withTimestamp(Instant timestamp) { return new Builder(this).timestamp(timestamp).create(); }

        private Entry() {}

        public Builder builder() { return new Builder(); }

        public static class Builder {
            private final Entry val;
            boolean created = false;
            private Builder() { val = new Entry(); }
            private Builder(Entry entry) {
                val = new Entry();
                val.address = entry.address;
                val.macAddress = entry.macAddress;
                val.position = entry.position;
                val.locationServicePending = entry.locationServicePending;
                val.isNeighbour = entry.isNeighbour;
                val.sequenceNumber = entry.sequenceNumber;
                val.timestamp = entry.timestamp;
            }
            private void checkCreated() {
                if (created) { throw new IllegalStateException("Already created"); }
            }
            public Entry create() { created = true; return val; }

            public Builder address(Address address) { checkCreated(); val.address = address; return this; }
            public Builder macAddress(MacAddress macAddress) { checkCreated(); val.macAddress = macAddress; return this; }
            public Builder position(LongPositionVector position) { checkCreated(); val.position = position; return this; }
            public Builder locationServicePending(boolean locationServicePending) { checkCreated(); val.locationServicePending = locationServicePending; return this; }
            public Builder isNeighbour(boolean isNeighbour) { checkCreated(); val.isNeighbour = isNeighbour; return this; }
            public Builder sequenceNumber(int sequenceNumber) { checkCreated(); val.sequenceNumber = sequenceNumber; return this; }
            public Builder timestamp(Instant timestamp) { checkCreated(); val.timestamp = timestamp; return this; }

        }

        // type: vehicle or rsu
        // GN version
        // packet data rate
    }


    /** Returns LongPositionVector or null. */
    public LongPositionVector getPosition(MacAddress macAddress) {
        for (Entry entry: gnMap.values()) {
            if (entry.macAddress().equals(macAddress)) {
                return entry.position();
            }
        }
        return null;
    }

    // TODO: add a list of nodes-to-not-use (last forwarder)
    public Optional<MacAddress> closerThanMeTo(Position destination, Position me) {
        Entry nearest = null;
        double shortestDistance = me.distanceInMetersTo(destination);
        for (Entry entry: gnMap.values()) {
            final double dist = entry.position().position().distanceInMetersTo(destination);
            if (dist < shortestDistance && entry.macAddress() != null) {
                shortestDistance = dist;
                nearest = entry;
            }
        }
        if (nearest == null) {
            return Optional.empty();
        } else {
            return Optional.of(nearest.macAddress());
        }
    }

    public void updateFromDirectMessage(final Address address, final MacAddress macAddress, final LongPositionVector position) {
        final Entry oldEntry = gnMap.get(address);
        final Entry entry = (oldEntry == null ? new Entry.Builder() : new Entry.Builder(oldEntry))
            .address(address)
            .macAddress(macAddress)
            .position(position)
            .isNeighbour(true)
            .timestamp(Instant.now())
            .create();
        logger.debug("Adding direct neighbour {}", address.toString());
        putAndSchedule(entry);
    }

    private void putAndSchedule(Entry entry) {
        final Address address = entry.address();
        ScheduledFuture<?> oldRemovalFuture = janitorFutures.get(address);
        if (oldRemovalFuture != null) { oldRemovalFuture.cancel(false); }
        janitorFutures.remove(address);
        gnMap.put(address, entry);
        final ScheduledFuture<?> future = executor.schedule(
                new Runnable() { @Override public void run() {
                    gnMap.remove(address);
                    janitorFutures.remove(address);
                    notifyListeners();} },
                configProvider.config().itsGnLifetimeLocTE,
                TimeUnit.SECONDS);
        janitorFutures.put(address, future);
        notifyListeners();
    }

    public void updateFromForwardedMessage(Address address, LongPositionVector position) {
        final Entry oldEntry = gnMap.get(address);
        final Entry entry = (oldEntry == null ? new Entry.Builder() : new Entry.Builder(oldEntry))
            .address(address)
            .position(position)
            .timestamp(Instant.now())
            .create();
        logger.debug("Adding forwarded {}", address.toString());
        putAndSchedule(entry);
    }

    public Collection<LocationTable.Entry> entries() {
        return gnMap.values();
    }

    public static interface UpdateListener {
        void onChange();
    }

    public void addListener(UpdateListener listener) { listeners.add(listener); }
    public void removeListener(UpdateListener listener) { listeners.remove(listener); }

    private void notifyListeners() {
        for (UpdateListener listener : listeners) { listener.onChange(); }
    }
}
