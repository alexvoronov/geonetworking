package net.gcdc.geonetworking;


public abstract class Destination {

    public abstract DestinationType  typeAndSubtype();
    public abstract Optional<Double> maxLifetimeSeconds();
    public abstract Optional<Byte>   maxHopLimit();
    public abstract Optional<Byte>   remainingHopLimit();

    public static class SingleHop extends Destination {
        private final Optional<Double> maxLifetimeSeconds;

        private SingleHop(Optional<Double> maxLifetimeSeconds) {
            this.maxLifetimeSeconds = maxLifetimeSeconds;
        }

        @Override public DestinationType  typeAndSubtype()     { return DestinationType.SINGLE_HOP; }
        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds; }
        @Override public Optional<Byte>   maxHopLimit()        { return Optional.of((byte) 1); }
        @Override public Optional<Byte>   remainingHopLimit()  { return Optional.of((byte) 1); }

        public SingleHop withMaxLifetimeSeconds(double lifetimeSeconds) {
            return new SingleHop(Optional.of(lifetimeSeconds));
        }
    }

    public static SingleHop singleHop() {
        Optional<Double> lifetime = Optional.empty();
        return new SingleHop(lifetime);
    }


    public static class Geobroadcast extends Destination {
        final private Area               area;
        final private Optional<Double>  maxLifetimeSeconds;
        final private Optional<Byte>    maxHopLimit;
        final private Optional<Byte>    remainingHopLimit;

        private Geobroadcast (
                Area              area,
                Optional<Double>  maxLifetimeSeconds,
                Optional<Byte>    maxHopLimit,
                Optional<Byte>    remainingHopLimit
                ) {
            this.area               = area;
            this.maxLifetimeSeconds = maxLifetimeSeconds;
            this.maxHopLimit        = maxHopLimit;
            this.remainingHopLimit  = remainingHopLimit;
        }

        public Area area() { return area; }

        @Override
        public DestinationType typeAndSubtype() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds; }
        @Override public Optional<Byte>   maxHopLimit()        { return maxHopLimit;        }
        @Override public Optional<Byte>   remainingHopLimit()  { return remainingHopLimit;  }

        public Geobroadcast withMaxLifetimeSeconds(double lifetimeSeconds) {
            return new Geobroadcast(
                this.area,
                Optional.of(lifetimeSeconds),
                this.maxHopLimit,
                this.remainingHopLimit
            );
        }
        public Geobroadcast withMaxHopLimit(byte maxHopLimit) {
            return new Geobroadcast(
                this.area,
                this.maxLifetimeSeconds,
                Optional.of(maxHopLimit),
                this.remainingHopLimit
            );
        }
        public Geobroadcast withRemainingHopLimit(byte remainingHopLimit) {
            return new Geobroadcast(
                this.area,
                this.maxLifetimeSeconds,
                this.maxHopLimit,
                Optional.of(remainingHopLimit)
            );
        }
    }

    public static Geobroadcast geobroadcast(Area area) {
        Optional<Double> emptyLifetime = Optional.empty();
        Optional<Byte>   emptyHops     = Optional.empty();
        return new Geobroadcast(area, emptyLifetime, emptyHops, emptyHops);
    }

    public static class Beacon extends Destination {
        private Beacon() {}
        @Override public DestinationType  typeAndSubtype()     { return DestinationType.BEACON; }
        @Override public Optional<Double> maxLifetimeSeconds() { return Optional.empty();       }
        @Override public Optional<Byte>   maxHopLimit()        { return Optional.of((byte) 1);  }
        @Override public Optional<Byte>   remainingHopLimit()  { return Optional.of((byte) 1);  }
    }

    public static Beacon beacon() { return new Beacon(); }
}
