package net.gcdc.vehicle;

import java.nio.ByteBuffer;
import net.gcdc.camdenm.CoopIts.*;
import net.gcdc.camdenm.Iclcm.*;

import net.gcdc.geonetworking.Area;
import net.gcdc.geonetworking.Area.*;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination.Geobroadcast;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;

import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.Optional;

import net.gcdc.geonetworking.*;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.threeten.bp.Instant;

import org.junit.Test;

//TODO: This needs some cleaning up
public class VehicleAdapterTest{
    public static final int MAX_PACKET_LENGTH = 2000;
    VehicleAdapter va;
    public static VehiclePositionProvider vehiclePositionProvider;

    /* PositionProvider is used by the beaconing service and for
     * creating the Geobroadcast address used for DENM messages.
     */
    public static class VehiclePositionProvider implements PositionProvider{
        public Address address;        
        public Position position;
        public boolean isPositionConfident;
        public double speedMetersPerSecond;
        public double headingDegreesFromNorth;

        //TODO: Remove once we have a proper address
        Optional<Address> emptyAddress = Optional.empty();

        VehiclePositionProvider(Address address){
            this.address = address;
            this.position = new Position(0, 0);
            this.isPositionConfident = false;
            this.speedMetersPerSecond = 0;
            this.headingDegreesFromNorth = 0;
        }

        //TODO: Is the formatting of lat/long the same as in the CAM message?
        public void updatePosition(int latitude, int longitude){
            this.position = new Position((double) latitude, (double) longitude);
        }

        public Position getPosition(){
            return position;
        }

        public LongPositionVector getLatestPosition(){
            return new LongPositionVector(emptyAddress,
                                          Instant.now(),
                                          position,
                                          isPositionConfident,
                                          speedMetersPerSecond,
                                          headingDegreesFromNorth);
        }
    }

    public void init() throws SocketException{
        StationConfig config = new StationConfig();
        LinkLayer linkLayer =
            new LinkLayerUdpToEthernet(0,
                                       new InetSocketAddress(0),
                                       true);
        
        MacAddress senderMac = new MacAddress(0);

        //TODO: Add a proper address
        /* TODO: StationType is both a class in CoopITS and an ENUM in
         * geonetworking 
         */
        vehiclePositionProvider = new VehiclePositionProvider(null);
        
        va = new VehicleAdapter(0, config, linkLayer,
                                vehiclePositionProvider, senderMac);
    }
    
    @Test
    public void testCam() throws SocketException{
        if(va == null) init();
        byte[] buffer = new byte[MAX_PACKET_LENGTH];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        //TODO: Replace all zeroes with non-zero values
        byteBuffer.put((byte) 2); //messageID
        byteBuffer.putInt(1337); //stationID
        byteBuffer.putInt(1); //generationDeltaTime
        byteBuffer.put((byte) 128); //containerMask
        byteBuffer.putInt(5); //stationType                
        byteBuffer.putInt(900000001); //latitude
        byteBuffer.putInt(1800000001); //longitude
        byteBuffer.putInt(0); //semiMajorConfidence
        byteBuffer.putInt(0); //semiMinorConfidence
        byteBuffer.putInt(0); //semiMajorOrientation
        byteBuffer.putInt(0); //altitude
        byteBuffer.putInt(1); //heading value
        byteBuffer.putInt(1); //headingConfidence
        byteBuffer.putInt(0); //speedValue
        byteBuffer.putInt(1); //speedConfidence        
        byteBuffer.putInt(40); //vehicleLength
        byteBuffer.putInt(20); //vehicleWidth
        byteBuffer.putInt(0); //longitudinalAcc
        byteBuffer.putInt(1); //longitudinalAccConf
        byteBuffer.putInt(YawRateValue.unavailable); //yawRateValue
        byteBuffer.putInt(1); //yawRateConfidence        
        byteBuffer.putInt(VehicleRole.taxi.value()); //vehicleRole

        Cam cam = va.simulinkToCam(buffer);

        byte[] received = new byte[MAX_PACKET_LENGTH];
        va.camToSimulink(cam, received);
       
        for(int i = 0;i < buffer.length;i++){
            if(buffer[i] != received[i]){
                System.out.println("[ERROR] CAM ELEMENT " + i + ":\tExpected " + buffer[i] + " GOT " + received[i]);
            }
        }

        assert(Arrays.equals(buffer, received));
    }

