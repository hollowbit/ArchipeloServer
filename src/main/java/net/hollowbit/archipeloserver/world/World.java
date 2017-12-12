package net.hollowbit.archipeloserver.world;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.graphics.Color;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.FlagsAddPacket;
import net.hollowbit.archipeloserver.network.packets.PlayerDeletePacket;
import net.hollowbit.archipeloserver.network.packets.PlayerListPacket;
import net.hollowbit.archipeloserver.network.packets.PlayerPickPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.SnapshotGenerator;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerCountQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerDataQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerListQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.map.Chunk;
import net.hollowbit.archipeloshared.StringValidator;

public class World implements PacketHandler, EventHandler {
	
	public static final int TICKS_PER_DAY = 36000;
	
	private int time;
	private ArrayList<Map> loadedMaps;
	private HashMap<Player, HashSet<Chunk>> playerLoadedChunks;
	private SnapshotGenerator snapshotGenerator;
	
	public World () {
		time = 0;
		loadedMaps = new ArrayList<Map>();
		this.snapshotGenerator = new SnapshotGenerator();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
		
		playerLoadedChunks = new HashMap<Player, HashSet<Chunk>>();
		this.addToEventManager(EventType.PlayerLeave);
	}
	
	public boolean isMapLoaded (String mapName) {
		for (Map map : duplicateMapList()) {
			if (map.getName().equalsIgnoreCase(mapName))
				return true;
		}
		return false;
	}
	
	public boolean loadMap (String mapName) {
		Map map = new Map(mapName, this);
		addMap(map);
		return map.load();
	}
	
	public boolean unloadMap (Map map) {
		if (isMapLoaded(map.getName())) {
			map.unload();
			removeMap(map);
			return true;
		} else {
			return false;	
		}
	}
	
	public Map getMap (String mapName) {
		for (Map map : duplicateMapList()) {
			if (map.getName().equalsIgnoreCase(mapName))
				return map;
		}
		return null;
	}
	
	@Override
	public boolean onPlayerLeave(PlayerLeaveEvent event) {
		playerLoadedChunks.remove(event.getPlayer());//Remove player from chunks map if player has left.
		return EventHandler.super.onPlayerLeave(event);
	}
	
	public void tick20 (float deltaTime) {//Executed 20 times per second.
		time++;
		if (time > TICKS_PER_DAY) {//This allows for 30 minute days.
			time = 0;
		}
		
		//Tick all maps
		for (Map map : duplicateMapList()) {
			map.tick20(deltaTime);
		}
		
		//Create world snapshots and send them
		snapshotGenerator.generateAndSend(this.duplicateMapList(), time);
	}
	
	public void tick60 (float deltaTime) {//Executed 60 times per second.
		for (Map map : duplicateMapList()) {
			map.tick60(deltaTime);
		}
	}
	
	public int getTime () {
		return time;
	}
	
	private synchronized ArrayList<Map> duplicateMapList () {
		ArrayList<Map> mapList = new ArrayList<Map>();
		mapList.addAll(loadedMaps);
		return mapList;
	}
	
	private synchronized void addMap (Map map) {
		loadedMaps.add(map);
	}
	
	private synchronized void removeMap (Map map) {
		loadedMaps.remove(map);
	}
	
