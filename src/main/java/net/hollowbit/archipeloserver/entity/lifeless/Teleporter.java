package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;

public class Teleporter extends LifelessEntity {
	
	float teleportX = 0, teleportY = 0;
	String teleportIsland, teleportMap;
	int teleportDirection;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.teleportX = fullSnapshot.getFloat("teleportX", teleportX);
		this.teleportY = fullSnapshot.getFloat("teleportY", teleportY);
		this.teleportIsland = fullSnapshot.getString("teleportIsland", "");
		this.teleportMap = fullSnapshot.getString("teleportMap", "");
		this.teleportDirection = fullSnapshot.getInt("teleportDirection", -1);
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
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
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putFloat("teleportX", teleportX);
		snapshot.putFloat("teleportY", teleportY);
		snapshot.putString("teleportIsland", teleportIsland);
		snapshot.putString("teleportMap", teleportMap);
		snapshot.putInt("teleportDirection", teleportDirection);
		return snapshot;
	}
	
}
