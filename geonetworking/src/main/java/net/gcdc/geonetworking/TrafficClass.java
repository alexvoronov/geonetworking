package net.gcdc.geonetworking;

/**
 * Traffic Class.
 *
 * The class is declared as final, just because there was no need for subclasses yet.
 * If you remove final, make sure to take good care of {@link #equals(Object)} and
 * {@link #hashCode()}.
 */
public final class TrafficClass {

	private final byte code;

    public TrafficClass(byte code) {
        this.code = code;
    }
	
	@Override
    public String toString() {
        return "TrafficClass [0x" + Integer.toHexString(code) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + code;
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
        TrafficClass other = (TrafficClass) obj;
        if (code != other.code)
            return false;
        return true;
    }

    public byte asByte() {
        return code;
    }

    public static TrafficClass fromByte(byte code) {
        return new TrafficClass(code);
    }

}
