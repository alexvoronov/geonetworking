package net.gcdc.geonetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DuplicatorLinkLayer {

    public static abstract class QueuingLinkLayer implements LinkLayer {

        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

        @Override
        public byte[] receive() throws IOException, InterruptedException {
            return queue.take();
        }

        public void addToQueue(byte[] payload) throws InterruptedException {
            queue.put(payload);
        }
    }

    Collection<QueuingLinkLayer> links = new ArrayList<>();

    public void sendToAll(byte[] payload) throws InterruptedException {
        for (QueuingLinkLayer l : links) {
            l.addToQueue(payload.clone());
        }
    }

    public LinkLayer get() {
        QueuingLinkLayer link = new QueuingLinkLayer() {
            @Override
            public void send(byte[] payload) throws IOException {
                try {
                    sendToAll(payload);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void close() { }
        };
        links.add(link);
        return link;
    }

}
