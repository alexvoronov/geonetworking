package net.gcdc;

import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;

public class RunAll {
    public static void main(String[] args) {
        String payload = "ASDF";

        PositionProvider positionProvider = new PositionProvider() {
            @Override
            public LongPositionVector getLatestPosition() {
                return null;
//                return new LongPositionVector(new Address(100), 100, new Position(), false, 20, 30);
            }
        };

        StationConfig config = new StationConfig();

        LinkLayer linkLayer = null;

        //GeonetStation gn = new GeonetStation(config, positionProvider, linkLayer);

        //BtpSocket socket = BtpSocket.on(gn);
//        socket.send(BtpPacket.beacon());
//        BtpPacket packet = socket.receive();
//        if (packet.port() == 222) {  // CAM
//            // Cam cam = Cam.fromBytes(packet.payload());
//        } else if (packet.port() == 333) {  // DENM
//            // do something
//        } else {}  // throw it away.

    }
}