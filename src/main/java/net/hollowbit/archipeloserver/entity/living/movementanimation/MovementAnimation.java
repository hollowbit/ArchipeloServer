package net.hollowbit.archipeloserver.entity.living.movementanimation;

import net.hollowbit.archipeloserver.entity.LivingEntity;

public abstract class MovementAnimation {
	
	protected LivingEntity entity;
	
	public MovementAnimation(LivingEntity entity) {
		this.entity = entity;
	}
	
	public abstract void tick60(float deltaTime);
	
	public abstract boolean isExpired();
	
}
