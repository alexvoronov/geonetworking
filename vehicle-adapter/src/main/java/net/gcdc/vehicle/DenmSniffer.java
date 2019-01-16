package net.gcdc.vehicle;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.threeten.bp.Instant;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.CauseCodeType;
import net.gcdc.camdenm.CoopIts.Denm;
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

public class DenmSniffer {
	public static void main(String[] args) throws InterruptedException, IOException {
		StationConfig config = new StationConfig();
		int snifferLinkLayerPort = 4000;  // We listen here for the data from the "Drivers".
		SocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 4005);  // "Drivers" listen there.
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
		
		DatagramSocket udpSocket = new DatagramSocket();
		byte[] udpSendData = new byte[1];
		InetAddress arduinoAddress = InetAddress.getByName("192.168.1.177");
		int arduinoPort = 8888;
		
		while(true) {
			BtpPacket btpPacket = btpSocket.receive();
			System.err.println("got packet!");
			if (btpPacket.destinationPort() == 2002) {  // DENM is 2002
				System.err.println("got packet on port 2002");
				Denm denm = UperEncoder.decode(btpPacket.payload(), Denm.class);
				if (denm.getDenm().hasSituation() && 
						denm
						.getDenm()
						.getSituation()
						.getEventType()
						.getCauseCode()
						.value() == CauseCodeType.emergencyVehicleApproaching
					) {
					
					System.err.println("Emergency vehicle approaching!");
					udpSendData[0] = '1';
					DatagramPacket udpPacket = new DatagramPacket(udpSendData, udpSendData.length,
							arduinoAddress, arduinoPort);
					udpSocket.send(udpPacket);
				}
			}
		}
	}
	
	
	
}
