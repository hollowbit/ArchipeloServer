package net.hollowbit.archipeloserver.entity;

import net.hollowbit.archipeloshared.EntityAnimationData;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class EntityAnimationManager {
	
	private Entity entity;
	private EntityAnimationData data;
	private String id;
	private float stateTime;
	private String meta;
	private float animationLength;
	
	public EntityAnimationManager (Entity entity, EntitySnapshot fullSnapshot) {
		this(entity, fullSnapshot.getString("anim", entity.getEntityType().getDefaultAnimationId()), fullSnapshot.getString("animMeta", ""), fullSnapshot.getFloat("animTime", 0));
	}
	
	public EntityAnimationManager (Entity entity, String animationId, String animationMeta, float animTime) {
		this.entity = entity;
		if (animationId == null || animationId.equals("") || animationId.equals("null")) {//If there is no animation specified in save, use the default one
			this.id = entity.getEntityType().getDefaultAnimationId();
		} else
			this.id = animationId;
		
		this.data = entity.getEntityType().getAnimationDataById(id);
		this.animationLength = data.totalRuntime;
		this.meta = animationMeta;
		this.stateTime = animTime;
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
	 * Applies the animation to an entity full snapshot
	 * @param interpSnapshot
	 */
	public void applyToEntityFullSnapshot (EntitySnapshot fullSnapshot) {
		fullSnapshot.putString("anim", id);
		fullSnapshot.putString("animMeta", meta);
		fullSnapshot.putFloat("animLength", animationLength);
		fullSnapshot.putFloat("animTime", stateTime);
	}
	
	/**
	 * Will change the animation id without reseting the statetime, meta and custom length.
	 * @param animationId
	 */
	public void changeWithoutReset(String animationId) {
		if (entity.getEntityType().hasAnimation(animationId)) {
			if (!this.id.equals(animationId)) {//Only apply if the animation id changed, otherwise there is no point
				this.id = animationId;
				this.data = entity.getEntityType().getAnimationDataById(animationId);
				applyToEntityChanges(animationId, meta, animationLength, false);
			}
		}
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
		if (entity.getEntityType().hasAnimation(animationId))//Needs to be checked here since we use it below
			this.change(animationId, animationMeta, entity.getEntityType().getAnimationDataById(animationId).totalRuntime);
	}
	
	public void change (String animationId, String animationMeta, float customAnimationLength) {
		//Make sure this entity has this animation
		if (entity.getEntityType().hasAnimation(animationId)) {
			boolean reset = false;
			boolean changed = false;
			if (!animationId.equals(id)) {
				this.stateTime = 0;
				this.id = animationId;
				this.data = entity.getEntityType().getAnimationDataById(animationId);
				reset = true;
				changed = true;
			}
			
			if(!this.meta.equals(animationMeta))
				changed = true;
			this.meta = animationMeta;
			
			if(this.animationLength != customAnimationLength)
				changed = true;
			this.animationLength = customAnimationLength;
			
			if (changed)//Only update client if there was a change made
				applyToEntityChanges(animationId, animationMeta, customAnimationLength, reset);
		}
	}
	
	private void applyToEntityChanges(String animationId, String meta, float animationLength, boolean reset) {
		entity.getChangesSnapshot().putString("anim", animationId);
		entity.getChangesSnapshot().putString("animMeta", meta);
		entity.getChangesSnapshot().putFloat("animLength", animationLength);
		entity.getChangesSnapshot().putBoolean("resetAnim", reset);
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
