package net.gcdc.geonetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CutoffableDuplicatorLinkLayer {

    private final boolean hasEthernetHeader = true;

    private double cutoffMeters;

    public CutoffableDuplicatorLinkLayer(double cutoffMeters) {
        this.cutoffMeters = cutoffMeters;
    }

    public static abstract class PositionedQueuingLinkLayer implements LinkLayer {

        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

        @Override
        public byte[] receive() throws IOException, InterruptedException { return queue.take(); }

        public void addToQueue(byte[] payload) throws InterruptedException { queue.put(payload); }

        public abstract Position position();
    }

    Collection<PositionedQueuingLinkLayer> links = new ArrayList<>();

    public void sendToAll(byte[] payload, Position senderPosition) throws InterruptedException {
        for (PositionedQueuingLinkLayer l : links) {
            if (l.position().distanceInMetersTo(senderPosition) < cutoffMeters) {
                l.addToQueue(payload.clone());
            }
        }
    }

    public LinkLayer get(final PositionProvider positionProvider) {
        PositionedQueuingLinkLayer link = new PositionedQueuingLinkLayer() {

            @Override public void send(byte[] payload) throws IOException {
                try {
                    sendToAll(payload, position());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override public void close() { }

            @Override public boolean hasEthernetHeader() { return hasEthernetHeader; }

            @Override public Position position() { return positionProvider.getLatestPosition().position(); }
        };
        links.add(link);
        return link;
    }

}
