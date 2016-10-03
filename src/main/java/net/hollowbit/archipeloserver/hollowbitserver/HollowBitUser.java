package net.hollowbit.archipeloserver.hollowbitserver;

import org.java_websocket.WebSocket;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;

public class HollowBitUser {
	
	private String uuid;
	private String name;
	private String email;
	private int points;
	private Player player = null;//If the user selected a character, this will no longer be null
	private boolean loggedIn = false;
	private WebSocket conn;
	
	public HollowBitUser (WebSocket conn) {
		this.conn = conn;
	}
	
	/**
	 * Logs in this user with the given credentials
	 * @param name
	 * @param password
	 */
	public void login (final String name, String password) {
		final HollowBitUser user = this;
		ArchipeloServer.getServer().getHollowBitServerConnectivity().sendGetUserDataQuery(name, password, new HollowBitServerQueryResponseHandler() {
			
			@Override
			public void responseReceived(int id, String[] data) {
				if (id == 3) {//3 means the login was successful
					user.name = name;
					user.email = data[0];
					user.uuid = data[1];
					user.points = Integer.parseInt(data[2]);
					loggedIn = true;
				} else {
					//Send invalid login response
				}
			}
		});
	}
	
	public String getUUID () {
		return uuid;
	}
	
	public String getName () {
		return name;
	}
	
	public String getEmailAddress () {
		return email;
	}
	
	public int getPoints () {
		return points;
	}
	
	public boolean isLoggedIn () {
		return loggedIn;
	}
	
	public Player getPlayer () {
		return player;
	}
	
	/**
	 * Once this account has picked a player, put it here. If the player disconnects, set it to null.
	 * @param player
	 */
	public void setPlayer (Player player) {
		this.player = player;
	}
	
}
