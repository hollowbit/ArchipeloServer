package net.hollowbit.archipeloserver.entity.living;

import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.components.MonsterFollowComponent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class Slime extends LivingEntity {
	
	protected MonsterFollowComponent followComponent;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		followComponent = new MonsterFollowComponent(this, fullSnapshot.getInt("activationZoneDistance", MonsterFollowComponent.DEFAULT_ACTIVATION_DISTANCE), fullSnapshot.getInt("deactivationZoneDistance", MonsterFollowComponent.DEFAULT_DEACTIVATION_DISTANCE));
		components.add(followComponent);
	}
	
	@Override
	public void tick60(float deltaTime) {
		super.tick60(deltaTime);
		if (isMoving())
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

}
