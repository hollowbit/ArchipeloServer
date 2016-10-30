package net.hollowbit.archipeloserver.world;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.graphics.Color;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.LogoutPacket;
import net.hollowbit.archipeloserver.network.packets.PlayerDeletePacket;
import net.hollowbit.archipeloserver.network.packets.PlayerListPacket;
import net.hollowbit.archipeloserver.network.packets.PlayerPickPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
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
		HollowBitUser hollowBitUser = ArchipeloServer.getServer().getNetworkManager().getUser(address);
		if (hollowBitUser == null)
			return null;
		return hollowBitUser.getPlayer();
	}
	
	@Override
	public boolean handlePacket (Packet packet, String address) {
		switch(packet.packetType) {
		case PacketType.PLAYER_PICK:
			//Use a thread to run code asynchronously since this makes database calls that can overall lag the server.
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					PlayerPickPacket playerPickPacket = (PlayerPickPacket) packet;
					Configuration config = ArchipeloServer.getServer().getConfig();
					
					HollowBitUser hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
					
					boolean firstTimeLogin = false;
					PlayerData pd;
					if (playerPickPacket.isNew) {
						//Check if user has too many characters
						if (ArchipeloServer.getServer().getDatabaseManager().getPlayerCount(hbu.getUUID()) >= ArchipeloServer.MAX_CHARACTERS_PER_PLAYER) {
							playerPickPacket.result = PlayerPickPacket.RESULT_TOO_MANY_CHARACTERS;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Check if user name is valid
						if (!StringValidator.isStringValid(playerPickPacket.name, StringValidator.USERNAME, StringValidator.MAX_USERNAME_LENGTH)) {
							playerPickPacket.result = PlayerPickPacket.RESULT_INVALID_USERNAME;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Send error response if name is taken
						if (ArchipeloServer.getServer().getDatabaseManager().doesPlayerExist(playerPickPacket.name)) {
							playerPickPacket.result = PlayerPickPacket.RESULT_NAME_ALREADY_TAKEN;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						Item body = new Item(ItemType.BODY);
						body.color = Color.rgba8888(PlayerPickPacket.BODY_COLORS[playerPickPacket.bodyColor]);
						Item hair = new Item(PlayerPickPacket.HAIR_STYLES[playerPickPacket.selectedHair]);
						hair.color = Color.rgba8888(PlayerPickPacket.HAIR_COLORS[playerPickPacket.hairColor]);
						Item face = new Item(PlayerPickPacket.FACE_STYLES[playerPickPacket.selectedFace]);
						face.color = Color.rgba8888(PlayerPickPacket.EYE_COLORS[playerPickPacket.eyeColor]);
						pd = Player.getNewPlayerData(playerPickPacket.name, hbu.getUUID(), hair, face, body);
						firstTimeLogin = true;
					} else {
						pd = ArchipeloServer.getServer().getDatabaseManager().getPlayerData(playerPickPacket.name);
						
						//If pd is null, then the player doesn't exist
						if (pd == null) {
							playerPickPacket.result = PlayerPickPacket.RESULT_NO_PLAYER_WITH_NAME;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//If the selected player does not belong to this user, don't allow the user to use it.
						if (!pd.bhUuid.equals(hbu.getUUID())) {
							playerPickPacket.result = PlayerPickPacket.RESULT_PLAYER_BELONGS_TO_ANOTHER_HBU;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						
						//Check if user is already online
						if (isPlayerOnline(playerPickPacket.name)) {
							playerPickPacket.result = PlayerPickPacket.RESULT_ALREADY_LOGGED_IN;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
					}
					
					Island island = null;
					Map map = null;
					
					String islandName = null;
					String mapName = null;
					
					//If island isn't loaded already, load it
					if (!isIslandLoaded(pd.island)) {
						if (!loadIsland(pd.island)) {
							//If island didn't load, send player to (their) spawn
							if (!isIslandLoaded(config.spawnIsland)) {
								loadIsland(config.spawnIsland);
							}
							islandName = config.spawnIsland;
						} else {
							islandName = pd.island;
						}
					} else {
						islandName = pd.island;
					}
					
					island = getIsland(islandName);
					
					//If map isn't loaded already, load it
					if (!island.isMapLoaded(pd.map)) {
						if (!island.loadMap(pd.map)) {
							//If map didn't load, send player to (their) spawn
							if (!island.isMapLoaded(config.spawnMap)) {
								island.loadMap(config.spawnMap);
							}
							mapName = config.spawnMap;
						} else {
							mapName = pd.map;
						}
					} else {
						mapName = pd.map;
					}
					
					map = island.getMap(mapName);
					
					Player player = new Player(pd.name, address, firstTimeLogin);
					player.load(map, pd, hbu);
					if (firstTimeLogin)
						ArchipeloServer.getServer().getDatabaseManager().createPlayer(player);
					
					//set hollowbit user player to this player
					hbu.setPlayer(player);
					
					map.addEntity(player);
					
					//Login was successful so tell client
					playerPickPacket.result = PlayerPickPacket.RESULT_SUCCESSFUL;
					player.sendPacket(playerPickPacket);
					
					//Send messages for login
					ArchipeloServer.getServer().getLogger().info("<Join> " + player.getName());
					player.sendPacket(new ChatMessagePacket(ArchipeloServer.getServer().getConfig().motd, "server"));
				}
				
			});
			thread.start();
			return true;
		case PacketType.PLAYER_DELETE:
			PlayerDeletePacket playerDeletePacket = (PlayerDeletePacket) packet;
			HollowBitUser hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
			ArchipeloServer.getServer().getDatabaseManager().deletePlayer(playerDeletePacket.name, hbu.getUUID());
			return true;
		case PacketType.PLAYER_LIST:
			Thread thread2 = new Thread (new Runnable() {
				@Override
				public void run() {
					PlayerListPacket playerListPacket = (PlayerListPacket) packet;
					
					HollowBitUser hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
					
					//Make sure user is only getting player data from players that belong to them
					if (!playerListPacket.email.equals(hbu.getEmailAddress())) {
						System.out.println("World.java  " + playerListPacket.email + "   " + hbu.getEmailAddress());
						playerListPacket.result = PlayerListPacket.RESULT_INVALID_LOGIN;
						playerListPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
						return;
					}
					
					//Get player datas from database and parse them into the packet
					ArrayList<PlayerData> playerDatas = ArchipeloServer.getServer().getDatabaseManager().getPlayerDataFromUser(hbu.getUUID());
					playerListPacket.playerEquippedInventories = new Item[playerDatas.size()][Player.EQUIP_SIZE];
					playerListPacket.names = new String[playerDatas.size()];
					playerListPacket.islands = new String[playerDatas.size()];
					playerListPacket.lastPlayedDateTimes = new String[playerDatas.size()];
					playerListPacket.creationDateTimes = new String[playerDatas.size()];
					
					DateFormat lastPlayedFormat = new SimpleDateFormat("MMM d, yyyy k:m:s");
					DateFormat createdFormat = new SimpleDateFormat("MMM d, yyyy");
					
					for (int i = 0; i < playerDatas.size(); i++) {
						playerListPacket.playerEquippedInventories[i] = playerDatas.get(i).equippedInventory;
						playerListPacket.names[i] = playerDatas.get(i).name;
						playerListPacket.islands[i] = playerDatas.get(i).island;
						playerListPacket.lastPlayedDateTimes[i] = lastPlayedFormat.format(playerDatas.get(i).lastPlayed);
						playerListPacket.creationDateTimes[i] = createdFormat.format(playerDatas.get(i).creationDate);
					}
					
					//Send packet with player datas
					playerListPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
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
