package net.gcdc.geonetworking;


public abstract class Destination {

    public abstract DestinationType  typeAndSubtype();
    public abstract Optional<Double> maxLifetimeSeconds();
    public abstract Optional<Byte>   maxHopLimit();
    public abstract Optional<Byte>   remainingHopLimit();

    public static final class SingleHop extends Destination {
        private final Optional<Double> maxLifetimeSeconds;

        private SingleHop(Optional<Double> maxLifetimeSeconds) {
            this.maxLifetimeSeconds = maxLifetimeSeconds;
        }

        @Override public DestinationType  typeAndSubtype()     { return DestinationType.SINGLE_HOP; }
        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds;    }
        @Override public Optional<Byte>   maxHopLimit()        { return Optional.of((byte) 1); }
        @Override public Optional<Byte>   remainingHopLimit()  { return Optional.of((byte) 1); }

        public SingleHop withMaxLifetimeSeconds(double lifetimeSeconds) {
            return new SingleHop(Optional.of(lifetimeSeconds));
        }

        @Override public int hashCode() {
            return 31 + ((maxLifetimeSeconds == null) ? 0 : maxLifetimeSeconds.hashCode());
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            SingleHop other = (SingleHop) obj;
            if (maxLifetimeSeconds == null) {
                if (other.maxLifetimeSeconds != null) return false;
            } else if (!maxLifetimeSeconds.equals(other.maxLifetimeSeconds))
                return false;
            return true;
        }

    }

    public static final class Geobroadcast extends Destination {
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((area == null) ? 0 : area.hashCode());
            result = prime * result + (isAnycast ? 1231 : 1237);
            result = prime * result + ((maxHopLimit == null) ? 0 : maxHopLimit.hashCode());
            result = prime * result
                    + ((maxLifetimeSeconds == null) ? 0 : maxLifetimeSeconds.hashCode());
            result = prime * result
                    + ((remainingHopLimit == null) ? 0 : remainingHopLimit.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Geobroadcast other = (Geobroadcast) obj;
            if (area == null) {
                if (other.area != null)
                    return false;
            } else if (!area.equals(other.area))
                return false;
            if (isAnycast != other.isAnycast)
                return false;
            if (maxHopLimit == null) {
                if (other.maxHopLimit != null)
                    return false;
            } else if (!maxHopLimit.equals(other.maxHopLimit))
                return false;
            if (maxLifetimeSeconds == null) {
                if (other.maxLifetimeSeconds != null)
                    return false;
            } else if (!maxLifetimeSeconds.equals(other.maxLifetimeSeconds))
                return false;
            if (remainingHopLimit == null) {
                if (other.remainingHopLimit != null)
                    return false;
            } else if (!remainingHopLimit.equals(other.remainingHopLimit))
                return false;
            return true;
        }
        final private Area              area;
        final private Optional<Double>  maxLifetimeSeconds;
        final private Optional<Byte>    maxHopLimit;
        final private Optional<Byte>    remainingHopLimit;
        final private boolean           isAnycast;

        private Geobroadcast (
                Area              area,
                Optional<Double>  maxLifetimeSeconds,
                Optional<Byte>    maxHopLimit,
                Optional<Byte>    remainingHopLimit,
                boolean           isAnycast
                ) {
            this.area               = area;
            this.maxLifetimeSeconds = maxLifetimeSeconds;
            this.maxHopLimit        = maxHopLimit;
            this.remainingHopLimit  = remainingHopLimit;
            this.isAnycast          = isAnycast;
        }

        public Area area() { return area; }

        @Override
        public DestinationType typeAndSubtype() {
            if (isAnycast) {
                switch (area.type()) {
                    case CIRCLE:    return DestinationType.GEOANYCAST_CIRCLE;
                    case RECTANGLE: return DestinationType.GEOANYCAST_RECTANGLE;
                    case ELLIPSE:   return DestinationType.GEOANYCAST_ELLIPSE;
                    default:        return DestinationType.ANY;
                }
            } else {
                switch (area.type()) {
                    case CIRCLE:    return DestinationType.GEOBROADCAST_CIRCLE;
                    case RECTANGLE: return DestinationType.GEOBROADCAST_RECTANGLE;
                    case ELLIPSE:   return DestinationType.GEOBROADCAST_ELLIPSE;
                    default:        return DestinationType.ANY;
                }
            }
        }

        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds; }
        @Override public Optional<Byte>   maxHopLimit()        { return maxHopLimit;        }
        @Override public Optional<Byte>   remainingHopLimit()  { return remainingHopLimit;  }

        public Geobroadcast withMaxLifetimeSeconds(double lifetimeSeconds) {
            return new Geobroadcast(
                this.area,
                Optional.of(lifetimeSeconds),
                this.maxHopLimit,
                this.remainingHopLimit,
                this.isAnycast
            );
        }
        public Geobroadcast withMaxHopLimit(byte maxHopLimit) {
            return new Geobroadcast(
                this.area,
                this.maxLifetimeSeconds,
                Optional.of(maxHopLimit),
                this.remainingHopLimit,
                this.isAnycast
            );
        }
        public Geobroadcast withRemainingHopLimit(byte remainingHopLimit) {
            return new Geobroadcast(
                this.area,
                this.maxLifetimeSeconds,
                this.maxHopLimit,
                Optional.of(remainingHopLimit),
                this.isAnycast
            );
        }
    }

    public static final class Beacon extends Destination {
        private Beacon() {}
        @Override public DestinationType  typeAndSubtype()     { return DestinationType.BEACON; }
        @Override public Optional<Double> maxLifetimeSeconds() { return Optional.empty();       }
        @Override public Optional<Byte>   maxHopLimit()        { return Optional.of((byte) 1);  }
        @Override public Optional<Byte>   remainingHopLimit()  { return Optional.of((byte) 1);  }
    }

    public static final class TopoScopedBroadcast extends Destination {
        private final Optional<Double> maxLifetimeSeconds;
        private final Optional<Byte> maxHopLimit;
        private final Optional<Byte> remainingHopLimit;

        private TopoScopedBroadcast(Optional<Double> maxLifetimeSeconds, Optional<Byte> maxHopLimit, Optional<Byte> remainingHopLimit) {
            this.maxLifetimeSeconds = maxLifetimeSeconds;
            this.maxHopLimit = maxHopLimit;
            this.remainingHopLimit = remainingHopLimit;
        }

        @Override public DestinationType  typeAndSubtype()     { return DestinationType.MULTI_HOP; }
        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds; }
        @Override public Optional<Byte>   maxHopLimit()        { return maxHopLimit; }
        @Override public Optional<Byte>   remainingHopLimit()  { return remainingHopLimit; }

        public TopoScopedBroadcast withMaxLifetimeSeconds(double lifetimeSeconds) {
            return new TopoScopedBroadcast(
                Optional.of(lifetimeSeconds),
                this.maxHopLimit,
                this.remainingHopLimit
            );
        }
        public TopoScopedBroadcast withMaxHopLimit(byte maxHopLimit) {
            return new TopoScopedBroadcast(
                this.maxLifetimeSeconds,
                Optional.of(maxHopLimit),
                this.remainingHopLimit
            );
        }
        public TopoScopedBroadcast withRemainingHopLimit(byte remainingHopLimit) {
            return new TopoScopedBroadcast(
                this.maxLifetimeSeconds,
                this.maxHopLimit,
                Optional.of(remainingHopLimit)
            );
        }
    }

    public static SingleHop singleHop() {
        Optional<Double> lifetime = Optional.empty();
        return new SingleHop(lifetime);
    }

    public static Geobroadcast geobroadcast(Area area) {
        Optional<Double> emptyLifetime = Optional.empty();
        Optional<Byte>   emptyHops     = Optional.empty();
        boolean isAnycast = false;
        return new Geobroadcast(area, emptyLifetime, emptyHops, emptyHops, isAnycast);
    }

    public static Geobroadcast geoanycast(Area area) {
        Optional<Double> emptyLifetime = Optional.empty();
        Optional<Byte>   emptyHops     = Optional.empty();
        boolean isAnycast = true;
        return new Geobroadcast(area, emptyLifetime, emptyHops, emptyHops, isAnycast);
    }

    public static Beacon beacon() { return new Beacon(); }

    public static TopoScopedBroadcast toposcopedbroadcast() {
        Optional<Double> emptyLifetime = Optional.empty();
        Optional<Byte>   emptyHops     = Optional.empty();
        return new TopoScopedBroadcast(emptyLifetime, emptyHops, emptyHops);
    }

    // public static GeoUnicast geounicast(Address address) { }

}
