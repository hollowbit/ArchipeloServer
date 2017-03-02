package net.hollowbit.archipeloserver.entity;

import net.hollowbit.archipeloshared.EntityAnimationData;

public class EntityAnimationManager {
	
	private Entity entity;
	private EntityAnimationData data;
	private String id;
	private float stateTime;
	private String meta;
	private float animationLength;
	
	public EntityAnimationManager (Entity entity, String animationId, String animationMeta, float stateTime) {
		this.entity = entity;
		
		if (animationId == null || animationId.equals("") || animationId.equals("null")) {//If there is no animation specified in save, use the default one
			this.id = entity.getEntityType().getDefaultAnimationId();
		} else
			this.id = animationId;
		
		this.data = entity.getEntityType().getAnimationDataById(id);
		this.animationLength = data.totalRuntime;
		this.meta = animationMeta;
		this.stateTime = stateTime;
	}
	
	public void update (float deltaTime) {
		stateTime += deltaTime;
		
		//If this animation doesn't loop and is over time limit, call change event on entities to get a new animation to replace it.
		if (data.finiteLength && stateTime > animationLength) {
			EntityAnimationObject newAnim = entity.animationCompleted(id);
			
			//Set new animation if not null
			if (newAnim != null)
				this.change(newAnim.animationId, newAnim.animationMeta);
			else//If null, use the default animation for this entity
				this.change(entity.getEntityType().getDefaultAnimationId());
		}
	}
	
	/**
	 * Applies the animation to an entity snapshot
	 * @param interpSnapshot
	 */
	public void applyToEntitySnapshot (EntitySnapshot interpSnapshot) {
		interpSnapshot.anim = id;
		interpSnapshot.animMeta = meta;
		interpSnapshot.animTime = stateTime;
	}
	
	/**
	 * Change entity's animation.
	 * @param animationId
	 */
	public void change (String animationId) {
		this.change(animationId, "");
	}
	
	/**
	 * Change entity's animation.
	 * @param animationId
	 * @param animationMeta
	 */
	public void change (String animationId, String animationMeta) {
		if (entity.getEntityType().hasAnimation(animationId))
			this.change(animationId, animationMeta, entity.getEntityType().getAnimationDataById(animationId).totalRuntime);
	}
	
	public void change (String animationId, String animationMeta, float customAnimationLength) {
		//Make sure this entity has this animation
		if (entity.getEntityType().hasAnimation(animationId)) {
			this.id = animationId;
			this.data = entity.getEntityType().getAnimationDataById(animationId);
			this.meta = animationMeta;
			this.stateTime = 0;
			this.animationLength = customAnimationLength;
		}
	}
	
	/**
	 * Used to determine if the current animation is an animation of a player using something.
	 * @return
	 */
	public boolean isUseAnimation () {
		return id.equals("use") || id.equals("usewalk") || id.equals("thrust");
	}
	
	public String getAnimationId () {
		return id;
	}
	
	public float getStateTime() {
		return stateTime;
	}

	public String getAnimationMeta() {
		return meta;
	}

	public static class EntityAnimationObject {
		
		public String animationId;
		public String animationMeta;
		
		public EntityAnimationObject(String animationId) {
			this(animationId, "");
		}
		
		public EntityAnimationObject(String animationId, String animationMeta) {
			this.animationId = animationId;
			this.animationMeta = animationMeta;
		}
		
	}
	
}
