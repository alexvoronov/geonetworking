/* Copyright 2016 Albin Severinson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.gcdc.vehicle;

import java.io.IOException;
import java.nio.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.IllegalArgumentException;

import net.gcdc.asn1.uper.UperEncoder;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.geonetworking.Area;
import net.gcdc.geonetworking.BtpPacket;
import net.gcdc.geonetworking.BtpSocket;
import net.gcdc.geonetworking.Destination.Geobroadcast;
import net.gcdc.geonetworking.GeonetStation;
import net.gcdc.geonetworking.LinkLayer;
import net.gcdc.geonetworking.MacAddress;
import net.gcdc.geonetworking.Position;
import net.gcdc.geonetworking.PositionProvider;
import net.gcdc.geonetworking.StationConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.InetAddress;


import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

/*
import net.gcdc.camdenm.CoopIts.AccelerationControl;
import net.gcdc.camdenm.CoopIts.AccelerationConfidence;
import net.gcdc.camdenm.CoopIts.ActionID;
import net.gcdc.camdenm.CoopIts.AlacarteContainer;
import net.gcdc.camdenm.CoopIts.Altitude;
import net.gcdc.camdenm.CoopIts.AltitudeConfidence;
import net.gcdc.camdenm.CoopIts.AltitudeValue;
import net.gcdc.camdenm.CoopIts.BasicContainer;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerHighFrequency;
import net.gcdc.camdenm.CoopIts.BasicVehicleContainerLowFrequency;
import net.gcdc.camdenm.CoopIts.Cam;
import net.gcdc.camdenm.CoopIts.CamParameters;
import net.gcdc.camdenm.CoopIts.CoopAwareness;
import net.gcdc.camdenm.CoopIts.Curvature;
import net.gcdc.camdenm.CoopIts.CurvatureConfidence;
import net.gcdc.camdenm.CoopIts.CurvatureValue;
import net.gcdc.camdenm.CoopIts.DangerousGoodsBasic;
import net.gcdc.camdenm.CoopIts.DangerousGoodsContainer;
import net.gcdc.camdenm.CoopIts.Denm;
import net.gcdc.camdenm.CoopIts.DecentralizedEnvironmentalNotificationMessage;
import net.gcdc.camdenm.CoopIts.DriveDirection;
import net.gcdc.camdenm.CoopIts.EmergencyContainer;
import net.gcdc.camdenm.CoopIts.ExteriorLights;
import net.gcdc.camdenm.CoopIts.GenerationDeltaTime;
import net.gcdc.camdenm.CoopIts.Heading;
import net.gcdc.camdenm.CoopIts.HeadingConfidence;
import net.gcdc.camdenm.CoopIts.HeadingValue;
import net.gcdc.camdenm.CoopIts.HighFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ItsPduHeader;
import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;
import net.gcdc.camdenm.CoopIts.Latitude;
import net.gcdc.camdenm.CoopIts.LightBarSirenInUse;
import net.gcdc.camdenm.CoopIts.LocationContainer;
import net.gcdc.camdenm.CoopIts.Longitude;
import net.gcdc.camdenm.CoopIts.LongitudinalAcceleration;
import net.gcdc.camdenm.CoopIts.LongitudinalAccelerationValue;
import net.gcdc.camdenm.CoopIts.LowFrequencyContainer;
import net.gcdc.camdenm.CoopIts.ManagementContainer;
import net.gcdc.camdenm.CoopIts.PathHistory;
import net.gcdc.camdenm.CoopIts.PosConfidenceEllipse;
import net.gcdc.camdenm.CoopIts.PtActivation;
import net.gcdc.camdenm.CoopIts.PtActivationData;
import net.gcdc.camdenm.CoopIts.PtActivationType;
import net.gcdc.camdenm.CoopIts.PublicTransportContainer;
import net.gcdc.camdenm.CoopIts.ReferencePosition;
import net.gcdc.camdenm.CoopIts.RescueContainer;
import net.gcdc.camdenm.CoopIts.RoadWorksContainerBasic;
import net.gcdc.camdenm.CoopIts.SafetyCarContainer;
import net.gcdc.camdenm.CoopIts.SemiAxisLength;
import net.gcdc.camdenm.CoopIts.SequenceNumber;
import net.gcdc.camdenm.CoopIts.SpecialTransportContainer;
import net.gcdc.camdenm.CoopIts.SpecialTransportType;
import net.gcdc.camdenm.CoopIts.SpecialVehicleContainer;
import net.gcdc.camdenm.CoopIts.Speed;
import net.gcdc.camdenm.CoopIts.SpeedConfidence;
import net.gcdc.camdenm.CoopIts.SpeedValue;
import net.gcdc.camdenm.CoopIts.StationType;
import net.gcdc.camdenm.CoopIts.StationID;
import net.gcdc.camdenm.CoopIts.SituationContainer;
import net.gcdc.camdenm.CoopIts.Termination;
import net.gcdc.camdenm.CoopIts.TimestampIts;
import net.gcdc.camdenm.CoopIts.VehicleLength;
import net.gcdc.camdenm.CoopIts.VehicleLengthValue;
import net.gcdc.camdenm.CoopIts.VehicleLengthConfidenceIndication;
import net.gcdc.camdenm.CoopIts.VehicleRole;
import net.gcdc.camdenm.CoopIts.VehicleWidth;
import net.gcdc.camdenm.CoopIts.YawRate;
import net.gcdc.camdenm.CoopIts.YawRateConfidence;
import net.gcdc.camdenm.CoopIts.YawRateValue;
*/

