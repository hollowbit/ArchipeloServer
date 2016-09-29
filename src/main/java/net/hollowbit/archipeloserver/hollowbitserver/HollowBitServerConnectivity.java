package net.hollowbit.archipeloserver.hollowbitserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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
	
	public HollowBitServerConnectivity () throws URISyntaxException {
		super(new URI("wss://" + ArchipeloServer.getServer().getConfig().hbServerAddress));
	}
	
	/**
	 * Sends the raw query to HollowBit server and puts the listener in the handlerMap
	 * @param query Query string to send to server
	 */
	private void sendQuery (String query) {
		String uuid = UUID.randomUUID().toString();
		String finalQuery = uuid + "/" + query;
		this.send(finalQuery);
	}
	
	/**
	 * Sends a query to the HollowBit server to add this server to the list
	 */
	public void sendAddServerQuery () {
		Configuration config = ArchipeloServer.getServer().getConfig();
		String query = ADD_SERVER_PACKET_ID + ";" + config.hbServerPassword + ";" + config.name + ";" + config.region + ";0;" + GAME_ID;
		sendQuery(query);
	}
	
	/**
	 * Sends a query to the HollowBit server to remove this server from the list
	 */
	public void sendRemoveServerQuery () {
		String query = REMOVE_SERVER_PACKET_ID + ";" + ArchipeloServer.getServer().getConfig().hbServerPassword;
		sendQuery(query);
	}
	
	/**
	 * Update traffic on server listing in HollowBit server
	 * @param traffic
	 */
	public void sendUpdateServerQuery (int traffic) {
		String query = UPDATE_SERVER_PACKET_ID + ";" + traffic;
		sendQuery(query);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		connected = true;
		ArchipeloServer.getServer().getLogger().info("Connected to HB!");
	}

	@Override
	public void onMessage(String message) {
    	//Don't handle responses
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
