package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;

public class Door extends LifelessEntity {
	
	float teleportX = 0, teleportY = 0;
	String teleportIsland, teleportMap;
	int teleportDirection;
	boolean teleports;
	
	private boolean open;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleports = fullSnapshot.getBoolean("teleports", true);
		this.teleportX = fullSnapshot.getFloat("teleportX", 0);
		this.teleportY = fullSnapshot.getFloat("teleportY", 0);
		this.teleportIsland = fullSnapshot.getString("teleportIsland", "");
		this.teleportMap = fullSnapshot.getString("teleportMap", "");
		this.teleportDirection = fullSnapshot.getInt("teleportDirection", -1);
		this.open = false;
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		switch (collisionRectName) {
		case "bottom":
			boolean wasOpen = open;
			if (interactionType == EntityInteraction.STEP_ON)
				open = true;
			else if (interactionType == EntityInteraction.STEP_OFF)
				open = false;
			
			//If status of door being open has changes, update it on clients
			if (wasOpen != open)
				changes.putBoolean("open", open);
			break;
		case "top":
			if (!teleports)//If this door doesn't teleport, don't run teleport logic
				return;
			
			if (interactionType == EntityInteraction.STEP_ON) {
				if (entity.isPlayer()) {//don't allow intermap teleportation with non-players
					Player p = (Player) entity;
					if (!teleportIsland.equals("")) {
						p.teleport(teleportX, teleportY, teleportDirection, teleportMap, teleportIsland);
						return;
					} else if (!teleportMap.equals("")) {
						p.teleport(teleportX, teleportY, teleportDirection, teleportMap);
						return;
					}
				}
				entity.teleport(teleportX, teleportY, teleportDirection);
			}
			break;
		}
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putBoolean("open", open);
		return snapshot;
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putBoolean("teleports", teleports);
		snapshot.putFloat("teleportX", teleportX);
		snapshot.putFloat("teleportY", teleportY);
		snapshot.putString("teleportIsland", teleportIsland);
		snapshot.putString("teleportMap", teleportMap);
		snapshot.putInt("teleportDirection", teleportDirection);
		snapshot.putBoolean("open", open);
		return snapshot;
	}

}