import net.gcdc.camdenm.CoopIts.ItsPduHeader.MessageId;


import net.gcdc.camdenm.Iclcm.*;

import net.gcdc.geonetworking.LinkLayerUdpToEthernet;
import net.gcdc.geonetworking.LongPositionVector;

import net.gcdc.geonetworking.Address;


import org.threeten.bp.Instant;

public class VehicleAdapter {
	private final static Logger logger = LoggerFactory.getLogger(VehicleAdapter.class);

	/*
	 * Sockets and GeonetStation used to send and receive BTP and UDP packets.
	 */
	private final DatagramSocket rcvSocket;
	private final GeonetStation station;
	private final BtpSocket btpSocket;

	/* BTP ports for CAM/DENM/iCLCM */
	private final static short PORT_CAM = 2001;
	private final static short PORT_DENM = 2002;
	private final static short PORT_ICLCM = 2010;

	/* Seconds for which the message is relevant. */
	public final static double CAM_LIFETIME_SECONDS = 0.9;
	public final static double iCLCM_LIFETIME_SECONDS = 0.9;

	/*
	 * Maximum size of the UDP buffer. Needs to be at least as large as the
	 * maximum message size.
	 */
	public final static int MAX_UDP_LENGTH = 300;

	/*
	 * Ports and IP address used for communicating with the vehicle control
	 * system.
	 */
	public static int simulink_cam_port = 5000;
	public static int simulink_denm_port = 5000;
	public static int simulink_iclcm_port = 5000;
	public static InetAddress simulink_address;

	/*
	 * TODO: Investigate other ways of creating the threads that will enforce
	 * lower delay.
	 */
	// public static final ExecutorService executor =
	// Executors.newCachedThreadPool();
	public static final ExecutorService executor = Executors.newFixedThreadPool(10);

	public VehicleAdapter(int portRcvFromSimulink, StationConfig config, LinkLayer linkLayer, PositionProvider position,
			MacAddress macAddress) throws SocketException {
		rcvSocket = new DatagramSocket(portRcvFromSimulink);
		station = new GeonetStation(config, linkLayer, position, macAddress);
		new Thread(station).start();

		/*
		 * TODO: Race conditions in the beaconing service is causing it to send
		 * too many beacons. Turn off until it's fixed. We don't need the
		 * beaconing service for anything we're using it for anyway as it's
		 * supposed to be quiet when sending other traffic.
		 */
		/*
		 * TODO: Thread crashes when attempting to send when the beaconing
		 * service isn't running.
		 */
		station.startBecon();

		btpSocket = BtpSocket.on(station);
		executor.submit(receiveFromSimulinkLoop);
		executor.submit(sendToSimulinkLoop);
		executor.submit(printStatistics);
	}

	/*
	 * For keeping track of the current vehicle position. Used for the
	 * broadcasting service and for generating Geonetworking addresses.
	 */
	public static VehiclePositionProvider vehiclePositionProvider;

