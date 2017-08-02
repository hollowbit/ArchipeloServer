package net.hollowbit.archipeloserver.world;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
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
import net.hollowbit.archipeloserver.network.packets.WorldSnapshotPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerCountQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerDataQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerListQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.world.map.Chunk;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.StringValidator;

public class World implements PacketHandler {
	
	public static final int TICKS_PER_DAY = 36000;
	
	private int time;
	private ArrayList<Map> loadedMaps;
	private HashMap<Player, HashSet<Chunk>> playerLoadedChunks;
	private Json json;
	
	public World () {
		time = 0;
		loadedMaps = new ArrayList<Map>();
		json = new Json();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
		
		playerLoadedChunks = new HashMap<Player, HashSet<Chunk>>();
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
		for (Map map : duplicateMapList()) {
			if (!map.isLoaded())
				continue;
			
			HashSet<Chunk> chunksUsed = new HashSet<Chunk>();
			
			String mapSnapshot = json.toJson(map.getChangesSnapshot());
			map.getChangesSnapshot().clear();
			String fullMapSnapshot = json.toJson(map.getFullSnapshot());
			
			HashMap<Integer, HashMap<Integer, String>> chunks = new HashMap<Integer, HashMap<Integer, String>>();
			HashMap<Integer, HashMap<Integer, String>> changesChunks = new HashMap<Integer, HashMap<Integer, String>>();
			HashMap<Integer, HashMap<Integer, String>> fullChunks = new HashMap<Integer, HashMap<Integer, String>>();
			
			for (Player player : map.getPlayers()) {
				long timeCreated = System.currentTimeMillis();
				WorldSnapshotPacket packet = new WorldSnapshotPacket(timeCreated, time, WorldSnapshotPacket.TYPE_INTERP);
				WorldSnapshotPacket changesPacket = new WorldSnapshotPacket(timeCreated, time, WorldSnapshotPacket.TYPE_CHANGES);
				WorldSnapshotPacket fullPacket = null;
				
				changesPacket.mapSnapshot = mapSnapshot;
				
				fullPacket = new WorldSnapshotPacket(timeCreated, time, WorldSnapshotPacket.TYPE_FULL);
				fullPacket.mapSnapshot = fullMapSnapshot;
				
				HashSet<Chunk> chunksForPlayer = new HashSet<Chunk>();
				
				boolean needsFullSnapshot = false;
				
				for (int r = -1 * (WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2); r <= WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2; r++) {
					for (int c = -1 * (WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2); c <= WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2; c++) {
						Chunk chunk = map.loadChunk(c + player.getLocation().getChunkX(), r + player.getLocation().getChunkY());
						//System.out.println("World.java in chunk!  " + (c + player.getLocation().getChunkX()) + "   " + (r + player.getLocation().getChunkY()));
						
						if (chunk != null) {//Could still be null if chunk cannot be loaded
							chunksForPlayer.add(chunk);
							chunksUsed.add(chunk);
							
							//Determine if player needs full chunk data
							boolean hasChunk = false;
							HashSet<Chunk> chunksLoadedByPlayer = playerLoadedChunks.get(player);
							if (chunksLoadedByPlayer != null)
								hasChunk = chunksLoadedByPlayer.contains(chunk);
							
							boolean playerIsNew = player.isNewOnMap() || !hasChunk;
							
							if (playerIsNew)
								needsFullSnapshot = true;
							
							int index = (r + WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2) * WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE + (c + WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2);
							
							HashMap<Integer, String> row = chunks.get(chunk.getY());
							if (row != null && row.containsKey(chunk.getX())) {//Check if chunk has already been loaded
								//Use already loaded chunks
								packet.chunks[index] = row.get(chunk.getX());
								changesPacket.chunks[index] = changesChunks.get(chunk.getY()).get(chunk.getX());
								
								//If new on map and full chunks not loaded, then load them
								if (playerIsNew) {
									HashMap<Integer, String> fullRow = fullChunks.get(chunk.getY());
									if (fullRow != null && fullRow.containsKey(chunk.getX())) {
										fullPacket.chunks[index] = fullRow.get(chunk.getX());
									} else {//Not loaded, so load it
										ChunkData fullData = new ChunkData(chunk.getX(), chunk.getY());
										fullData.tiles = chunk.getTiles();
										fullData.elements = chunk.getElements();
										
										for (Entity entity : map.getEntities()) {
											if (entity.getLocation().getChunkX() == chunk.getX() && entity.getLocation().getChunkY() == chunk.getY()) {
												fullData.entities.put(entity.getName(), entity.getFullSnapshot());
											}
										}
										
										String fullDataText = json.toJson(fullData);
										fullRow = chunks.get(chunk.getY());
										if (fullRow == null) {
											fullRow = new HashMap<Integer, String>();
											fullChunks.put(chunk.getY(), fullRow);
										}
										fullRow.put(chunk.getX(), fullDataText);
										fullPacket.chunks[index] = fullDataText;
									}
								}
							} else {//If not loaded, load them
								ChunkData data = new ChunkData(chunk.getX(), chunk.getY());
								ChunkData changesData = new ChunkData(chunk.getX(), chunk.getY());
								ChunkData fullData = new ChunkData(chunk.getX(), chunk.getY());
								
								ArrayList<EntitySnapshot> changesSnapshots = new ArrayList<EntitySnapshot>();
								
								for (Entity entity : map.getEntities()) {
									if (entity.getLocation().getChunkX() == chunk.getX() && entity.getLocation().getChunkY() == chunk.getY()) {
										data.entities.put(entity.getName(), entity.getInterpSnapshot());
										if (!entity.getChangesSnapshot().isEmpty())
											changesData.entities.put(entity.getName(), entity.getChangesSnapshot());
										changesSnapshots.add(entity.getChangesSnapshot());
										
										if (playerIsNew) {
											fullData.entities.put(entity.getName(), entity.getFullSnapshot());
										}
									}
								}
								
								//Parse data to Strings and save them
								String dataText = json.toJson(data);
								HashMap<Integer, String> rowInterp = chunks.get(chunk.getY());
								if (rowInterp == null) {
									rowInterp = new HashMap<Integer, String>();
									chunks.put(chunk.getY(), rowInterp);
								}
								rowInterp.put(chunk.getX(), dataText);
								packet.chunks[index] = dataText;
								
								String changesDataText = json.toJson(changesData);
								HashMap<Integer, String> rowChanges = chunks.get(chunk.getY());
								if (rowChanges == null) {
									rowChanges = new HashMap<Integer, String>();
									changesChunks.put(chunk.getY(), rowChanges);
								}
								rowChanges.put(chunk.getX(), changesDataText);
								changesPacket.chunks[index] = changesDataText;
								
								if (playerIsNew) {
									fullData.tiles = chunk.getTiles();
									fullData.elements = chunk.getElements();
									fullData.collisionData = chunk.getSerializedCollisionData();
									
									String fullDataText = json.toJson(fullData);
									HashMap<Integer, String> rowFull = chunks.get(chunk.getY());
									if (rowFull == null) {
										rowFull = new HashMap<Integer, String>();
										fullChunks.put(chunk.getY(), rowFull);
									}
									rowFull.put(chunk.getX(), fullDataText);
									fullPacket.chunks[index] = fullDataText;
								}
								
								//Clear changes snapshots
								for (EntitySnapshot snapshot : changesSnapshots)
									snapshot.clear();
							}
						}
					}
				}
				
				//Send compiled packets
				player.sendPacket(packet);
				player.sendPacket(changesPacket);
				
				if (needsFullSnapshot) {
					if (player.isNewOnMap())
						fullPacket.newMap = true;
					player.sendPacket(fullPacket);
					player.setNewOnMap(false);
				}
				
				playerLoadedChunks.put(player, chunksForPlayer);
			}
			
			map.unloadChunksNotInSet(chunksUsed);
		}
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
					loadMap(config.spawnMap);
				}
				mapName = config.spawnMap;
			} else {
				mapName = pd.map;
			}
		} else {
			mapName = pd.map;
		}
		
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
