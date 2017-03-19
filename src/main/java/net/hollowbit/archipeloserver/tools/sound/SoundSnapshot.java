package net.hollowbit.archipeloserver.tools.sound;

import net.hollowbit.archipeloserver.ArchipeloServer;

public class SoundSnapshot {
	
	public int x = 0, y = 0;//tile coordinate to play sound in
	public String id = "";
	public float volume = 1;
	
	public SoundSnapshot (String id, float x, float y) {
		this(id, x, y, 1);
	}
	
	/**
	 * Volume must be from 0 to 1.
	 * @param id
	 * @param x
	 * @param y
	 * @param volume
	 */
	public SoundSnapshot (String id, float x, float y, float volume) {
		if (volume < 0 || volume > 1)
			throw new IllegalArgumentException("Volume must be from 0 to 1.");
		
		this.id = id;
		this.x = getTileCoord(x);
		this.y = getTileCoord(y);
		this.volume = volume;
	}
	
	private int getTileCoord(float pos) {
		return (int) (pos / ArchipeloServer.TILE_SIZE);
	}
	
}
