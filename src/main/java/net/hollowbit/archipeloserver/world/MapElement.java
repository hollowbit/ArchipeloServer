package net.hollowbit.archipeloserver.world;

import net.hollowbit.archipeloshared.ElementData;

public class MapElement {
	
	protected String id;
	protected String name;
	protected int width;
	protected int height;
	protected boolean[][] collisionTable;
	protected int offsetX, offsetY;
	
	public MapElement(ElementData data) {
		this.id = data.id;
		this.name = data.name;
		this.width = data.width;
		this.height = data.height;
		this.collisionTable = data.collisionTable;
		this.offsetX = data.offsetX;
		this.offsetY = data.offsetY;
	}

	public String getId () {
		return id;
	}

	public String getName () {
		return name;
	}
	
	public int getWidth () {
		return width;
	}
	
	public int getHeight () {
		return height;
	}

	public boolean[][] getCollisionTable() {
		return collisionTable;
	}
	
	public int getOffsetX () {
		return offsetX;
	}
	
	public int getOffsetY (){
		return offsetY;
	}
	
}
