package net.hollowbit.archipeloserver.tools.entity;

import net.hollowbit.archipeloserver.entity.Entity;

public class EntityStepOnData {
	
	public String collisionRectName;
	public Entity entity;
	
	public EntityStepOnData (String collisionRectName, Entity entity) {
		this.collisionRectName = collisionRectName;
		this.entity = entity;
	}
	
}
