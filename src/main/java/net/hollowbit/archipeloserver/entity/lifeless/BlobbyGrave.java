package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;

public class BlobbyGrave extends LifelessEntity {
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);

		if (entity instanceof Player) {
			Player player = (Player) entity;
			
			if (interactionType == EntityInteraction.STEP_ON)//Crush grave for player if stepped on
				player.getFlagsManager().addFlag("blobbyGraveCrushed");
			else if (interactionType == EntityInteraction.HIT) {//Activate grave messages if hit
				player.getNpcDialogManager().sendNpcDialog(this, "blobbyGraveStart", "blobby-grave");
			}
		}
	}

	@Override
	public EntityAnimationObject animationCompleted (String animationId) {
		return null;
	}
	
}
