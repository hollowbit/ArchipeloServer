package net.hollowbit.archipeloserver.hollowbitserver;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.Configuration;

public class HollowBitServerConnectivity extends WebSocketClient {
	
	private static final int GAME_ID = 0;
	private static final int ADD_SERVER_PACKET_ID = 688;
	private static final int UPDATE_SERVER_PACKET_ID = 773;
	private static final int REMOVE_SERVER_PACKET_ID = 420;
	
	private boolean connected = false;
	private HashMap<String, HollowBitServerQueryResponseHandler> handlerMap;
	
	public HollowBitServerConnectivity () throws URISyntaxException {
		super(new URI("wss://" + ArchipeloServer.getServer().getConfig().hbServerAddress));
		handlerMap = new HashMap<String, HollowBitServerQueryResponseHandler>();//Create map for handlers
		
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
	
			this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends the raw query to HollowBit server and puts the listener in the handlerMap
	 * @param query Query string to send to server
	 */
	private void sendQuery (String query, HollowBitServerQueryResponseHandler handler) {
		String uuid = UUID.randomUUID().toString();
		if (handler != null)
			handlerMap.put(uuid, handler);
		String finalQuery = uuid + "/" + query;
		this.send(finalQuery);
	}
	
	/**
	 * Sends a query to the HollowBit server to add this server to the list
	 */
	public void sendAddServerQuery () {
		Configuration config = ArchipeloServer.getServer().getConfig();
		String query = ADD_SERVER_PACKET_ID + ";" + config.hbServerPassword + ";" + config.name + ";" + config.region + ";0;" + GAME_ID;
		sendQuery(query, null);
	}
	
	/**
	 * Sends a query to the HollowBit server to remove this server from the list
	 */
	public void sendRemoveServerQuery () {
		String query = REMOVE_SERVER_PACKET_ID + ";" + ArchipeloServer.getServer().getConfig().hbServerPassword;
		sendQuery(query, null);
	}
	
	/**
	 * Update traffic on server listing in HollowBit server
	 * @param traffic
	 */
	public void sendUpdateServerQuery (int traffic) {
		String query = UPDATE_SERVER_PACKET_ID + ";" + traffic;
		sendQuery(query, null);
	}
	
	/**
	 * Send query to HollowBit server to see if login credentials are correct.
	 * @param name Name of HollowBit user
	 * @param password Password for user used to authenticate
	 * @param handler Handles response to queries
	 */
	public void sendVerifyQuery (String name, String password, HollowBitServerQueryResponseHandler handler) {
		String query = "2;" + name + ";" + password;
		sendQuery(query, handler);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		connected = true;
		ArchipeloServer.getServer().getLogger().info("Connected to HB!");
	}

	@Override
	public void onMessage(String message) {
		try {
    		String[] splitter = message.split("/");//Split at bracket to seperate uuid
    		String packetId = splitter[0];//Get uuid
    		
    		String[] data = splitter[1].split(";");//split at semi-colon to get data parts
    		int dataId = Integer.parseInt(data[0]);//Set dataid to first entry in data
			
    		String[] newData = new String[1];
    		if (data.length > 1) {//If there is some extra data
    			//Create new data array, basically using data, but with dataId removed
    			newData = new String[data.length - 1];
				for (int i = 0; i < newData.length; i++)
					newData[i] = data[i + 1];
    		} else {//Otherwise set some default data saying there isn't any
    			newData[0] = "No data";
    		}
			
			handlerMap.get(packetId).responseReceived(dataId, newData);//Handle response packet
			handlerMap.remove(packetId);//Remove handler
		} catch (Exception e) {}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		ArchipeloServer.getServer().getLogger().info("Disconnected from HB - status: " + code + ", reason: " + reason);
	}

	@Override
	public void onError(Exception ex) {
		ArchipeloServer.getServer().getLogger().error("Error from HB: " + ex.getMessage());
	}
	
	public boolean isConnected () {
		return connected;
	}
	
}
