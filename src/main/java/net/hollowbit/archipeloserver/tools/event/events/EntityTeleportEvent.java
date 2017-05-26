package net.hollowbit.archipeloserver.tools.event.events;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.tools.event.CancelableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Direction;

public class EntityTeleportEvent extends CancelableEvent {

	private Entity entity;
	private Vector2 newPos, oldPos;
	private Direction oldDirection, newDirection;
	private Map oldMap;
	private String newMap, newIsland;

	public EntityTeleportEvent(Entity entity, Vector2 newPos, Vector2 oldPos, Map oldMap, String newMap, String newIsland, Direction oldDirection, Direction newDirection) {
		super(EventType.EntityTeleport);
		this.entity = entity;
		this.newPos = newPos;
		this.oldPos = oldPos;
		this.oldMap = oldMap;
		this.newMap = newMap;
		this.newIsland = newIsland;
		this.oldDirection = oldDirection;
		this.newDirection = newDirection;
	}

	public Vector2 getNewPos() {
		return newPos;
	}

	public void setNewPos(Vector2 newPos) {
		this.newPos = newPos;
	}

	public String getNewMap() {
		return newMap;
	}

	public void setNewMap(String newMap) {
		this.newMap = newMap;
	}

	public String getNewIsland() {
		return newIsland;
	}

	public void setNewIsland(String newIsland) {
		this.newIsland = newIsland;
	}

	public Direction getNewDirection() {
		return newDirection;
	}

	public void setNewDirection(Direction newDirection) {
		this.newDirection = newDirection;
	}

	public Direction getOldDirection() {
		return oldDirection;
	}

	public Entity getEntity() {
		return entity;
	}

	public Vector2 getOldPos() {
		return oldPos;
	}

	public Map getOldMap() {
		return oldMap;
	}
	
	public boolean isNewIsland() {
		return !oldMap.getIsland().getName().equals(newIsland);
	}
	
	public boolean isNewMap() {
		return !oldMap.getName().equals(newMap) || isNewIsland();
	}

}
