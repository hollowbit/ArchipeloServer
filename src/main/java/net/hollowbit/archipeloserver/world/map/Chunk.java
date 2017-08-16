package net.hollowbit.archipeloserver.world.map;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.TileData;

public class Chunk {
	
	private int x, y;
	private String[][] tiles;
	private String[][] elements;
	private boolean[][] collisionMap;
	private String collisionData;
	private String overrideCollisionData;
	private Map map;
	
	public Chunk(ChunkData data, Map map) {
		this.map = map;
		
		this.x = data.x;
		this.y = data.y;
		this.tiles = data.tiles;
		this.elements = data.elements;
		for (EntitySnapshot snapshot : data.entities.values())
			map.addEntity(EntityType.createEntityBySnapshot(snapshot, map));
		
		this.collisionData = data.collisionData;
		this.overrideCollisionData = data.overrideCollisionData;
		
		this.collisionMap = new boolean[ChunkData.SIZE * TileData.COLLISION_MAP_SCALE][ChunkData.SIZE * TileData.COLLISION_MAP_SCALE];
		if (data.collisionData != null && !data.collisionData.equals("") && data.overrideCollisionData != null && !data.overrideCollisionData.equals("")) {
			int i = 0;
	        for (int r = 0; r < collisionMap.length; r++) {
	            for (int c = 0; c < collisionMap[0].length; c++) {
	            	byte override = Byte.parseByte("" + data.overrideCollisionData.charAt(i));
	            	if (override == 0) {//Default
	            		this.collisionMap[r][c] = data.collisionData.charAt(i) == '1';
	            	} else if (override == 1) {//Override none
	            		this.collisionMap[r][c] = false;
	            	} else {//Override yes
	            		this.collisionMap[r][c] = true;
	            	}
	            	
	            	i++;
	            }
	        }
		}
	}
	
	/**
	 * Returns the chunk data to be saved to a file
	 * @return
	 */
	public ChunkData getSaveData() {
		ChunkData data = new ChunkData();
		data.x = this.x;
		data.y = this.y;
		data.tiles = this.tiles;
		data.elements = this.elements;
		
		HashMap<String, EntitySnapshot> entities = new HashMap<String, EntitySnapshot>();
		for (Entity entity : map.getEntities()) {
			if (entity.getLocation().getChunkX() == x && entity.getLocation().getChunkY() == y)
				entities.put(entity.getName(), entity.getSaveSnapshot());
		}
		
		data.entities = entities;
		data.collisionData = collisionData;
		data.overrideCollisionData = overrideCollisionData;
		
		return data;
	}
	
	public String getSerializedCollisionData() {
		String collisionData = "";
        for (int r = 0; r < collisionMap.length; r++) {
            for (int c = 0; c < collisionMap[0].length; c++) {
                collisionData += collisionMap[r][c] ? "1" : "0";
            }
        }
        return collisionData;
	}
	
	public Map getMap() {
		return map;
	}
	
	public int getX() {
		return x;
	}
	
	public float getPixelX() {
		return x * ChunkData.SIZE * ArchipeloServer.TILE_SIZE;
	}
	
	public int getY() {
		return y;
	}
	
	public float getPixelY() {
		return y * ChunkData.SIZE * ArchipeloServer.TILE_SIZE;
	}
	
	public String[][] getElements() {
		return elements;
	}
	
	public String[][] getTiles() {
		return tiles;
	}
	
	public boolean[][] getCollisionMap() {
		return collisionMap;
	}
	
}