	/* Print statistics in one second intervals. */
	private static int num_tx_cam = 0;
	private static int num_tx_denm = 0;
	private static int num_tx_iclcm = 0;
	private static int num_rx_cam = 0;
	private static int num_rx_denm = 0;
	private static int num_rx_iclcm = 0;
	private Runnable printStatistics = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.warn("Interrupted during sleep.");
			}
			System.out.println("#### Vehicle Adapter ####" + "\nListening on port " + rcvSocket.getLocalPort()
					+ "\nVehicle Control System IP is " + simulink_address + "\nSending incoming CAM to port "
					+ simulink_cam_port + "\nSending incoming DENM to port " + simulink_denm_port
					+ "\nSending incoming iCLCM to port " + simulink_iclcm_port
					+ "\nCopyright: Albin Severinson (albin@severinson.org) License: Apache 2.0"
					+ "\nNotice: GeoNetworking library by Alexey Voronov" + "\n");

			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.warn("Interrupted during sleep.");
				}
				logger.info("#CAM (Tx/Rx): {}/{}\t#DENM (Tx/Rx): {}/{}\t#iCLCM (Tx/Rx): {}/{}", num_tx_cam, num_rx_cam,
						num_tx_denm, num_rx_denm, num_tx_iclcm, num_rx_iclcm);
			}
		}
	};

	/*
	 * Receive local CAM/DENM/iCLCM from Simulink, parse them and create the
	 * proper messages, and send them to the link layer.
	 */
	private Runnable receiveFromSimulinkLoop = new Runnable() {
		byte[] buffer = new byte[MAX_UDP_LENGTH];
		private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			try {
				while (true) {
					logger.debug("Waiting for packet from vehicle control...");
					rcvSocket.receive(packet);
					byte[] receivedData = Arrays.copyOfRange(packet.getData(), packet.getOffset(),
							packet.getOffset() + packet.getLength());
					assert(receivedData.length == packet.getLength());
					logger.debug(
							"Received packet from vehicle control! ID: " + receivedData[0] + " Data: " + receivedData);

					/*
					 * executor.submit(new
					 * UdpParser(Arrays.copyOfRange(packet.getData(),
					 * packet.getOffset(), packet.getOffset() +
					 * packet.getLength())));
					 */

					/* First byte is the MessageId */
					switch (receivedData[0]) {
					case MessageId.cam: {
						num_tx_cam++;
						try {
							LocalCam localCam = new LocalCam(receivedData);
							Cam cam = localCam.asCam();
							send(cam);

							double latitude = (double) localCam.latitude;
							latitude /= 1e7;

							double longitude = (double) localCam.longitude;
							longitude /= 1e7;

							vehiclePositionProvider.updatePosition(latitude, longitude);
						} catch (IllegalArgumentException e) {
							logger.error("Irrecoverable error when creating CAM. Ignoring message.", e);
						}
						break;
					}

					case MessageId.denm: {
						num_tx_denm++;
						try {
							LocalDenm localDenm = new LocalDenm(receivedData);
							Denm denm = localDenm.asDenm();

							/*
							 * TODO: How does GeoNetworking addressing work in
							 * GCDC16? For now let's just broadcast everything
							 * in a large radius.
							 */
							send(denm, Geobroadcast.geobroadcast(
									Area.circle(vehiclePositionProvider.getPosition(), Double.MAX_VALUE)));
						} catch (IllegalArgumentException e) {
							logger.error("Irrecoverable error when creating DENM. Ignoring message.", e);
						}
						break;
					}

					case net.gcdc.camdenm.Iclcm.MessageID_iCLCM: {
						num_tx_iclcm++;
						try {
							LocalIclcm localIclcm = new LocalIclcm(receivedData);
							IgameCooperativeLaneChangeMessage iclcm = localIclcm.asIclcm();
							send(iclcm);
						} catch (IllegalArgumentException e) {
							logger.error("Irrecoverable error when creating iCLCM. Ignoring message.", e);
						}
						break;
					}

					default:
						logger.warn("Received incorrectly formated message! ID: {} Data: {}", receivedData[0],
								receivedData);
					}
				}
			} catch (IOException e) {
				logger.error("Failed to receive packet from Simulink, terminating", e);
				System.exit(1);
			}
		}
	};

	/*
	 * Take a received UDP packet, parse it and send it as a GeoNetworking
	 * packet.
	 */
	/*
	 * TODO: The performance benefit of parallelizing this part was quite low.
	 * Needs some more research if we're gonna do it.
	 */
	private class UdpParser implements Runnable {
		byte[] receivedData;

		UdpParser(byte[] receivedData) {
			this.receivedData = receivedData;
		}

		@Override
		public void run() {
			logger.info("This souldn't run!");
		}
	}

	/*
	 * Receive incoming CAM/DENM/iCLCM to Simulink, convert them to their local
	 * representation, and send them to Simulink over UDP.
	 */
	private Runnable sendToSimulinkLoop = new Runnable() {
		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			try {
				while (true) {
					BtpPacket btpPacket;
					;
					btpPacket = btpSocket.receive();
					executor.submit(new BtpParser(btpPacket));
				}
			} catch (InterruptedException e) {
				logger.warn("BTP socket receive was interrupted", e);
			}
		}
	};

	/*
	 * Take a received BtpPacket, decode it and send it to the control system.
	 */
	private class BtpParser implements Runnable {
		private byte[] buffer = new byte[MAX_UDP_LENGTH];
		private final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		private BtpPacket btpPacket;

		BtpParser(BtpPacket btpPacket) {
			this.btpPacket = btpPacket;
		}

		@Override
		public void run() {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
			packet.setAddress(simulink_address);
			switch (btpPacket.destinationPort()) {
			case PORT_CAM: {
				Cam cam;
				try {
					num_rx_cam++;
					cam = UperEncoder.decode(btpPacket.payload(), Cam.class);
					LocalCam localCam = new LocalCam(cam);

					buffer = localCam.asByteArray();
					packet.setData(buffer, 0, buffer.length);

					packet.setPort(simulink_cam_port);

					try {
						rcvSocket.send(packet);
					} catch (IOException e) {
						logger.warn("Failed to send CAM to Simulink", e);
					}
				} catch (NullPointerException e) {
					logger.warn("Can't decode CAM: Incorrect formatting.");
				} catch (IllegalArgumentException | UnsupportedOperationException | BufferOverflowException e) {
					logger.warn("Can't decode CAM:", e);
				}
				break;
			}

			case PORT_DENM: {
				Denm denm;
				try {
					num_rx_denm++;
					denm = UperEncoder.decode(btpPacket.payload(), Denm.class);
					LocalDenm localDenm = new LocalDenm(denm);

					buffer = localDenm.asByteArray();
					packet.setData(buffer, 0, buffer.length);

					packet.setPort(simulink_denm_port);

					try {
						rcvSocket.send(packet);
					} catch (IOException e) {
						logger.warn("Failed to send DENM to Simulink", e);
					}
				} catch (NullPointerException e) {
					logger.warn("Can't decode DENM: Incorrect formatting.");
				} catch (IllegalArgumentException | UnsupportedOperationException | BufferOverflowException e) {
					logger.warn("Can't decode DENM:", e);
				}
				break;
			}

			case PORT_ICLCM: {
				IgameCooperativeLaneChangeMessage iclcm;
				try {
					num_rx_iclcm++;
					iclcm = UperEncoder.decode(btpPacket.payload(), IgameCooperativeLaneChangeMessage.class);
					LocalIclcm localIclcm = new LocalIclcm(iclcm);

					buffer = localIclcm.asByteArray();
					packet.setData(buffer, 0, buffer.length);

					packet.setPort(simulink_iclcm_port);

					try {
						rcvSocket.send(packet);
					} catch (IOException e) {
						logger.warn("Failed to send iCLCM to Simulink", e);
					}
				} catch (NullPointerException e) {
					logger.warn("Can't decode iCLCM: Incorrect formatting.");
				} catch (IllegalArgumentException | UnsupportedOperationException | BufferOverflowException e) {
					logger.warn("Can't decode iCLCM:", e);
				}
				break;
			}

			default:
				// fallthrough
			}
		}
	};

	public void send(Cam cam) {
		byte[] bytes;
		try {
			bytes = UperEncoder.encode(cam);
		} catch (IllegalArgumentException | UnsupportedOperationException e) {
			logger.warn("Failed to encode CAM {}, ignoring", cam, e);
			return;
		}
		BtpPacket packet = BtpPacket.singleHop(bytes, PORT_CAM, CAM_LIFETIME_SECONDS);
		try {
			btpSocket.send(packet);
		} catch (IOException e) {
			logger.warn("failed to send cam", e);
		}
	}

	private void send(Denm denm, Geobroadcast destination) {
		byte[] bytes;
		try {
			bytes = UperEncoder.encode(denm);
		} catch (IllegalArgumentException | UnsupportedOperationException e) {
			logger.error("Failed to encode DENM {}, ignoring", denm, e);
			return;
		}
		BtpPacket packet = BtpPacket.customDestination(bytes, PORT_DENM, destination);
		try {
			btpSocket.send(packet);
		} catch (IOException e) {
			logger.warn("failed to send denm", e);
		}
	}

	private void send(IgameCooperativeLaneChangeMessage iclcm) {
		byte[] bytes;
		try {
			bytes = UperEncoder.encode(iclcm);
		} catch (IllegalArgumentException | UnsupportedOperationException e) {
			logger.error("Failed to encode iCLCM {}, ignoring", iclcm, e);
			return;
		}
		BtpPacket packet = BtpPacket.singleHop(bytes, PORT_ICLCM, iCLCM_LIFETIME_SECONDS);
		try {
			btpSocket.send(packet);
		} catch (IOException e) {
			logger.warn("Failed to send iclcm", e);
		}
	}

	public static class SocketAddressFromString { // Public, otherwise JewelCLI
													// can't access it!
		private final InetSocketAddress address;

		public SocketAddressFromString(final String addressStr) {
			String[] hostAndPort = addressStr.split(":");
			if (hostAndPort.length != 2) {
				throw new ArgumentValidationException("Expected host:port, got " + addressStr);
			}
			String hostname = hostAndPort[0];
			int port = Integer.parseInt(hostAndPort[1]);
			this.address = new InetSocketAddress(hostname, port);
		}

		public InetSocketAddress asInetSocketAddress() {
			return address;
		}
	}

	private static interface CliOptions {
		/* Port to receive messages from Simulink on */
		@Option
		int getPortRcvFromSimulink();

		/*
		 * Ports to send CAM, DENM, iCLCM messages on. These ports can be the
		 * same or different.
		 */
		@Option
		int getPortSendCam();

		@Option
		int getPortSendDenm();

		@Option
		int getPortSendIclcm();

		/* IP of Simulink */
		@Option
		String getSimulinkAddress();

		/*
		 * The local port and remote address for the link layer. The link layer
		 * can either run on the same machine or a separate one.
		 */
		@Option
		int getLocalPortForUdpLinkLayer();

		@Option
		SocketAddressFromString getRemoteAddressForUdpLinkLayer();

		/* Mac address to use when broadcasting. */
		@Option
		MacAddress getMacAddress();

		/* Country code */
		@Option
		int getCountryCode();
	}

	/*
	 * PositionProvider is used by the beaconing service and for creating the
	 * Geobroadcast address used for DENM messages.
	 */
	public static class VehiclePositionProvider implements PositionProvider {
		public Address address;
		public Position position;
		public boolean isPositionConfident;
		public double speedMetersPerSecond;
		public double headingDegreesFromNorth;

		VehiclePositionProvider(Address address) {
			this.address = address;
			this.position = new Position(0, 0);
			this.isPositionConfident = false;
			this.speedMetersPerSecond = 0;
			this.headingDegreesFromNorth = 0;
		}

		/*
		 * TODO: Is the formatting of lat/long the same as in the CAM message?
		 */
		public void updatePosition(double latitude, double longitude) {
			this.position = new Position(latitude, longitude);
			logger.debug("VehiclePositionProvider position updated: {}", this.position);
		}

		public Position getPosition() {
			return position;
		}

		public LongPositionVector getLatestPosition() {
			return new LongPositionVector(address, Instant.now(), position, isPositionConfident, speedMetersPerSecond,
					headingDegreesFromNorth);
		}
	}

	public static void main(String[] args) throws IOException {
		// Parse CLI options
		CliOptions opts = CliFactory.parseArguments(CliOptions.class, args);

		StationConfig config = new StationConfig();
		LinkLayer linkLayer = new LinkLayerUdpToEthernet(opts.getLocalPortForUdpLinkLayer(),
				opts.getRemoteAddressForUdpLinkLayer().asInetSocketAddress(), true);
		simulink_address = InetAddress.getByName(opts.getSimulinkAddress());

		MacAddress senderMac = opts.getMacAddress();

		Address address = new Address(true, // isManual
				net.gcdc.geonetworking.StationType.values()[5], // 5 for
																// passenger car
				opts.getCountryCode(), // countryCode
				senderMac.value()); // lowLevelAddress

		vehiclePositionProvider = new VehiclePositionProvider(address);

		simulink_cam_port = opts.getPortSendCam();
		simulink_denm_port = opts.getPortSendDenm();
		simulink_iclcm_port = opts.getPortSendIclcm();

		/* Create the vehicle adapter. */
		VehicleAdapter va = new VehicleAdapter(opts.getPortRcvFromSimulink(), config, linkLayer,
				vehiclePositionProvider, senderMac);
	}
}
