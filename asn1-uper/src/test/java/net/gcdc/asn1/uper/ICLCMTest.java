package net.gcdc.asn1.uper;

import static org.junit.Assert.assertEquals;


import net.gcdc.camdenm.CoopIts.GenerationDeltaTime;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.ProtocolVersion;
import net.gcdc.camdenm.Iclcm;
import net.gcdc.camdenm.Iclcm.ControllerType;
import net.gcdc.camdenm.Iclcm.CruiseSpeed;
import net.gcdc.camdenm.Iclcm.EndOfScenario;
import net.gcdc.camdenm.Iclcm.IclmParameters;
import net.gcdc.camdenm.Iclcm.IgameCooperativeLaneChangeMessage;
import net.gcdc.camdenm.Iclcm.IgameCooperativeLaneChangeMessageBody;
import net.gcdc.camdenm.Iclcm.Lane;
import net.gcdc.camdenm.Iclcm.LaneObject;
import net.gcdc.camdenm.Iclcm.MergeObject;
import net.gcdc.camdenm.Iclcm.MioBearing;
import net.gcdc.camdenm.Iclcm.MioRange;
import net.gcdc.camdenm.Iclcm.MioRangeRate;
import net.gcdc.camdenm.Iclcm.MostImportantObjectContainer;
import net.gcdc.camdenm.Iclcm.PairIdObject;
import net.gcdc.camdenm.Iclcm.ParticipantsReady;
import net.gcdc.camdenm.Iclcm.ScenarioObject;
import net.gcdc.camdenm.Iclcm.StartPlatoon;
import net.gcdc.camdenm.Iclcm.TargetLongitudonalAcceleration;
import net.gcdc.camdenm.Iclcm.TimeHeadway;
import net.gcdc.camdenm.Iclcm.VehicleContainerHighFrequency;
import net.gcdc.camdenm.Iclcm.VehicleContainerLowFrequency;
import net.gcdc.camdenm.Iclcm.VehicleRearAxleLocation;
import net.gcdc.camdenm.Iclcm.VehicleResponseTime;
import net.gcdc.camdenm.Iclcm.VehicleResponseTimeConstant;
import net.gcdc.camdenm.Iclcm.VehicleResponseTimeDelay;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICLCMTest {
	private final static Logger logger = LoggerFactory.getLogger(UperEncoder.class);
	
	@Test public void encodeParameterContainersVHF() {
		
		//VehicleContainerHighFrequency
		VehicleRearAxleLocation vehicleRearAxleLocation = new VehicleRearAxleLocation();
		ControllerType controllerType = new ControllerType();
		VehicleResponseTime vehicleResponseTime = new VehicleResponseTime();
		TargetLongitudonalAcceleration targetLongitudinalAcceleration = new TargetLongitudonalAcceleration();
		TimeHeadway timeHeadway = new TimeHeadway();
		CruiseSpeed cruisespeed = new CruiseSpeed();
		StationID stationID = new StationID(12345);
		GenerationDeltaTime deltaTime = new GenerationDeltaTime(1480);
		UperEncoder.encode(vehicleRearAxleLocation);
		UperEncoder.encode(controllerType);
		UperEncoder.encode(targetLongitudinalAcceleration);
		UperEncoder.encode(timeHeadway);
		UperEncoder.encode(cruisespeed);
		
		byte[] encoded_responsetime = UperEncoder.encode(vehicleResponseTime);
		logger.debug("hex: {}", getStringByteCode(encoded_responsetime));
		logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded_responsetime));
		
		byte[] encoded_deltaTime = UperEncoder.encode(deltaTime);
		logger.debug("hex: {}", getStringByteCode(encoded_deltaTime));
		logger.debug("data hex: {}", UperEncoder.hexStringFromBytes(encoded_deltaTime));
		
		byte[] encoded_stationID = UperEncoder.encode(stationID);
		logger.debug("hex: {}", getStringByteCode(encoded_stationID));
		
		byte[] encoded_highFrequency=UperEncoder.encode(new VehicleContainerHighFrequency());
		logger.debug("hex: {}", getStringByteCode(encoded_highFrequency));
		logger.debug("bin out: {}", UperEncoder.hexStringFromBytes(encoded_highFrequency));
		assertEquals("000000001F40000000",UperEncoder.hexStringFromBytes(encoded_highFrequency));
		
		
	}
	
	@Test public void test_encodeDecodeVehicleContainerHighFrequency(){
		// Empty default message
		byte[] encoded_highVelocity=UperEncoder.encode(new VehicleContainerHighFrequency());
		logger.debug("hex: {}", getStringByteCode(encoded_highVelocity));
		logger.debug("bin out: {}", UperEncoder.hexStringFromBytes(encoded_highVelocity));
		assertEquals("000000001F40000000",UperEncoder.hexStringFromBytes(encoded_highVelocity));
		
		VehicleContainerHighFrequency vf_filled = new VehicleContainerHighFrequency(
				new VehicleRearAxleLocation(4095),
				new ControllerType(ControllerType.cacc),
				new VehicleResponseTime(
						new VehicleResponseTimeConstant(VehicleResponseTimeConstant.oneSecond),
						new VehicleResponseTimeDelay(VehicleResponseTimeDelay.oneSecond)),
				new TargetLongitudonalAcceleration(0),
				new TimeHeadway(TimeHeadway.oneSecond),
				new CruiseSpeed(CruiseSpeed.oneMeterPerSecond));
		
		byte[] encoded_vf_filled=UperEncoder.encode(vf_filled);
		logger.debug("hex: {}", getStringByteCode(encoded_vf_filled));
		logger.debug("bin out: {}", UperEncoder.hexStringFromBytes(encoded_vf_filled));
		assertEquals("FFFC64191F40280C80",UperEncoder.hexStringFromBytes(encoded_vf_filled));
	}
	
	@Test public void test_encodeLowFrequencyContainer(){
		ParticipantsReady participantsReady = new ParticipantsReady();//41
		StartPlatoon startPlatoon = new StartPlatoon();//42
		EndOfScenario endOfScenario = new EndOfScenario();//43
		VehicleContainerLowFrequency lowfreq_empty = new VehicleContainerLowFrequency();
		VehicleContainerLowFrequency lowfreq_filled_all= new VehicleContainerLowFrequency.Builder()
			.participantsReady(participantsReady)
			.startPlatoon(startPlatoon)
			.endOfScenario(endOfScenario).create();
		
		byte[] encoded_empty = UperEncoder.encode(lowfreq_empty);
		byte[] encoded_full = UperEncoder.encode(lowfreq_filled_all);
		
		logger.debug("bin empty: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("bin full: {}", UperEncoder.hexStringFromBytes(encoded_full));
		
		assertEquals("00",UperEncoder.hexStringFromBytes(encoded_empty));
		assertEquals("E0",UperEncoder.hexStringFromBytes(encoded_full));
	
	}
	
	@Test public void test_encodeDecodeMostImportantObjectContainer(){
		MostImportantObjectContainer mioContainerDefault = new MostImportantObjectContainer(
				new StationID(), new MioRange(), new MioBearing(), new MioRangeRate() 
				);
		
		byte[] encoded_empty = UperEncoder.encode(mioContainerDefault);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		assertEquals("00000000FFFF623FFFE0",UperEncoder.hexStringFromBytes(encoded_empty));
	}
	
	@Test public void test_encodeLaneObject(){
		LaneObject lane_unavailable = new LaneObject(new Lane(Lane.unavailable));
		LaneObject lane_one = new LaneObject(new Lane(Lane.laneOne));
		
		byte[] encoded_empty = UperEncoder.encode(lane_unavailable);
		byte[] encoded_one = UperEncoder.encode(lane_one);
		
		logger.debug("bin-empty: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		
		assertEquals("C0",UperEncoder.hexStringFromBytes(encoded_empty));//Unavailable
		assertEquals("00",UperEncoder.hexStringFromBytes(encoded_one));//One
	}
	
	@Test public void test_encodePairIdObject(){
		PairIdObject pairObject = new PairIdObject();
		
		byte[] encoded_empty = UperEncoder.encode(pairObject);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		assertEquals(UperEncoder.hexStringFromBytes(encoded_empty),"000000000000000000");
	}
	
	@Test public void test_encodeMergeObject(){
		MergeObject obj = new MergeObject();
		
		byte[] encoded_empty = UperEncoder.encode(obj);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		assertEquals(UperEncoder.hexStringFromBytes(encoded_empty),"00");
	}
	
	@Test public void test_encodeScenarioObject(){
		ScenarioObject so = new ScenarioObject();
		
		byte[] encoded_empty = UperEncoder.encode(so);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		assertEquals(UperEncoder.hexStringFromBytes(encoded_empty),"00000000");
	}
	
	@Test public void test_encodeIclmParameters(){
		VehicleContainerHighFrequency vehicleContainerHighFrequency = new VehicleContainerHighFrequency();
		VehicleContainerLowFrequency lowFrequencyContainer = new VehicleContainerLowFrequency();
		MostImportantObjectContainer mostImportantObjectContainer = new MostImportantObjectContainer();
		LaneObject laneObject = new LaneObject();
		PairIdObject pairIdObject = new PairIdObject();
		MergeObject mergeObject = new MergeObject();
		ScenarioObject scenarioObject = new ScenarioObject();;
		
		IclmParameters parameters = new IclmParameters(vehicleContainerHighFrequency,
				lowFrequencyContainer,
				mostImportantObjectContainer,
				laneObject,
				pairIdObject,
				mergeObject,
				scenarioObject);
		
		byte[] encoded_empty = UperEncoder.encode(parameters);
		
		//High freq: 1*10 bytes: 00 00 00 00 00 00 00 00 00 00
		logger.debug("binary vehicleContainerHighFrequency: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(vehicleContainerHighFrequency)));
		//ERROR! Low freq: 1*1 byte: 00
		logger.debug("binary lowFrequencyContainer: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(lowFrequencyContainer)));
		//MIO: 1* 11 bytes: 00 00 00 00 00 00 00 00 00 00 00
		logger.debug("binary mostImportantObjectContainer: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(mostImportantObjectContainer)));
		//Lane: 1* 1 byte: 00
		logger.debug("binary laneObject: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(laneObject)));
		//PairID: 1*9 bytes: 00 00 00 00 00 00 00 00 00
		logger.debug("binary pairIdObject: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(pairIdObject)));
		//Merge: 1*1 byte: 00
		logger.debug("binary mergeObject: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(mergeObject)));
		//Scen: 1* 4 byte: 00 00 00 00
		logger.debug("binary scenarioObject: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(scenarioObject)));
		//Parameters: 1*33 bytes: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
		logger.debug("parameters: {}", UperEncoder.hexStringFromBytes(UperEncoder.encode(parameters)));
		assertEquals("800000000FA000000000000001FFFEC47FFFD8000000000000000000000000",UperEncoder.hexStringFromBytes(encoded_empty));
	}
	
	@Test public void test_encodeIgameCooperativeLaneChangeMessageBody(){
		GenerationDeltaTime generationDeltaTime = new GenerationDeltaTime();
		IclmParameters iclmParameters = new IclmParameters();
		IgameCooperativeLaneChangeMessageBody body_default = new IgameCooperativeLaneChangeMessageBody();
		IgameCooperativeLaneChangeMessageBody body_filled = new IgameCooperativeLaneChangeMessageBody(generationDeltaTime,iclmParameters);
		
		byte[] encoded_empty = UperEncoder.encode(body_default);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_empty));
		logger.debug("");
		//1*35 bytes
		assertEquals("0064000000000FA00000000000000FFFF623FFFEC0000000000000000000000000",UperEncoder.hexStringFromBytes(encoded_empty));
		
		byte[] encoded_filled = UperEncoder.encode(body_filled);
		logger.debug("bin: {}", UperEncoder.hexStringFromBytes(encoded_filled));
		logger.debug("");
		assertEquals("0064000000000FA00000000000000FFFF623FFFEC0000000000000000000000000",UperEncoder.hexStringFromBytes(encoded_filled));
	}
	
    @Test public void test_IgameCooperativeLaneChangeMessage() {
    	//NOTE: it is very important to specify ICLCM if pdu is constructed separately
    	//This test was written for the 1.0 version
    	final long protocolVersion = 1;//test is configured for this encoding
    	
    	ItsPduHeader header = new ItsPduHeader(
    			new ProtocolVersion(protocolVersion),
    			new MessageId(Iclcm.MessageID_iCLCM),
    			new StationID());
        IgameCooperativeLaneChangeMessageBody iclm = new IgameCooperativeLaneChangeMessageBody();
    	
    	IgameCooperativeLaneChangeMessage message_default = new IgameCooperativeLaneChangeMessage();
    	IgameCooperativeLaneChangeMessage message_filled = new IgameCooperativeLaneChangeMessage(header,iclm);
    	
    	
    	byte[] encoded_header = UperEncoder.encode(header);
    	logger.debug("bin header: {}", UperEncoder.hexStringFromBytes(encoded_header));
    	assertEquals("010A00000000",UperEncoder.hexStringFromBytes(encoded_header));
    	
    	byte[] encoded_default = UperEncoder.encode(message_default);
    	byte[] encoded_filled = UperEncoder.encode(message_filled);
    	logger.debug("bin default: {}", UperEncoder.hexStringFromBytes(encoded_default));
        logger.debug("bin filled: {}", UperEncoder.hexStringFromBytes(encoded_filled));

        assertEquals("010A000000000064000000000FA00000000000000FFFF623FFFEC0000000000000000000000000",UperEncoder.hexStringFromBytes(encoded_filled));
        }
    
	/**
	 * Utility: Byte array to readable hexadecimal representation
	 * @param data
	 * @return String with encoded hexadecimal representation
	 */
	public static String getStringByteCode(byte[] data){
		StringBuilder sb = new StringBuilder();
	    for (byte b : data) {
	        sb.append(String.format("%02X ", b));
	    }
	    return sb.toString();
	}
}