    @Test
    public void testDenm() throws SocketException{
        if(va == null) init();
        byte[] buffer = new byte[MAX_PACKET_LENGTH];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        //TODO: Replace all zeroes with non-zero values
        byteBuffer.put((byte) 1); //messageId
        byteBuffer.putInt(1337); //stationID
        byteBuffer.putInt(1000); //generationDeltaTime
        byteBuffer.put((byte) 160); //containerMask
        byteBuffer.put((byte) 64); //managementMask
        byteBuffer.putInt(1); //detectionTime
        byteBuffer.putInt(2); //referenceTime
        byteBuffer.putInt(0); //termination
        byteBuffer.putInt(Latitude.unavailable); //latitude
        byteBuffer.putInt(Longitude.unavailable); //longtitude
        byteBuffer.putInt(SemiAxisLength.unavailable); //semiMajorConfidence
        byteBuffer.putInt(SemiAxisLength.unavailable); //semiMinorConfidence
        byteBuffer.putInt(HeadingValue.unavailable); //semiMajorOrientation
        byteBuffer.putInt(AltitudeValue.unavailable); //altitude
        byteBuffer.putInt(0); //relevanceDistance
        byteBuffer.putInt(0); //relevanceTrafficDirection
        byteBuffer.putInt(0); //validityDuration
        byteBuffer.putInt(0); //transmissionIntervall
        byteBuffer.putInt(net.gcdc.camdenm.CoopIts.StationType.passengerCar); //stationType
        byteBuffer.put((byte) 128);    //situationMask
        byteBuffer.putInt(4); //informationQuality
        byteBuffer.putInt(CauseCodeType.dangerousSituation); //causeCode
        byteBuffer.putInt(2); //subCauseCode
        byteBuffer.putInt(0); //linkedCuaseCode
        byteBuffer.putInt(0); //linkedSubCauseCode
        byteBuffer.put((byte) 8);    //alacarteMask
        byteBuffer.putInt(0); //lanePosition
        byteBuffer.putInt(0); //temperature
        byteBuffer.putInt(5); //positioningSolutionType        

        Denm denm = va.simulinkToDenm(buffer);
        byte[] received = new byte[MAX_PACKET_LENGTH];
        va.denmToSimulink(denm, received);

        
        for(int i = 0;i < buffer.length;i++){
            if(buffer[i] != received[i]){
                System.out.println("[ERROR] DENM ELEMENT " + i + ":\t Expected " + buffer[i] + " Got " + received[i]);
            }
        }

        assert(Arrays.equals(buffer, received));
    }

    @Test
    public void testIclcm() throws SocketException{
        if(va == null) init();
        byte[] buffer = new byte[MAX_PACKET_LENGTH];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        //TODO: Replace all zeroes with non-zero values
        byteBuffer.put((byte) 10); //messageID
        byteBuffer.putInt(1337); //stationID
        byteBuffer.put((byte) 128); //containerMask
        byteBuffer.putInt(100); //rearAxleLocation
        byteBuffer.putInt(0); //controllerType
        byteBuffer.putInt(1001); //responseTimeConstant
        byteBuffer.putInt(1001); //responseTimeDelay
        byteBuffer.putInt(TargetLongitudonalAcceleration.unavailable); //targetLongAcc
        byteBuffer.putInt(TimeHeadway.unavailable); //timeHeadway
        byteBuffer.putInt(CruiseSpeed.unavailable); //cruiseSpeed
        byteBuffer.put((byte) 128); //lowFrequencyMask
        byteBuffer.putInt(1); //participantsReady
        byteBuffer.putInt(0); //startPlatoon
        byteBuffer.putInt(0); //endOfScenario
        byteBuffer.putInt(255); //mioID
        byteBuffer.putInt((int) MioRange.unavailable); //mioRange
        byteBuffer.putInt((int) MioBearing.unavailable); //mioBearing
        byteBuffer.putInt((int) MioRangeRate.unavailable); //mioRangeRate
        byteBuffer.putInt(3); //lane
        byteBuffer.putInt(0); //forwardID
        byteBuffer.putInt(0); //backwardID
        byteBuffer.putInt(0); //ackFlag
        byteBuffer.putInt(0); //mergeRequest
        byteBuffer.putInt(0); //safeToMerge
        byteBuffer.putInt(1); //flag
        byteBuffer.putInt(0); //flagTail
        byteBuffer.putInt(1); //flagHead
        byteBuffer.putInt(254); //platoonID
        byteBuffer.putInt(100); //distanceTravelledCz
        byteBuffer.putInt(2); //intention
        byteBuffer.putInt(6); //counter
        
        IgameCooperativeLaneChangeMessage iclcm = va.simulinkToIclcm(buffer);
        byte[] received = new byte[MAX_PACKET_LENGTH];
        va.iclcmToSimulink(iclcm, received);

        
        for(int i = 0;i < buffer.length;i++){
            if(buffer[i] != received[i]){
                System.out.println("[ERROR] iCLCM ELEMENT " + i + ":\t Expected " + buffer[i] + " Got " + received[i]);
            }
        }

        assert(Arrays.equals(buffer, received));
    }
}
