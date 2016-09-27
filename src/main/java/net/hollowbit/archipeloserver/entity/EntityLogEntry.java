package net.hollowbit.archipeloserver.entity;

public class EntityLogEntry {
	
	public float x, y;
	public float speed;
	public long creationTime;
	
	public EntityLogEntry (float x, float y, float speed) {
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.creationTime = System.currentTimeMillis();
	}
	
}
