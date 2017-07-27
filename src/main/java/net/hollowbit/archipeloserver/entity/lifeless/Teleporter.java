package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Direction;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.SavedLocation;

public class Teleporter extends LifelessEntity {
	
	SavedLocation teleportLocation;
	Direction teleportDirection;
	boolean changesDirection;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleportLocation = fullSnapshot.getObject("teleportLocation", null, SavedLocation.class);
		
		if (teleportLocation != null) {
			if (teleportLocation.direction == -1)
				changesDirection = false;
			else {
				changesDirection = true;
				this.teleportDirection = Direction.values()[teleportLocation.direction];
			}
		}
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		if (interactionType == EntityInteractionType.STEP_ON) {
			if (this.teleportLocation == null || !entity.isPlayer())
				return;

			Direction newDirection = teleportDirection;
			if (!changesDirection)
				newDirection = entity.getLocation().getDirection();
			
			if (entity.isPlayer()) {//don't allow inter-map teleportation with non-players
				Player p = (Player) entity;
				
				if (!teleportLocation.map.equals("")) {
					p.teleport(teleportLocation.x, teleportLocation.y, newDirection, teleportLocation.map);
					return;
				}
			}
			entity.teleport(teleportLocation.x, teleportLocation.y, newDirection);
		}
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putObject("teleportLocation", teleportLocation);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}
	
}
