package net.hollowbit.archipeloserver.world;

import java.util.ArrayList;
import java.util.Collection;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.LoginPacket;
import net.hollowbit.archipeloserver.network.packets.LogoutPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.DatabaseManager;
import net.hollowbit.archipeloserver.tools.PlayerData;
import net.hollowbit.archipeloshared.StringValidator;

public class World implements PacketHandler {
	
	public static final int TICKS_PER_DAY = 36000;
	
	private int time;
	private ArrayList<Island> loadedIslands;
	
	public World () {
		time = 0;
		loadedIslands = new ArrayList<Island>();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}
	
	public boolean isIslandLoaded (String islandName) {
		for (Island island : duplicateIslandList()) {
			if (island.getName().equalsIgnoreCase(islandName))
				return true;
		}
		return false;
	}
	
	public boolean loadIsland (String islandName) {
		Island island = new Island(islandName, this);
		addIsland(island);
		return island.load();
	}
	
	public boolean unloadIsland (Island island) {
		if (isIslandLoaded(island.getName())) {
			island.unload();
			removeIsland(island);
			return true;
		} else {
			return false;	
		}
	}
	
	public Island getIsland (String islandName) {
		for (Island island : duplicateIslandList()) {
			if (island.getName().equalsIgnoreCase(islandName))
				return island;
		}
		return null;
	}
	
	public void tick20 () {//Executed 20 times per second.
		time++;
		if (time > TICKS_PER_DAY) {//This allows for 30 minute days.
			time = 0;
		}
		
		//Tick all islands
		for (Island island : duplicateIslandList()) {
			island.tick20();
		}
		
		//Create world snapshots and send them
		for (Island island : duplicateIslandList()) {
			for (Map map : island.duplicateMapList()) {
				if (!map.isLoaded())
					continue;
				
				WorldSnapshot snapshot = new WorldSnapshot(this, map, WorldSnapshot.TYPE_INTERP);
				String snapshotPacketString = ArchipeloServer.getServer().getNetworkManager().getPacketString(snapshot.getPacket());
				
				WorldSnapshot changesSnapshot = new WorldSnapshot(this, map, WorldSnapshot.TYPE_CHANGES);
				String changesSnapshotPacketString = ArchipeloServer.getServer().getNetworkManager().getPacketString(changesSnapshot.getPacket());
				
				WorldSnapshot fullSnapshot = null;
				String fullSnapshotPacketString = null;
				
				//Check if there are any new players on map, if so; create a full snapshot for them, otherwise don't even bother making it.
				if (map.isThereNewPlayerOnMap()) {
					fullSnapshot = new WorldSnapshot(this, map, WorldSnapshot.TYPE_FULL);
					fullSnapshotPacketString = ArchipeloServer.getServer().getNetworkManager().getPacketString(fullSnapshot.getPacket());
				}
				
				for (Player player : map.getPlayers()) {
					if (player.isNewOnMap()) {
						ArchipeloServer.getServer().getNetworkManager().sendPacketString(fullSnapshotPacketString, player.getConnection());
						player.setNewOnMap(false);
					} else {
						ArchipeloServer.getServer().getNetworkManager().sendPacketString(snapshotPacketString, player.getConnection());
						ArchipeloServer.getServer().getNetworkManager().sendPacketString(changesSnapshotPacketString, player.getConnection());
					}
				}
				
				//Clears all changes snapshots of all entities so that next time they are reset.
				changesSnapshot.clear();
			}
		}
	}
	
	public void tick60 () {//Executed 60 times per second.
		for (Island island : duplicateIslandList()) {
			island.tick60();
		}
	}
	
	public int getTime () {
		return time;
	}
	
	private synchronized ArrayList<Island> duplicateIslandList () {
		ArrayList<Island> islandList = new ArrayList<Island>();
		islandList.addAll(loadedIslands);
		return islandList;
	}
	
	private synchronized void addIsland (Island island) {
		loadedIslands.add(island);
	}
	
	private synchronized void removeIsland (Island island) {
		loadedIslands.remove(island);
	}
	
