package net.hollowbit.archipeloserver.world;

import net.hollowbit.archipeloshared.TileData;

public class Tile {
	
	private String id;
	private String name;
	private float speedMultiplier;
	private boolean swimmable;
	private boolean[][] collisionTable;
	
	public Tile (TileData tileData) {
		this.setId(tileData.id);
		this.setName(tileData.name);
		this.setSpeedMultiplier(tileData.speedMultiplier);
		this.setSwimmable(tileData.swimmable);
		this.setCollisionTable(tileData.collisionTable);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getSpeedMultiplier() {
		return speedMultiplier;
	}

	public void setSpeedMultiplier(float speedMultiplier) {
		this.speedMultiplier = speedMultiplier;
	}

	public boolean isSwimmable() {
		return swimmable;
	}

	public void setSwimmable(boolean swimmable) {
		this.swimmable = swimmable;
	}

	public boolean[][] getCollisionTable() {
		return collisionTable;
	}

	public void setCollisionTable(boolean[][] collisionTable) {
		this.collisionTable = collisionTable;
	}

}
