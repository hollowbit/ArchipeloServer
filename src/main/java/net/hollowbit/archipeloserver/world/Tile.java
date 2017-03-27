package net.hollowbit.archipeloserver.world;

import net.hollowbit.archipeloshared.TileData;

public class Tile {
	
	private String id;
	private String name;
	private float speedMultiplier;
	private boolean swimmable;
	private String footstepSound;
	private boolean[][] collisionTable;
	
	public Tile (TileData tileData) {
		this.id = tileData.id;
		this.name = tileData.name;
		this.speedMultiplier = tileData.speedMultiplier;
		this.swimmable = tileData.swimmable;
		this.footstepSound = tileData.footstepSound;
		this.collisionTable = tileData.collisionTable;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getSpeedMultiplier() {
		return speedMultiplier;
	}
	public boolean isSwimmable() {
		return swimmable;
	}

	public boolean[][] getCollisionTable() {
		return collisionTable;
	}

	public String getFootstepSound() {
		return footstepSound;
	}

}