	public boolean isPlayerOnline (String name) {
		for (Player player : getOnlinePlayers()) {
			if (player.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public Player getPlayer (String name) {
		for (Player player : getOnlinePlayers()) {
			if (player.getName().equalsIgnoreCase(name))
				return player;
		}
		return null;
	}
	
	public Collection<Player> getOnlinePlayers () {
		ArrayList<Player> onlinePlayers = new ArrayList<Player>();
		for (Island island : loadedIslands) {
			onlinePlayers.addAll(island.getPlayers());
		}
		return onlinePlayers;
	}
	
	public Player getPlayerByAddress (String address) {
		HollowBitUser hollowBitUser = ArchipeloServer.getServer().getNetworkManager().getUserByAddress(address);
		if (hollowBitUser == null)
			return null;
		return hollowBitUser.getPlayer();
	}
	
	@Override
	public boolean handlePacket(Packet packet, String address) {
		switch(packet.packetType) {
		case PacketType.LOGIN:
			//Use a thread to run code asynchronously since this makes database calls that can overall lag the server.
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					LoginPacket loginPacket = (LoginPacket) packet;
					
					//If the client doesn't have the same version as the server, send an error
					if (!loginPacket.version.equals(ArchipeloServer.VERSION)) {
						loginPacket.result = LoginPacket.RESULT_BAD_VERSION;
						loginPacket.version = ArchipeloServer.VERSION;
						loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
						return;
					}
					
					DatabaseManager dbm = ArchipeloServer.getServer().getDatabaseManager();
					Configuration config = ArchipeloServer.getServer().getConfig();
					
					boolean firstTimeLogin = false;
			
					PlayerData playerData = null;		
					if (!loginPacket.registering) {
						playerData = dbm.getPlayerData(loginPacket.username);
						
						//If player data is null, then there is no user with that name
						if (playerData == null) {
							loginPacket.result = LoginPacket.RESULT_NO_USER_WITH_NAME;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Check if passwords match
						if (!ArchipeloServer.getServer().getPasswordHasher().isSamePassword(loginPacket.password, playerData.salt, playerData.hashedPassword)) {
							loginPacket.result = LoginPacket.RESULT_PASSWORD_WRONG;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Check if user is already logged in
						if (isPlayerOnline(loginPacket.username)) {
							loginPacket.result = LoginPacket.RESULT_ALREADY_LOGGED_IN;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						firstTimeLogin = false;
					} else {
						//Check for invalid characters in username and pass
						if (!StringValidator.isStringValid(loginPacket.username, StringValidator.USERNAME)) {
							loginPacket.result = LoginPacket.RESULT_INVALID_USERNAME;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						if (!StringValidator.isStringValid(loginPacket.password, StringValidator.PASSWORD)) {
							loginPacket.result = LoginPacket.RESULT_INVALID_PASSWORD;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Check if username is taken
						if (dbm.doesPlayerExist(loginPacket.username)) {
							loginPacket.result = LoginPacket.RESULT_USERNAME_TAKEN;
							loginPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						playerData = Player.getNewPlayerData(loginPacket.username, loginPacket.password, config);
						firstTimeLogin = true;
					}
					
					Island island = null;
					Map map = null;
					
					String islandName = null;
					String mapName = null;
					
					//If island isn't loaded already, load it
					if (!isIslandLoaded(playerData.island)) {
						if (!loadIsland(playerData.island)) {
							//If island didn't load, send player to (their) spawn
							if (!isIslandLoaded(config.spawnIsland)) {
								loadIsland(config.spawnIsland);
							}
							islandName = config.spawnIsland;
						} else {
							islandName = playerData.island;
						}
					} else {
						islandName = playerData.island;
					}
					
					island = getIsland(islandName);
					
					//If map isn't loaded already, load it
					if (!island.isMapLoaded(playerData.map)) {
						if (!island.loadMap(playerData.map)) {
							//If map didn't load, send player to (their) spawn
							if (!island.isMapLoaded(config.spawnMap)) {
								island.loadMap(config.spawnMap);
							}
							mapName = config.spawnMap;
						} else {
							mapName = playerData.map;
						}
					} else {
						mapName = playerData.map;
					}
					
					map = island.getMap(mapName);
					
					Player player = new Player(playerData.name, address, firstTimeLogin);
					player.load(map, playerData);
					if (firstTimeLogin)
						dbm.createPlayer(player);
					
					map.addEntity(player);
					
					//Login was successful so tell client
					loginPacket.result = LoginPacket.RESULT_SUCCESS;
					loginPacket.hasCreatedPlayer = player.hasCreatedPlayer();//Sends to client whether to enter player creation menu or not
					player.sendPacket(loginPacket);
					
					ArchipeloServer.getServer().getLogger().info("<Join> " + player.getName());
					player.sendPacket(new ChatMessagePacket(ArchipeloServer.getServer().getConfig().motd, "server"));
				}
				
			});
			thread.start();
			return true;
		case PacketType.LOGOUT:
			Thread thread2 = new Thread (new Runnable(){
				@Override
				public void run() {
					LogoutPacket logoutPacket = (LogoutPacket) packet;
					Player player2 = getPlayerByAddress(address);
					logoutPlayer(player2, LogoutReason.get(logoutPacket.reason), logoutPacket.alt);
				}
			});
			thread2.start();
			return true;
		}
		return false;
	}
	
	public void logoutPlayer (Player player, LogoutReason reason, String alt) {
		player.getLocation().getMap().removeEntity(player);
		player.sendPacket(new LogoutPacket(reason.reason, alt));
		
		//Don't remove connection on logoff, since player may join back
		//ArchipeloServer.getServer().getNetworkManager().removeConnection(player.getAddress());
		
		//Build leave message
		StringBuilder leaveMessage = new StringBuilder();
		leaveMessage.append("<Leave> " + player.getName());
		leaveMessage.append(" " + reason.message);
		leaveMessage.append(" " + alt);
		
		ArchipeloServer.getServer().getLogger().info(leaveMessage.toString());
	}
	
}
