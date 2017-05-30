package net.hollowbit.archipeloserver.entity.living.movementanimation.types;

import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.living.movementanimation.MovementAnimation;
import net.hollowbit.archipeloshared.Direction;

public class KnockbackMovementAnimation extends MovementAnimation {
	
	private float distanceTraveled;
	private float maxDistance;
	private float speed;
	private Direction direction;
	
	/**
	 * Create a knockback animation using the opposite of the entity's facing direction.
	 * @param direction
	 * @param distance
	 * @param time Time it should take to travel the distance.
	 */
	public KnockbackMovementAnimation(LivingEntity entity, float distance, float time) {
		super(entity);
		this.distanceTraveled = 0;
		this.direction = entity.getLocation().getDirection().opposite();
		this.maxDistance = distance;
		this.speed = distance / time;
	}
	
	/**
	 * Create a Knockback animation with a given direction.
	 * @param direction
	 * @param distance
	 * @param time Time it should take to travel the distance.
	 */
	public KnockbackMovementAnimation(LivingEntity entity, Direction direction, float distance, float time) {
		super(entity);
		this.distanceTraveled = 0;
		this.direction = direction;
		this.maxDistance = distance;
		this.speed = distance / time;
	}
	
	@Override
	public void tick60(float deltaTime) {
		float addedDistance = deltaTime * speed * (direction.isDiagonal() ? LivingEntity.DIAGONAL_FACTOR : 1);
		distanceTraveled += addedDistance;
		//Calculate new speed so the player doesn't go over max
		if (distanceTraveled > maxDistance)
			this.speed = this.speed * (1 - ((maxDistance - distanceTraveled) / speed));
		
		entity.move(direction, deltaTime, true, speed);
	}

	@Override
	public boolean isExpired() {
		return distanceTraveled >= maxDistance;
	}

}
