package net.gcdc.geonetworking.gpsdclient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Optional;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GpsdClient implements PositionProvider, AutoCloseable,TelnetNotificationHandler,Runnable {
    private final static Logger logger = LoggerFactory.getLogger(GpsdClient.class);

    private TPV                   lastSeenTPV = null;
    private final Gson            gson        = new GsonBuilder().create();
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private Future<?>       runner;
    
    private static TelnetClient tc = null;

    @Override
    public void run(){
    	String remoteip = "127.0.0.1";
    	int remoteport = 2947;
    	
        tc = new TelnetClient();

        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
        try{
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        }
        catch (InvalidTelnetOptionException e){
            logger.error("GpsdClient:Error registering option handlers: " + e.getMessage());
        } catch (IOException e) {
			e.printStackTrace();
		}

        while (true){
            try{
                tc.connect(remoteip, remoteport);
                tc.registerNotifHandler(this);
                
                createReader();
                
                OutputStream outstr = tc.getOutputStream();

                Thread.sleep(1000);//Wait till ready
                
                String line = "?WATCH={\"enable\":true,\"json\":true}\r\n";                
                byte[] buff = line.getBytes();
                outstr.write(buff, 0 , buff.length);
                outstr.flush();
                
                //Wait till finished or error occured
                runner.get();
                
            }catch (IOException | InterruptedException | ExecutionException e){
            	//System.err.println("Exception while connecting:" + e.getMessage());
                
            	//PANIC?
            	logger.error("GpsdClient:Exception while connecting:" + e.getMessage());
            	
            	//return;//set failsafe
            	//System.exit(1);
            }
        }
    }
    
    public void onMsgReceived(String line){
    	//System.err.println(line);
    	try{
	    	if (line.equals(""))
	            return;
	        if (line.startsWith("{\"class\":\"TPV\"")) {
	            TPV msg = gson.fromJson(line, TPV.class);
	            logger.debug("tpv update:"+msg.time()+": ("+msg.lat()+","+msg.lon()+")");
	            lastSeenTPV = msg;
	        } else if (line.startsWith("{\"class\":\"SKY\"")) {
	            //@SuppressWarnings("unused")
	            
	            //SKY msg = gson.fromJson(line, SKY.class);  // Ignore.
	        } else if(line.startsWith("{\"class\":\"DEVICES\"")){
	        	//DEVICES
	    	} else if(line.startsWith("{\"class\":\"DEVICE\"")){
	    		//DEVICE (activated)
	    	}else if(line.startsWith("{\"class\":\"VERSION\"")){
	    		//VERSION INFO
	    	}else{
	        	logger.debug("??:'"+line+"'");
	        }
    	}catch(com.google.gson.JsonSyntaxException j){
    		//Parsing error
    		logger.debug("Json parsing error :"+j.getMessage()+" :: "+line);//probably partial message
    	}catch(NumberFormatException n){
    		//ignore for printing
    		logger.debug("Json parsing error (NumberFormatException) :"+n.getMessage()+" :: "+line);
    	}catch(Exception e){
    		e.printStackTrace();
    		return;
    	}
    }
    
    private Future<?> createReader(){
    	 runner = executor.submit(new Runnable() {
             @Override public void run() {
            	final int buffsize = 4096;
 	            InputStream instr = tc.getInputStream();
 	            BufferedInputStream bf = new BufferedInputStream(instr);
 	            BufferedReader reader = new BufferedReader(
 	        	        new InputStreamReader(bf, StandardCharsets.UTF_8));

 	            try{
 	                byte[] buff = new byte[buffsize];
 	                int ret_read = 0;
 	
 	                do{
 	                	
 	                    //ret_read = instr.read(buff);
 	                    //if(ret_read > 0){
 	                    //	onMsgReceived(new String(buff, 0, ret_read));
 	                    //}
 	                    
 	                   String line = reader.readLine();
 	                   if(!line.isEmpty()){
 	                	  onMsgReceived(line);
 	                   }
 	                    
 	                    
 	                }while (ret_read >= 0);
 	            }catch (IOException e){
 	            	logger.error("Exception while reading socket:" + e.getMessage());
 	            }
 	            //STOP
 	            try{
 	                tc.disconnect();
 	                //STOPPING
 	            }
 	            catch (IOException e){
 	            	logger.error("Exception while closing telnet:" + e.getMessage());
 	            }
             }
         });
    	 return runner;
    }
    
    public GpsdClient(InetSocketAddress address) throws IOException {
        logger.info("Starting GPSd client");
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
    	//socket.close
    	try{
            tc.disconnect();
        }
        catch (IOException e){
        	logger.error("Exception while closing telnet:" + e.getMessage());
        }
    	
        runner.cancel(true);
        executor.shutdownNow();
    }
    
    public GpsdClient startClient(){
    	(new Thread(this)).start();
    	return this;
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
        System.out.println("GPS: "+hostAndPort[0]+":"+hostAndPort[1]);
        InetSocketAddress remoteAddress =
                new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1]));

        try (GpsdClient client = new GpsdClient(remoteAddress)) {
        	client.startClient();
        	while(true) {
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println("Stopping GpsdClient..");
    }

	@Override
	public void receivedNegotiation(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
