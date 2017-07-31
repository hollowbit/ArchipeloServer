package net.hollowbit.archipeloserver.world.map;

import java.util.ArrayList;

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
	private Map map;
	
	public Chunk(ChunkData data, Map map) {
		this.map = map;
		
		this.x = data.x;
		this.y = data.y;
		this.tiles = data.tiles;
		this.elements = data.elements;
		for (EntitySnapshot snapshot : data.entities)
			map.addEntity(EntityType.createEntityBySnapshot(snapshot, map));
		
		this.collisionMap = new boolean[ChunkData.SIZE * TileData.COLLISION_MAP_SCALE][ChunkData.SIZE * TileData.COLLISION_MAP_SCALE];
		if (data.collisionData != null && !data.collisionData.equals("") && data.overrideCollisionData != null && !data.overrideCollisionData.equals("")) {
			char[] bytes = data.collisionData.toCharArray();
			char[] overrideBytes = data.overrideCollisionData.toCharArray();
			
			int i = 0;
			int i2 = 0;
	        for (int r = 0; r < collisionMap.length; r++) {
	            for (int c = 0; c < collisionMap[0].length; c++) {
	            	byte val = (byte) overrideBytes[i / Byte.SIZE];
	            	int pos = i % Byte.SIZE;
	            	boolean tick1 = ((val >> pos) & 1) == 1;
	            	i++;
	            	
	            	val = (byte) overrideBytes[i / Byte.SIZE];
	            	pos = i % Byte.SIZE;
	            	boolean tick2 = ((val >> pos) & 1) == 1;
	            	i++;
	            	
	            	if (!tick1 && !tick2) {//0
	            		val = (byte) bytes[i2 / Byte.SIZE];
	                	pos = i2 % Byte.SIZE;
	                	collisionMap[r][c] = ((val >> pos) & 1) == 1;
	            	} else if (!tick1 && tick2)//1
	            		collisionMap[r][c] = false;
	            	else if (tick1 && !tick2)//2
	            		collisionMap[r][c] = true;
	                
	                i2++;
	            }
	        }
		}
	}
	
	/**
	 * Returns the chunk data to be saved to a file
	 * @return
	 */
	public ChunkData getData() {
		ChunkData data = new ChunkData();
		data.x = this.x;
		data.y = this.y;
		data.tiles = this.tiles;
		data.elements = this.elements;
		
		ArrayList<EntitySnapshot> entities = new ArrayList<EntitySnapshot>();
		for (Entity entity : map.getEntities()) {
			if (entity.getLocation().getChunkX() == x && entity.getLocation().getChunkY() == y)
				entities.add(entity.getSaveSnapshot());
		}
		
		data.entities = entities;
		
		//Serialize collision data
		String collisionData = "";
        int i = 0;
        byte accum = 0;
        for (int r = 0; r < collisionMap.length; r++) {
            for (int c = 0; c < collisionMap[0].length; c++) {
                if (collisionMap[r][c])
                    accum |= (1 << i);
                else
                	accum &= ~(1 << i);
                    
                i++;
                if (i >= Byte.SIZE) {
                    i = 0;
                    collisionData += (char) accum;
                }
            }
        }
        collisionData += (char) accum;
		
        data.collisionData = collisionData;
		
		return data;
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
