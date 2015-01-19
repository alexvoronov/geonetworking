package net.gcdc.geonetworking;


public abstract class Destination {

    public abstract DestinationType  typeAndSubtype();
    public abstract Optional<Double> maxLifetimeSeconds();
    public abstract Optional<Byte>   maxHopLimit();
    public abstract Optional<Byte>   remainingHopLimit();

    public static class SingleHop extends Destination {
        private final Optional<Double> maxLifetimeSeconds;

        public SingleHop(Optional<Double> maxLifetimeSeconds) {
            this.maxLifetimeSeconds = maxLifetimeSeconds;
        }

        @Override public DestinationType  typeAndSubtype()     { return DestinationType.SINGLE_HOP; }
        @Override public Optional<Double> maxLifetimeSeconds() { return maxLifetimeSeconds; }
        @Override public Optional<Byte>   maxHopLimit()        { return Optional.of((byte) 1); }
        @Override public Optional<Byte>   remainingHopLimit()  { return Optional.of((byte) 1); }
    }

    public static SingleHop singleHop() {
        Optional<Double> lifetime = Optional.empty();
        return singleHop(lifetime);
    }
    public static SingleHop singleHop(double lifetimeSeconds) {
        return singleHop(Optional.of(lifetimeSeconds));
    }
    public static SingleHop singleHop(Optional<Double> lifetimeSeconds) {
        return new SingleHop(lifetimeSeconds);
    }

    public static Geobroadcast geobroadcast(Area area) {
        Geobroadcast gbc = new Geobroadcast();
        gbc.area = area;
        return gbc;
    }

    public static class Geobroadcast extends Destination {
        public Area               area;
        private Optional<Double>  maxLifetimeSeconds;
        private Optional<Byte>    maxHopLimit;
        private Optional<Byte>    remainingHopLimit;

        @Override
        public DestinationType typeAndSubtype() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public net.gcdc.geonetworking.Optional<Double> maxLifetimeSeconds() {
            return maxLifetimeSeconds;
        }

        @Override
        public net.gcdc.geonetworking.Optional<Byte> maxHopLimit() {
            return maxHopLimit;
        }

        @Override
        public net.gcdc.geonetworking.Optional<Byte> remainingHopLimit() {
            return remainingHopLimit;
        }
    }
}
