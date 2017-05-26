package net.hollowbit.archipeloserver.tools.event.events.editable;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.tools.event.EditableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Direction;

public class EntityTeleportEvent extends EditableEvent {

	private Entity entity;
	private Vector2 newPos, oldPos;
	private Direction oldDirection, newDirection;
	private Map oldMap;
	private String newMap, newIsland;

	public EntityTeleportEvent(Entity entity, Vector2 newPos, Vector2 oldPos, Map oldMap, String newMap, String newIsland, Direction oldDirection, Direction newDirection) {
		super(EventType.EntityTeleport);
		this.entity = entity;
		this.newPos = new Vector2(newPos);
		this.oldPos = new Vector2(oldPos);
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
		if (editingPrevented)
			return;
		
		this.newPos = newPos;
	}

	public String getNewMap() {
		return newMap;
	}

	public void setNewMap(String newMap) {
		if (editingPrevented)
			return;
		
		this.newMap = newMap;
	}

	public String getNewIsland() {
		return newIsland;
	}

	public void setNewIsland(String newIsland) {
		if (editingPrevented)
			return;
		
		this.newIsland = newIsland;
	}

	public Direction getNewDirection() {
		return newDirection;
	}

	public void setNewDirection(Direction newDirection) {
		if (editingPrevented)
			return;
		
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
