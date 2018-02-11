package net.hollowbit.archipeloserver.tools.entity;

import net.hollowbit.archipeloserver.entity.Entity;

public class EntityStepOnData {
	
	public String theirCollisionRectName;
	public String yourCollisionRectName;
	public Entity entity;
	
	public EntityStepOnData (String theirCollisionRectName, String yourCollisionRectName, Entity entity) {
		this.theirCollisionRectName = theirCollisionRectName;
		this.yourCollisionRectName = yourCollisionRectName;
		this.entity = entity;
	}
}
