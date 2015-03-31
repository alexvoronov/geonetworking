package net.gcdc.geonetworking;


public class TrafficClass {

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

    private final byte code;

    public TrafficClass(byte code) {
        this.code = code;
    }

    public byte asByte() {
        return code;
    }

    public static TrafficClass fromByte(byte code) {
        return new TrafficClass(code);
    }

}
