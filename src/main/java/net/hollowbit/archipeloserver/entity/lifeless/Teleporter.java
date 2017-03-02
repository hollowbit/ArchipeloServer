package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Direction;

public class Teleporter extends LifelessEntity {
	
	float teleportX = 0, teleportY = 0;
	String teleportIsland, teleportMap;
	Direction teleportDirection;
	boolean changesDirection;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleportX = fullSnapshot.getFloat("teleportX", teleportX);
		this.teleportY = fullSnapshot.getFloat("teleportY", teleportY);
		this.teleportIsland = fullSnapshot.getString("teleportIsland", "");
		this.teleportMap = fullSnapshot.getString("teleportMap", "");
		int direction = fullSnapshot.getInt("teleportDirection", -1);
		if (direction == -1)
			changesDirection = false;
		else {
			changesDirection = true;
			this.teleportDirection = Direction.values()[direction];
		}
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		if (interactionType == EntityInteraction.STEP_ON) {

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
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putFloat("teleportX", teleportX);
		snapshot.putFloat("teleportY", teleportY);
		snapshot.putString("teleportIsland", teleportIsland);
		snapshot.putString("teleportMap", teleportMap);
		if (changesDirection)
			snapshot.putInt("teleportDirection", teleportDirection.ordinal());
		else
			snapshot.putInt("teleportDirection", -1);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}
	
}
