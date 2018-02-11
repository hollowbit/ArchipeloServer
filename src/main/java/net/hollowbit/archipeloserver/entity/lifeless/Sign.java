package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class Sign extends LifelessEntity {
	
	String startMessageId;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.startMessageId = fullSnapshot.getString("startMessageId", "");
	}
	
	@Override
	public void interactFrom(Entity entity, String yourCollisionRectName, String theirCollisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, yourCollisionRectName, theirCollisionRectName, interactionType);
		
		switch(yourCollisionRectName) {
		case "full":
			if (interactionType == EntityInteractionType.HIT && entity instanceof Player) {
				Player player = (Player) entity;
				player.getNpcDialogManager().sendNpcDialog(this, startMessageId);
			}
			break;
		}
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putString("startMessageId", startMessageId);
		return snapshot;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}
	
}
