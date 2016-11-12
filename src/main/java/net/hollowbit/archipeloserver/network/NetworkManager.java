package net.hollowbit.archipeloserver.network;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.network.packets.LoginPacket;

public class NetworkManager extends WebSocketServer {
	
	private static final int PACKET_LIFESPAN = 5000;//ms
	
	@SuppressWarnings("rawtypes")
	private HashMap<Integer, Class> packetMap;
	private ArrayList<PacketHandler> packetHandlers;
	
	private ArrayList<PacketWrapper> packets;
	
	private HashMap<String, HollowBitUser> users;
	
	Json json;
	
	public NetworkManager (int port) {
		super(new InetSocketAddress(port));
		// load up the key store
		String STORETYPE = "JKS";
		String KEYSTORE = "keystore";
		String STOREPASSWORD = "changeit";
		String KEYPASSWORD = "changeit";
		
		try {
			KeyStore ks = KeyStore.getInstance(STORETYPE);
			File kf = new File(KEYSTORE);
			ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());
	
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYPASSWORD.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
	
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	
			this.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		packetHandlers = new ArrayList<PacketHandler>();
		packets = new ArrayList<PacketWrapper>();
		users = new HashMap<String, HollowBitUser>();
		json = new Json();
		registerPackets();
	}
	
	public void update () {
		ArrayList<PacketWrapper> currentPackets = new ArrayList<PacketWrapper>();
		currentPackets.addAll(packets);
		
		ArrayList<PacketWrapper> packetsToRemove = new ArrayList<PacketWrapper>();
		for (PacketWrapper packetWrapper : currentPackets) {
			ArrayList<PacketHandler> currentPacketHandlers = new ArrayList<PacketHandler>();
			currentPacketHandlers.addAll(packetHandlers);
			
			//Only remove handled packets, keep unhandled ones for the next cycle
			boolean packetHandled = false;
			for (PacketHandler packetHandler : currentPacketHandlers) {
				if (packetHandler.handlePacket(packetWrapper.packet, packetWrapper.address)) {
					packetHandled = true;
				}
			}
			
			if (packetHandled || System.currentTimeMillis() - packetWrapper.time >= PACKET_LIFESPAN) 
				packetsToRemove.add(packetWrapper);
		}
		
		removeAllPackets(packetsToRemove);
	}
	
	private synchronized void removeAllPackets (ArrayList<PacketWrapper> packets) {
		this.packets.removeAll(packets);
	}
	
	public synchronized WebSocket getConnectionByAddress (String address) {
		if (users.containsKey(address))
			return users.get(address).getConnection();
		else 
			return null;
	}
	
	public String getAddress (WebSocket conn) {
		return conn.getRemoteSocketAddress().getAddress().toString() + ";" + conn.getLocalSocketAddress().getAddress().toString() + ";" + conn.getRemoteSocketAddress().getPort();
	}
	
	public synchronized String addUser (WebSocket conn) {
		users.put(getAddress(conn), new HollowBitUser(conn));
		return getAddress(conn);
	}
	
	public synchronized void removeUser (WebSocket conn) {
		users.remove(getAddress(conn));
	}
	
	public synchronized boolean containsUser (String address) {
		return users.containsKey(address);
	}
	
	public synchronized HollowBitUser getUser (String address) {
		return users.get(address);
	}
	
	@Override
	public void onClose (WebSocket conn, int code, String reason, boolean remote) {
		logoutUser(getAddress(conn));
	}

	@Override
	public void onError (WebSocket conn, Exception error) {
		//error.printStackTrace();
		//ArchipeloServer.getServer().getLogger().error(error.getMessage());
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		//Used for ping getter
		if (message.equals("ping")) {
			conn.send("pong");
			return;
		}
		
		String[] packetWrapArray = message.split(";");
		int type = Integer.parseInt(packetWrapArray[0]);
		message = packetWrapArray[1];
		@SuppressWarnings("unchecked")
		Packet packet = (Packet) json.fromJson(packetMap.get(type), message);
		
		//Handle login/logoff packets
		if (packet.packetType == PacketType.LOGIN) {
			LoginPacket loginPacket = (LoginPacket) packet;
			
			if (loginPacket.email == null || loginPacket.email.equals(""))
				return;
			
			if (loginPacket.password == null || loginPacket.password.equals(""))
				return;
			
			if (loginPacket.version == null || loginPacket.version.equals(""))
				return;
			
			//Check if version is valid
			if (!loginPacket.version.equals(ArchipeloServer.VERSION)) {
				loginPacket.version = ArchipeloServer.VERSION;
				loginPacket.result = LoginPacket.RESULT_BAD_VERSION;
				sendPacket(loginPacket, conn);
				return;
			}
			
			//Add user is version is valid
			HollowBitUser hollowBitUser = users.get(getAddress(conn));
			hollowBitUser.login(loginPacket.email, loginPacket.password);
		} else if (packet.packetType == PacketType.LOGOUT)
			logoutUser(getAddress(conn));
		else {
			//Only handle other packets if the user is logged in
			if (containsUser(getAddress(conn)) && getUser(getAddress(conn)).isLoggedIn())
				addPacket(new PacketWrapper(getAddress(conn), packet));
		}
	}
	
	private synchronized void logoutUser (String address) {
		if (users.containsKey(address)) {
			users.get(address).logout();
			users.remove(address);
		}
	}
	
	private synchronized void addPacket (PacketWrapper packetWrapper) {
		packets.add(packetWrapper);
	}
	
	@Override
	public void onOpen (WebSocket conn, ClientHandshake handshake) {
		//ArchipeloServer.getServer().getLogger().info("Connection received!");
		addUser(conn);
	}
	
	public void sendPacket (Packet packet, WebSocket conn) {
		String packetString = null;
		try {
			packetString = json.toJson(packet);
		} catch (Exception e) {
			ArchipeloServer.getServer().getLogger().caution("Could not serialize packet! Type: " + packet.packetType + " Error: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		try {
			conn.send(packet.packetType + ";" + packetString);
		} catch (Exception e) {
			removeConnection(conn);
		}
	}
	
	public String getPacketString (Packet packet) {
		String packetString = null;
		try {
			packetString = json.toJson(packet);
		} catch (Exception e) {
			ArchipeloServer.getServer().getLogger().caution("Could not serialize packet! Type: " + packet.packetType + " Error: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		
		return packet.packetType + ";" + packetString;
	}
	
	public void sendPacketString (String packet, WebSocket conn) {
		try {
			conn.send(packet);
		} catch (Exception e) {
			removeConnection(conn);
		}
	}
	
	private void registerPackets () {
		packetMap = PacketType.registerPackets();
	}
	
	public synchronized void addPacketHandler (PacketHandler packetHandler) {
		packetHandlers.add(packetHandler);
	}
	
	public synchronized void removePacketHandler (PacketHandler packetHandler) {
		packetHandlers.remove(packetHandler);
	}
	
}
