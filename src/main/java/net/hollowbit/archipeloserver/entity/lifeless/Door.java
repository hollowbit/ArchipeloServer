package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Direction;

public class Door extends LifelessEntity {
	
	float teleportX = 0, teleportY = 0;
	String teleportIsland, teleportMap;
	Direction teleportDirection;
	boolean teleports;
	boolean changesDirection;
	
	private boolean open;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleports = fullSnapshot.getBoolean("teleports", true);
		this.teleportX = fullSnapshot.getFloat("teleportX", 0);
		this.teleportY = fullSnapshot.getFloat("teleportY", 0);
		this.teleportIsland = fullSnapshot.getString("teleportIsland", "");
		this.teleportMap = fullSnapshot.getString("teleportMap", "");
		int direction = fullSnapshot.getInt("teleportDirection", -1);
		if (direction == -1)
			changesDirection = false;
		else {
			changesDirection = true;
			this.teleportDirection = Direction.values()[direction];
		}
		this.closeDoor();
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		switch (collisionRectName) {
		case "bottom":
			if (interactionType == EntityInteractionType.STEP_ON)
				openDoor();
			else if (interactionType == EntityInteractionType.STEP_OFF)
				closeDoor();
			break;
		case "top":
			if (!teleports)//If this door doesn't teleport, don't run teleport logic
				return;
			
			if (interactionType == EntityInteractionType.STEP_ON) {
				Direction newDirection = teleportDirection;
				if (!changesDirection)
					newDirection = entity.getLocation().getDirection();
				
				if (entity.isPlayer()) {//don't allow intermap teleportation with non-players
					Player p = (Player) entity;
					
					if (!teleportIsland.equals("")) {
						p.teleport(teleportX, teleportY, newDirection, teleportMap, teleportIsland);
						return;
					} else if (!teleportMap.equals("")) {
						p.teleport(teleportX, teleportY, newDirection, teleportMap);
						return;
					}
				}
				entity.teleport(teleportX, teleportY, newDirection);
			}
			break;
		}
	}
	
	public void openDoor () {
		if (!open)
			changes.putBoolean("open", true);
		this.open = true;
		this.animationManager.change("open");
	}
	
	public void closeDoor () {
		if (open)
			changes.putBoolean("open", false);
		this.open = true;
		this.animationManager.change("closed");
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putBoolean("open", open);
		return snapshot;
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putBoolean("teleports", teleports);
		snapshot.putFloat("teleportX", teleportX);
		snapshot.putFloat("teleportY", teleportY);
		snapshot.putString("teleportIsland", teleportIsland);
		snapshot.putString("teleportMap", teleportMap);
		if (changesDirection)
			snapshot.putInt("teleportDirection", teleportDirection.ordinal());
		else
			snapshot.putInt("teleportDirection", -1);
		snapshot.putBoolean("open", open);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}

}
