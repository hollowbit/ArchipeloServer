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

public class Door extends LifelessEntity {
	
	SavedLocation teleportLocation;
	boolean teleports;
	boolean changesDirection;
	Direction teleportDirection;
	
	private boolean open;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleports = fullSnapshot.getBoolean("teleports", true);
		this.teleportLocation = fullSnapshot.getObject("teleportLocation", new SavedLocation(), SavedLocation.class);
		
		if (teleportLocation.direction == -1)
			changesDirection = false;
		else {
			changesDirection = true;
			this.teleportDirection = Direction.values()[teleportLocation.direction];
		}
		this.closeDoor();
	}
	
	@Override
	public void interactFrom(Entity entity, String yourCollisionRectName, String theirCollisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, yourCollisionRectName, theirCollisionRectName, interactionType);
		switch (yourCollisionRectName) {
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
					
					if (!teleportLocation.map.equals("")) {
						p.teleport(teleportLocation.x, teleportLocation.y, newDirection, teleportLocation.map);
						return;
					}
				}
				entity.teleport(teleportLocation.x, teleportLocation.y, newDirection);
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
		snapshot.putObject("teleportLocation", teleportLocation);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}

}
