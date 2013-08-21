
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import artnet4j.ArtNet;
import artnet4j.ArtNetException;
import artnet4j.ArtNetNode;
import artnet4j.ArtNetNodeDiscovery;
import artnet4j.events.ArtNetDiscoveryListener;
import artnet4j.packets.ArtDmxPacket;

public class ArtnetDevice implements ArtNetDiscoveryListener {

	protected static final Logger logger = Logger.getLogger(ArtnetDevice.class
			.getName());


	private ArtNetNode connectedNode;
	private ArtNet artnet;
	private int sequenceID;

	public ArtnetDevice() {
		ArtNet.logger.setLevel(Level.WARNING);
		logger.setLevel(Level.INFO);
	}

	@Override
	public void discoveredNewNode(ArtNetNode node) {
		logger.info("new node discovered: " + node);
		connectedNode = node;

	}

	@Override
	public void discoveredNodeDisconnected(ArtNetNode node) {
		logger.info("disconnected: " + node);
		connectedNode = null;
	}

	@Override
	public void discoveryCompleted(List<ArtNetNode> nodes) {

	}

	@Override
	public void discoveryFailed(Throwable e) {
		logger.log(Level.SEVERE, "discovery failed", e);
	}


	public void shutdown() {

	}

	public void start() {
		try {
			artnet = new ArtNet();
			artnet.start();
			artnet.setBroadCastAddress("10.255.255.255");

			ArtNetNodeDiscovery discovery = artnet.getNodeDiscovery();
			discovery.addListener(this);
			discovery.setInterval(1000);
			discovery.start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (ArtNetException e) {
			e.printStackTrace();
		}
	}

	public void allChannelsOn() {

		byte[] buffer = new byte[512];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) 255;
		}

		sendPacket(buffer);


	}

	public void sendValuesForChannels(HashMap<Integer, Integer> channelValues) {
		Set<Entry<Integer, Integer>> s = channelValues.entrySet();


		byte[] buffer = new byte[512];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte)0;
		}
		
		for (Entry<Integer, Integer> entry : s) {
			buffer[entry.getKey()] =  entry.getValue().byteValue();

		}


	}

	public void sendPacket( byte[] data){

		if (connectedNode != null) {

			ArtDmxPacket dmx = new ArtDmxPacket();
			dmx.setUniverse(connectedNode.getSubNet(), connectedNode.getDmxOuts()[0]);
			dmx.setSequenceID(sequenceID % 255);
			dmx.setDMX(data, data.length);
			artnet.unicastPacket(dmx, connectedNode.getIPAddress());
			sequenceID++;

		}    
	}
}

