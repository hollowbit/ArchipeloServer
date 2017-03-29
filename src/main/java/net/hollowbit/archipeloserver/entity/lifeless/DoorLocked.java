package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class DoorLocked extends Door {
	
	String unlockFlag;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.unlockFlag = fullSnapshot.getString("unlockFlag", getDefaultUnlockFlag());//Default flag is perfectly valid to use
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		
		switch(collisionRectName) {
		case "bottom":
			if (interactionType == EntityInteractionType.HIT && entity instanceof Player) {
				Player player = (Player) entity;
				if (!player.getFlagsManager().hasFlag(unlockFlag))//If the player hasnt unlocked this door, open the dialog for the player
					player.getNpcDialogManager().sendNpcDialog(this, this.name + "Locked");
			}
			break;
		}
	}
	
	private String getDefaultUnlockFlag () {
		return this.location.map.getIsland().getName() + "-" + this.location.map.getName() + "-" + this.name + "Unlock";
	}
	
	@Override
	public boolean ignoreHardnessOfCollisionRects(Player player, String rectName) {
		//If the player has the needed flag and this is the bottom rect, ignore the collision since it is unlocked
		if (rectName.equalsIgnoreCase("bottom") && player.getFlagsManager().hasFlag(unlockFlag))
			return true;
		else
			return super.ignoreHardnessOfCollisionRects(player, rectName);
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putString("unlockFlag", unlockFlag);
		return snapshot;
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putString("unlockFlag", unlockFlag);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return super.animationCompleted(animationId);
	}
	
}
