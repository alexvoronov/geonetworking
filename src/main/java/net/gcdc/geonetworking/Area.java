package net.gcdc.geonetworking;

import java.nio.ByteBuffer;

public abstract class Area {
    public static enum Type {
        CIRCLE    (0),
        RECTANGLE (1),
        ELLIPSE   (2);

        private final int code;
        private Type(int code) { this.code = code; }
        public  int code()     { return code;      }

        public static Area.Type fromCode(int code) {
            for (Area.Type h: Area.Type.values()) { if (h.code() == code) { return h; } }
            throw new IllegalArgumentException("Can't recognize upper protocol: " + code);
        }
    }
    public static Area getFrom(ByteBuffer buffer, Area.Type type) {
        throw new UnsupportedOperationException("Not implemented yet.");  // TODO
    }

    public abstract ByteBuffer putTo(ByteBuffer buffer);

    public abstract Area.Type type();

    public abstract boolean contains(Position position);
}