	public boolean isPlayerOnline (String name) {
		for (Player player : getOnlinePlayers()) {
			if (player.getName().equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public Player getPlayer (String name) {
		for (Map map : loadedMaps) {
			Player player = map.getEntityManager().getPlayer(name);
			if (player != null)
				return player;
		}
		return null;
	}
	
	public Collection<Player> getOnlinePlayers () {
		ArrayList<Player> onlinePlayers = new ArrayList<Player>();
		for (Map map : loadedMaps) {
			onlinePlayers.addAll(map.getPlayers());
		}
		return onlinePlayers;
	}
	
	public Player getPlayerByAddress (String address) {
		HollowBitUser hollowBitUser = ArchipeloServer.getServer().getNetworkManager().getUser(address);
		if (hollowBitUser == null)
			return null;
		return hollowBitUser.getPlayer();
	}
	
	private void loadPlayerUsingPlayerData (String address, HollowBitUser hbu, PlayerPickPacket playerPickPacket, PlayerData pd, boolean firstTimeLogin) {
		Configuration config = ArchipeloServer.getServer().getConfig();
		Map map = null;
		
		String mapName = null;
		
		//If map isn't loaded already, load it
		if (!isMapLoaded(pd.map)) {
			if (!loadMap(pd.map)) {
				//If map didn't load, send player to (their) spawn
				if (!isMapLoaded(config.spawnMap)) {
					
					if (!loadMap(config.spawnMap)) {
						ArchipeloServer.getServer().getLogger().error("Could not load the spawn map!");
					}
				}
				mapName = config.spawnMap;
				ArchipeloServer.getServer().getLogger().error("A player could not spawn in on the map they were on: " + mapName);
			} else {
				mapName = pd.map;
			}
		} else {
			mapName = pd.map;
		}
		
		for (Map mapN : loadedMaps)
			System.out.println(mapN.getName());
		
		map = getMap(mapName);
		
		Player player = new Player(pd.name, address, firstTimeLogin);
		player.setNewOnMap(true);
		player.load(map, pd, hbu);
		if (firstTimeLogin)
			ArchipeloServer.getServer().getDatabaseManager().createPlayer(player);
		
		//set hollowbit user player to this player
		hbu.setPlayer(player);
		
		map.addEntity(player);
		
		//Login was successful so tell client
		playerPickPacket.result = PlayerPickPacket.RESULT_SUCCESSFUL;
		player.sendPacket(playerPickPacket);
		
		player.sendPacket(new FlagsAddPacket(player.getFlagsManager().getFlagsList()));
		
		//Send messages for login
		ArchipeloServer.getServer().getLogger().broadcastAsServer("<{join}>", player.getName());
		player.sendPacket(new ChatMessagePacket("{serverTag}", ArchipeloServer.getServer().getConfig().motd, "server"));
	}
	
	@Override
	public boolean handlePacket (Packet packet, String address) {
		HollowBitUser hbu;
		switch(packet.packetType) {
		case PacketType.PLAYER_PICK:
			PlayerPickPacket playerPickPacket = (PlayerPickPacket) packet;
			
			if (playerPickPacket.name == null || playerPickPacket.name.equals(""))
				return true;
			hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
			
			if (playerPickPacket.isNew) {
				ArchipeloServer.getServer().getDatabaseManager().getPlayerCount(hbu.getUUID(), new PlayerCountQueryTaskResponseHandler() {
					
					@Override
					public void responseReceived (int playerCount) {
						//Check if user has too many characters
						if (playerCount >= ArchipeloServer.MAX_CHARACTERS_PER_PLAYER) {
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
						
						ArchipeloServer.getServer().getDatabaseManager().doesPlayerExist(playerPickPacket.name, new PlayerExistsQueryTaskResponseHandler() {
							
							@Override
							public void responseReceived(boolean playerExists) {
								//Send error response if name is taken
								if (playerExists) {
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
								
								loadPlayerUsingPlayerData(address, hbu, playerPickPacket, Player.getNewPlayerData(playerPickPacket.name, hbu.getUUID(), hair, face, body), true);
							}
						});
					}
				});
			} else {
				//Check if user is already online
				if (isPlayerOnline(playerPickPacket.name)) {
					playerPickPacket.result = PlayerPickPacket.RESULT_ALREADY_LOGGED_IN;
					playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
					return true;
				}
				ArchipeloServer.getServer().getDatabaseManager().getPlayerData(playerPickPacket.name, hbu.getUUID(), new PlayerDataQueryTaskResponseHandler() {
					
					@Override
					public void responseReceived(PlayerData playerData) {
						//If pd is null, then the player doesn't exist
						if (playerData == null) {
							playerPickPacket.result = PlayerPickPacket.RESULT_NO_PLAYER_WITH_NAME;
							playerPickPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
							return;
						}
						loadPlayerUsingPlayerData(address, hbu, playerPickPacket, playerData, false);
					}
				});
			}
			return true;
		case PacketType.PLAYER_DELETE:
			PlayerDeletePacket playerDeletePacket = (PlayerDeletePacket) packet;
			
			if (playerDeletePacket.name == null || playerDeletePacket.name.equals(""))
				return true;
			
			//Check if user name is valid
			if (!StringValidator.isStringValid(playerDeletePacket.name, StringValidator.USERNAME, StringValidator.MAX_USERNAME_LENGTH)) {
				return true;//Don't bother trying to delete if name is invalid
			}
			
			hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
			ArchipeloServer.getServer().getDatabaseManager().deletePlayer(playerDeletePacket.name, hbu.getUUID());
			return true;
		case PacketType.PLAYER_LIST:
			PlayerListPacket playerListPacket = (PlayerListPacket) packet;
					
			if (playerListPacket.email == null || playerListPacket.email.equals(""))
				return true;
			
			hbu = ArchipeloServer.getServer().getNetworkManager().getUser(address);
			
			//Make sure user is only getting player data from players that belong to them
			if (!playerListPacket.email.equals(hbu.getEmailAddress())) {
				playerListPacket.result = PlayerListPacket.RESULT_INVALID_LOGIN;
				playerListPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
				return true;
			}
					
			//Get player datas from database and parse them into the packet
			ArchipeloServer.getServer().getDatabaseManager().getPlayerListFromUser(hbu.getUUID(), new PlayerListQueryTaskResponseHandler() {
				
				@Override
				public void responseReceived (ArrayList<PlayerData> playerList) {
					playerListPacket.playerEquippedInventories = new Item[playerList.size()][PlayerInventory.DISPLAY_EQUIP_SIZE];
					playerListPacket.names = new String[playerList.size()];
					playerListPacket.islands = new String[playerList.size()];
					playerListPacket.lastPlayedDateTimes = new String[playerList.size()];
					playerListPacket.creationDateTimes = new String[playerList.size()];
					
					DateFormat lastPlayedFormat = new SimpleDateFormat("MMM d, yyyy k:m:s");
					DateFormat createdFormat = new SimpleDateFormat("MMM d, yyyy");
					
					for (int i = 0; i < playerList.size(); i++) {
						playerListPacket.playerEquippedInventories[i] = PlayerInventory.getDisplayInventory(playerList.get(i).uneditableEquippedInventory, playerList.get(i).equippedInventory, playerList.get(i).cosmeticInventory, playerList.get(i).weaponInventory);
						playerListPacket.names[i] = playerList.get(i).name;
						playerListPacket.islands[i] = playerList.get(i).map;
						playerListPacket.lastPlayedDateTimes[i] = lastPlayedFormat.format(playerList.get(i).lastPlayed);
						playerListPacket.creationDateTimes[i] = createdFormat.format(playerList.get(i).creationDate);
					}
							
					//Send packet with player datas
					playerListPacket.send(ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
					
				}
			});
			return true;
		}
		return false;
	}
	
}
