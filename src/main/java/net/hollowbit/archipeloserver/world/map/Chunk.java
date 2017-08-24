package net.hollowbit.archipeloserver.world.map;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.TileData;

public class Chunk {
	
	private int x, y;
	private String[][] tiles;
	private String[][] elements;
	private boolean[][] collisionMap;
	private Map map;
	
	private String chunkData;
	
	public Chunk(String chunkDataString, Map map) {
		this.map = map;
		this.chunkData = chunkDataString;
		
		ChunkData data = StaticTools.getJson().fromJson(ChunkData.class, chunkDataString);
		
		this.x = data.x;
		this.y = data.y;
		this.tiles = data.tiles;
		this.elements = data.elements;
		
		this.collisionMap = new boolean[ChunkData.SIZE * TileData.COLLISION_MAP_SCALE][ChunkData.SIZE * TileData.COLLISION_MAP_SCALE];
		if (data.collisionData != null && !data.collisionData.equals("")) {
			int i = 0;
	        for (int r = 0; r < collisionMap.length; r++) {
	            for (int c = 0; c < collisionMap[0].length; c++) {
	            	collisionMap[r][c] = data.collisionData.charAt(i) == '1';
	            	i++;
	            }
	        }
		}
	}
	
	public String getGeneratedData() {
		return chunkData;
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
