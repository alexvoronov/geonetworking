package net.gcdc.geonetworking.gpsdclient;

import com.google.gson.annotations.SerializedName;

public class SKY {
    @SerializedName("class")
    private String      clazz;
    private String      tag;
    private String      device;
    private double      time;
    private double      xdop;
    private double      ydop;
    private double      vdop;
    private double      tdop;
    private double      hdop;
    private double      pdop;
    private double      gdop;
    private Satellite[] satellites;

    public static class Satellite {
        @SerializedName("PRN")
        private int     prn;
        private int     az;
        private int     el;
        private int     ss;
        private boolean used;

        public int getPrn() {
            return prn;
        }

        public int getAz() {
            return az;
        }

        public int getEl() {
            return el;
        }

        public int getSs() {
            return ss;
        }

        public boolean isUsed() {
            return used;
        }

        @Override
        public String toString() {
            return "prn=" + prn
                    + " az=" + az
                    + " el=" + el
                    + " ss=" + ss
                    + " used=" + used;
        }
    }

    public String getClazz() {
        return clazz;
    }

    public String getTag() {
        return tag;
    }

    public String getDevice() {
        return device;
    }

    public double getTime() {
        return time;
    }

    public double getXdop() {
        return xdop;
    }

    public double getYdop() {
        return ydop;
    }

    public double getVdop() {
        return vdop;
    }

    public double getTdop() {
        return tdop;
    }

    public double getHdop() {
        return hdop;
    }

    public double getPdop() {
        return pdop;
    }

    public double getGdop() {
        return gdop;
    }

    public Satellite[] getSatellites() {
        return satellites;
    }

    public int getNofSatellites() {
        return (satellites == null) ? 0 : satellites.length;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("class=" + clazz);
        sb.append(" tag=" + tag);
        sb.append(" device=" + device);
        sb.append(" time=" + time);
        sb.append(" xdop=" + xdop);
        sb.append(" ydop=" + ydop);
        sb.append(" vdop=" + vdop);
        sb.append(" tdop=" + tdop);
        sb.append(" hdop=" + hdop);
        sb.append(" pdop=" + pdop);
        sb.append(" gdop=" + gdop);

        if (satellites == null || satellites.length < 1) {
            sb.append(" satellites=[]");
        } else {
            sb.append("\n satellites:");
            for (Satellite s : satellites) {
                sb.append("\n  " + s);
            }
        }
        return sb.toString();
    }
}
