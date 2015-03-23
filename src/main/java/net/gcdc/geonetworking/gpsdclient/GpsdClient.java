package net.gcdc.geonetworking.gpsdclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GpsdClient implements PositionProvider, AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(GpsdClient.class);

    private TPV                   lastSeenTPV = null;

    private final Gson            gson        = new GsonBuilder().create();
    private final Socket          socket;  // GPSd uses TCP socket.
    private final BufferedReader  rdr;
    private final BufferedWriter  wrt;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Future<?>       runner;

    public GpsdClient(InetSocketAddress address) throws IOException {
        logger.info("Starting GPSd client");
        socket = new Socket(address.getAddress(), address.getPort());
        rdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        wrt = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Test with Plugtest server: "nc 212.234.160.4 1941"
        wrt.write("?WATCH={\"enable\":true,\"json\":true};\n");  // Send us updates automatically.
        wrt.flush();
        runner = executor.submit(new Runnable() {
            @Override public void run() {
                // We don't need to poll with "?POLL;" command if "json":true was in ?WATCH.
                while (rdr != null) {
                    String line = null;
                    try {
                        line = rdr.readLine();  // GPSd speaks one long line per one message.
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (line == null)
                        break;
                    if (line.startsWith("{\"class\":\"TPV\"")) {
                        TPV msg = gson.fromJson(line, TPV.class);
                        lastSeenTPV = msg;
                    } else if (line.startsWith("{\"class\":\"SKY\"")) {
                        @SuppressWarnings("unused")
                        SKY msg = gson.fromJson(line, SKY.class);  // Ignore.
                    } else {
                        // Ignore.
                    }
                }
            }
        });
    }

    @Override
    public LongPositionVector getLatestPosition() {
        Optional<Address> emptyAddress = Optional.empty();
        if (lastSeenTPV == null) {
            Instant timestamp = Instant.now();
            Position position = new Position(Double.NaN, Double.NaN);  // NaN or 0?
            boolean isPositionConfident = false;
            double speedMetersPerSecond = 0;
            double headingDegreesFromNorth = 0;
            return new LongPositionVector(emptyAddress, timestamp, position, isPositionConfident,
                    speedMetersPerSecond, headingDegreesFromNorth);
        } else {
            final TPV tpv = lastSeenTPV;  // Is this enough to ensure that tpv will remain the same
                                          // through the rest of the method?
            Instant timestamp = OffsetDateTime.parse(tpv.time()).toInstant();
            Position position = new Position(tpv.lat(), tpv.lon());
            boolean isPositionConfident = false;  // TODO: double-check conditions for PAI=true.
            double speedMetersPerSecond = tpv.speed();
            double headingDegreesFromNorth = tpv.track();
            return new LongPositionVector(emptyAddress, timestamp, position, isPositionConfident,
                    speedMetersPerSecond, headingDegreesFromNorth);
        }
    }

    @Override
    public void close() throws IOException {
        rdr.close();  // Needed?
        wrt.close();
        socket.close();
        runner.cancel(true);
        executor.shutdownNow();
    }

    // Just a test main method.
    // java -cp ... net.gcdc.geonetworking.gpsdclient.GpsdClient 212.234.160.4:1945
    public static void main(String[] args) {
        final String usage = "Usage: java ... net.gcdc.geonetworking.gpsdclient.GpsdClient host:port";
        if (args.length != 1) {
            System.err.println(usage);
            System.exit(1);
        }

        String[] hostAndPort = args[0].split(":");
        if (hostAndPort.length != 2) {
            System.err.println(usage);
            System.exit(1);
        }
        InetSocketAddress remoteAddress =
                new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));

        try (GpsdClient client = new GpsdClient(remoteAddress)) {
            while(true) {
                System.out.println(client.getLatestPosition());
                System.out.flush();
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
