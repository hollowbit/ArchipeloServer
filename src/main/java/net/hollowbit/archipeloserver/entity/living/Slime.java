package net.hollowbit.archipeloserver.entity.living;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.components.MonsterFollowComponent;
import net.hollowbit.archipeloserver.entity.living.movementanimation.types.KnockbackMovementAnimation;
import net.hollowbit.archipeloserver.particles.types.EntityChunkParticles;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class Slime extends LivingEntity {
	
	protected MonsterFollowComponent followComponent;
	protected float damage = -10;
	protected float initAttackWait = 0.5f;
	protected float attackInterval = 2;
	protected float timer = attackInterval - initAttackWait;
	protected boolean engaged = false;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		followComponent = new MonsterFollowComponent(this, fullSnapshot.getInt("activationZoneDistance", MonsterFollowComponent.DEFAULT_ACTIVATION_DISTANCE), fullSnapshot.getInt("deactivationZoneDistance", MonsterFollowComponent.DEFAULT_DEACTIVATION_DISTANCE));
		this.damage = -Math.abs(fullSnapshot.getFloat("damage", damage));
		components.add(followComponent);
	}
	
	@Override
	public void tick20(float deltaTime) {
		super.tick20(deltaTime);
	}
	
	@Override
	public void tick60(float deltaTime) {
		super.tick60(deltaTime);
		
		if (followComponent.isTargetWithinDistance(24)) {
			engaged = true;
			timer += deltaTime;
			if (timer >= attackInterval) {
				followComponent.getTarget().addMovementAnimation(new KnockbackMovementAnimation(followComponent.getTarget(), this.getLocation().getDirection(), 16, 0.2f));
				followComponent.healTarget(-10);
				timer -= attackInterval;
			}
		} else {
			engaged = false;
			timer = attackInterval - initAttackWait;
		}
		
		if (isMoving() && !engaged)
			this.animationManager.change("walk");
		else
			this.animationManager.change("default");
	}
	
	@Override
	public boolean isMoving() {
		return followComponent.isMoving();
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}
	
	@Override
	public boolean ignoreHardnessOfCollisionRects(Player player, String rectName) {
		return false;
	}
	
	@Override
	public boolean heal(float amount, Entity healer) {
		boolean dead = super.heal(amount, healer);
		
		if (amount < 0 && healer != null)
			location.map.spawnParticles(new EntityChunkParticles(this, healer.getLocation().getDirection()));
		return dead;
	}

}
