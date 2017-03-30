package net.hollowbit.archipeloserver.entity.living;

import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.LivingEntity;

public class Wizard extends LivingEntity {
	
	@Override
	public boolean isMoving() {
		return false;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}

}
