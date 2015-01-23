package net.gcdc.geonetworking;

import java.io.IOException;

public class BtpSocket implements AutoCloseable {

    private GeonetStation station;

    private BtpSocket(GeonetStation station) {
        this.station = station;
    }

    public static BtpSocket on(StationConfig config, LinkLayer linkLayer, PositionProvider positionProvider) {
        GeonetStation station = new GeonetStation(config, linkLayer, positionProvider);
        new Thread(station).start();
        return new BtpSocket(station);
    }

    public void send(BtpPacket packet) throws IOException {
        station.send(new GeonetData(
                packet.sourcePort().isPresent() ?
                        UpperProtocolType.BTP_A :
                        UpperProtocolType.BTP_B,
                packet.destination(),
                packet.trafficClass(),
                packet.senderPosition(),
                packet.asBytes()
                ));
    }

    public BtpPacket receive() throws InterruptedException {
        GeonetData data = station.receive();
        while(data.protocol != UpperProtocolType.BTP_A &&
                data.protocol != UpperProtocolType.BTP_B) {
            data = station.receive();
        }
        return BtpPacket.fromGeonetData(data);
    }

    @Override
    public void close() {
        station.close();
    }
}
