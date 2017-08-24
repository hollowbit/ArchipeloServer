package net.hollowbit.archipeloserver.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.WorldSnapshotPacket;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloserver.world.map.Chunk;
import net.hollowbit.archipeloshared.EntityData;

public class SnapshotGenerator {
	
	private HashMap<Player, HashSet<Chunk>> playerLoadedChunks;
	private Json json;
	
	public SnapshotGenerator() {
		playerLoadedChunks = new HashMap<Player, HashSet<Chunk>>();
		json = new Json();
	}
	
	public void generateAndSend(Collection<Map> maps, int worldTime) {
		for (Map map : maps) {
			if (!map.isLoaded())
				continue;
			
			HashSet<Chunk> chunksUsed = new HashSet<Chunk>();//Used to unload unused chunks at the end
			
			String mapSnapshot = json.toJson(map.getChangesSnapshot());
			map.getChangesSnapshot().clear();
			String fullMapSnapshot = json.toJson(map.getFullSnapshot());
			
			HashMap<Integer, HashMap<Integer, String>> entitySnapshots = new HashMap<Integer, HashMap<Integer, String>>();
			HashMap<Integer, HashMap<Integer, String>> entityChangesSnapshots = new HashMap<Integer, HashMap<Integer, String>>();
			HashMap<Integer, HashMap<Integer, String>> entityFullSnapshots = new HashMap<Integer, HashMap<Integer, String>>();
			
			for (Player player : map.getPlayers()) {
				long timeCreated = System.currentTimeMillis();
				WorldSnapshotPacket packet = new WorldSnapshotPacket(timeCreated, worldTime, WorldSnapshotPacket.TYPE_INTERP);
				WorldSnapshotPacket packetChanges = new WorldSnapshotPacket(timeCreated, worldTime, WorldSnapshotPacket.TYPE_CHANGES);
				WorldSnapshotPacket packetFull = new WorldSnapshotPacket(timeCreated, worldTime, WorldSnapshotPacket.TYPE_FULL);
				
				packetChanges.mapSnapshot = mapSnapshot;
				packetFull.mapSnapshot = fullMapSnapshot;
				
				HashSet<Chunk> chunksForPlayer = new HashSet<Chunk>();
				
				boolean needsFullSnapshot = player.isNewOnMap();
				
				//Loop through all player adjacent chunks
				for (int r = -1 * (WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2); r <= WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2; r++) {
					for (int c = -1 * (WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2); c <= WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2; c++) {
						Chunk chunk = map.loadChunk(c + player.getLocation().getChunkX(), r + player.getLocation().getChunkY());
						
						if (chunk == null)
							continue;//Some chunks simply cannot be loaded because they don't exist
						
						chunksForPlayer.add(chunk);
						chunksUsed.add(chunk);
						
						//Determine if player needs full chunk data
						boolean hasChunk = !player.isNewOnMap() && doesPlayerHaveFullChunkAlready(player, chunk);
						
						int index = (r + WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2) * WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE + (c + WorldSnapshotPacket.NUM_OF_CHUNKS_WIDE / 2);
						
						//Pre-generate list of all entities in the current chunk
						ArrayList<Entity> entitiesInChunk = new ArrayList<Entity>();
						for (Entity entity : map.getEntities()) {
							if (entity.getLocation().getChunkX() == chunk.getX() && entity.getLocation().getChunkY() == chunk.getY())
								entitiesInChunk.add(entity);
						}
						
						if (!hasChunk) {//If player doesn't have the chunk, create and send it the full data
							needsFullSnapshot = true;
							packetFull.chunks[index] = chunk.getGeneratedData();
							
							String entityDataString = getDataAtPosition(chunk.getX(), chunk.getY(), entityFullSnapshots);
							if (entityDataString == null) {//If entity data doesn't already exist for this chunk, generate it
								//Build entity data
								EntityData entityData = new EntityData();
								for (Entity entity : entitiesInChunk) {
									entityData.entities.add(entity.getFullSnapshot());
								}
								entityDataString = json.toJson(entityData);
								putDataAtPosition(entityDataString, chunk.getX(), chunk.getY(), entityFullSnapshots);
							}
							
							packetFull.entities[index] = entityDataString;//Add entity data to snapshot
						} else {
							//Get interp data
							String entityDataString = getDataAtPosition(chunk.getX(), chunk.getY(), entitySnapshots);
							if (entityDataString == null) {//If entity data doesn't already exist for this chunk, generate it
								//Build entity data
								EntityData entityData = new EntityData();
								for (Entity entity : entitiesInChunk) {
									entityData.entities.add(entity.getInterpSnapshot());
								}
								entityDataString = json.toJson(entityData);
								putDataAtPosition(entityDataString, chunk.getX(), chunk.getY(), entitySnapshots);
							}
							packet.entities[index] = entityDataString;
							
							//Get changes data
							entityDataString = getDataAtPosition(chunk.getX(), chunk.getY(), entityChangesSnapshots);
							if (entityDataString == null) {//If entity data doesn't already exist for this chunk, generate it
								//Build entity data
								EntityData entityData = new EntityData();
								for (Entity entity : entitiesInChunk) {
									entityData.entities.add(entity.getChangesSnapshot());
								}
								entityDataString = json.toJson(entityData);
								putDataAtPosition(entityDataString, chunk.getX(), chunk.getY(), entityChangesSnapshots);
							}
							packetChanges.entities[index] = (fullMapSnapshot);
						}
					}
				}
				
				player.sendPacket(packet);
				player.sendPacket(packetChanges);
				
				if (needsFullSnapshot) {
					packetFull.newMap = player.isNewOnMap();
					player.sendPacket(packetFull);
					player.setNewOnMap(false);
				}
				
				playerLoadedChunks.put(player, chunksForPlayer);
			}
			
			map.unloadChunksNotInSet(chunksUsed);
		}
	}
	
	private String getDataAtPosition(int x, int y, HashMap<Integer, HashMap<Integer, String>> map) {
		HashMap<Integer, String> row = map.get(y);
		if (row == null)
			return null;
		
		return row.get(x);
	}
	
	private void putDataAtPosition(String data, int x, int y, HashMap<Integer, HashMap<Integer, String>> map) {
		HashMap<Integer, String> row = map.get(y);
		if (row == null) {
			row = new HashMap<Integer, String>();
			map.put(y, row);
		}
		row.put(x, data);
	}
	
	private boolean doesPlayerHaveFullChunkAlready(Player player, Chunk chunk) {
		HashSet<Chunk> chunksLoadedByPlayer = playerLoadedChunks.get(player);
		if (chunksLoadedByPlayer == null || chunksLoadedByPlayer.isEmpty())
			return false;
		
		return chunksLoadedByPlayer.contains(chunk);
	}
	
}
