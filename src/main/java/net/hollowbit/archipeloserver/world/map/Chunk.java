package net.hollowbit.archipeloserver.world.map;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class Chunk {
	
	private int x, y;
	private String[][] tiles;
	private String[][] elements;
	private Map map;
	
	public Chunk(int x, int y, Map map) {
		super();
		this.x = x;
		this.y = y;
		this.tiles = new String[ChunkData.SIZE][ChunkData.SIZE];
		this.elements = new String[ChunkData.SIZE][ChunkData.SIZE];
	}
	
	public Chunk(ChunkData data, Map map) {
		this.map = map;
		
		this.x = data.x;
		this.y = data.y;
		this.tiles = data.tiles;
		this.elements = data.elements;
		for (EntitySnapshot snapshot : data.entities)
			map.addEntity(EntityType.createEntityBySnapshot(snapshot, map));
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
		return data;
	}
	
	/**
	 * Duplicates the values of the given chunk into this one.
	 * @param chunk
	 */
	public void set(Chunk chunkToCopy) {
		this.x = chunkToCopy.x;
		this.y = chunkToCopy.y;
		
		this.tiles = new String[ChunkData.SIZE][ChunkData.SIZE];
		for (int r = 0; r < ChunkData.SIZE; r++) {
			for (int c = 0; c < ChunkData.SIZE; c++)
				this.tiles[r][c] = chunkToCopy.tiles[r][c];
		}
		
		this.elements = new String[ChunkData.SIZE][ChunkData.SIZE];
		for (int r = 0; r < ChunkData.SIZE; r++) {
			for (int c = 0; c < ChunkData.SIZE; c++)
				this.elements[r][c] = chunkToCopy.elements[r][c];
		}
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
	
}
