package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;

public class BlobbyGrave extends LifelessEntity {
	
	@Override
	public void interactFrom(Entity entity, String yourCollisionRectName, String theirCollisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, yourCollisionRectName, theirCollisionRectName, interactionType);

		if (entity instanceof Player) {
			Player player = (Player) entity;
			
			if (interactionType == EntityInteractionType.STEP_ON)//Crush grave for player if stepped on
				player.getFlagsManager().addFlag("blobbyGraveCrushed");
			else if (interactionType == EntityInteractionType.HIT) {//Activate grave messages if hit
				player.getNpcDialogManager().sendNpcDialog(this, "blobbyGraveStart", "blobby-grave");
			} else if (interactionType == EntityInteractionType.STEP_CONTINUAL) {
				player.heal(-1);
			}
		}
	}

	@Override
	public EntityAnimationObject animationCompleted (String animationId) {
		return null;
	}
	
}
