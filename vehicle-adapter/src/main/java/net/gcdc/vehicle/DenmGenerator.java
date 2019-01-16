package net.gcdc.vehicle;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import org.threeten.bp.Instant;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.CauseCode;
import net.gcdc.camdenm.CoopIts.CauseCodeType;
import net.gcdc.camdenm.CoopIts.DecentralizedEnvironmentalNotificationMessage;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.camdenm.CoopIts.InformationQuality;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ManagementContainer;
import net.gcdc.camdenm.CoopIts.SituationContainer;
import net.gcdc.camdenm.CoopIts.SubCauseCodeType;
import net.gcdc.geonetworking.Address;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;
import net.gcdc.geonetworking.StationType;

public class DenmGenerator {
	public static void main(String[] args) throws InterruptedException, IOException {
		StationConfig config = new StationConfig();
		int snifferLinkLayerPort = 5000;  // We listen here for the data from the "Drivers".
		SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 4000);  // "Drivers" listen there.
		boolean hasEthernetHeader = true;
		LinkLayer linkLayer = new LinkLayerUdpToEthernet(snifferLinkLayerPort, 
				remoteAddress, hasEthernetHeader);
		final Address gnAddress = Address.random(true, StationType.Road_Side_Unit, 46);
		PositionProvider position = new PositionProvider() {			
			@Override
			public LongPositionVector getLatestPosition() {
				return new LongPositionVector(gnAddress, Instant.now(), 
						new Position(53, 17), false, 0, 0);
			}
		};
		GeonetStation station = new GeonetStation(config, linkLayer, position, 
				new MacAddress(gnAddress.lowLevelAddress()));
        new Thread(station).start();
		BtpSocket btpSocket = BtpSocket.on(station);
		
		
		while(true) {
			Denm denm = new Denm(new ItsPduHeader(), 
					new DecentralizedEnvironmentalNotificationMessage(
							new ManagementContainer(),
							new SituationContainer(
									new InformationQuality(),
									new CauseCode(
											new CauseCodeType(CauseCodeType.emergencyVehicleApproaching), 
											new SubCauseCodeType(0))),
							null,
							null) );
			byte[] payload = UperEncoder.encode(denm);
			BtpPacket btpPacket = BtpPacket.singleHop(payload, (short)2002);
			btpSocket.send(btpPacket);
			System.err.println("Denm sent");
			Thread.sleep(3000);
		}
	}

}
