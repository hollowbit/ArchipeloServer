package net.hollowbit.archipeloserver.hollowbitserver;

import org.java_websocket.WebSocket;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.packets.LoginPacket;

public class HollowBitUser {
	
	private String uuid;
	private String email;
	private int points;
	private Player player = null;//If the user selected a character, this will no longer be null
	private boolean loggedIn = false;
	private WebSocket conn;
	private volatile boolean pointsUpToDate = true;
	private int ping = 0;
	
	public long timePingSent = System.currentTimeMillis();
	
	public HollowBitUser (WebSocket conn) {
		this.conn = conn;
	}
	
	/**
	 * Logs in this user with the given credentials. Runs asynchronously
	 * @param name
	 * @param password
	 */
	public void login (final String email, String password) {
		final HollowBitUser user = this;
		Thread thread = new Thread(new Runnable() {//Make it runs asynchronously
			public void run() {

				ArchipeloServer.getServer().getHollowBitServerConnectivity().sendGetUserDataQuery(email, password, new HollowBitServerQueryResponseHandler() {
					
					@Override
					public void responseReceived (int id, String[] data) {
						if (id == HollowBitServerConnectivity.USER_DATA_RESPONSE_PACKET_ID) {//Login was successful
							user.email = email;
							user.uuid = data[0];
							user.points = Integer.parseInt(data[1]);
							loggedIn = true;
						}
						
						//Send login response.
						ArchipeloServer.getServer().getNetworkManager().sendPacket(new LoginPacket(loggedIn ? LoginPacket.RESULT_LOGIN_SUCCESSFUL : LoginPacket.RESULT_LOGIN_ERROR), conn);//Send response login packet depending on login result
					}
				});
			};
		});
		thread.start();
	}
	
	/**
	 * Add points to user. You can also remove points by passing a negative number.
	 * @param pointsToAdd
	 */
	public void addPoints (int pointsToAdd) {
		ArchipeloServer.getServer().getHollowBitServerConnectivity().sendAddUserPoints(email, pointsToAdd);
	}
	
	public void logout () {
		if (this.getPlayer() != null)//Remove the player if there is one.
			this.getPlayer().remove(LogoutReason.LEAVE, "");
		this.loggedIn = false;
	}
	
	public String getUUID () {
		return uuid;
	}
	
	public String getEmailAddress () {
		return email;
	}
	
	public int getPing() {
		return ping;
	}

	public void setPing(int ping) {
		this.ping = ping;
	}

	/**
	 * Asynchronously updates points for this user object.
	 * Updates handler when the points are up to date.
	 */
	public void updatePoints (HollowBitUserPointsUpdate handler) {
		HollowBitUser user = this;
		this.pointsUpToDate = false;
		ArchipeloServer.getServer().getHollowBitServerConnectivity().sendGetUserPointsQuery(email, new HollowBitServerQueryResponseHandler() {
			
			@Override
			public void responseReceived(int id, String[] data) {
				if (id == HollowBitServerConnectivity.GET_USER_POINTS_RESPONSE_PACKET) {
					user.points = Integer.parseInt(data[0]);
					user.pointsUpToDate = true;
					handler.pointsUpdated(user.points);
				}
			}
		});
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
	
	public boolean arePointsUpToDate () {
		return this.pointsUpToDate;
	}
	
	/**
	 * Once this account has picked a player, put it here. If the player disconnects, set it to null.
	 * @param player
	 */
	public void setPlayer (Player player) {
		this.player = player;
	}
	
	public WebSocket getConnection () {
		return conn;
	}
	
	public interface HollowBitUserPointsUpdate {
		public abstract void pointsUpdated (int points);
	}
	
}
