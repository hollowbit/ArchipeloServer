package net.hollowbit.archipeloserver.tools;

import net.hollowbit.archipeloserver.entity.Entity;

public class EntityStepOnData {
	
	public String collisionRectName;
	public Entity entity;
	
	public EntityStepOnData (String collisionRectName, Entity entity) {
		this.collisionRectName = collisionRectName;
		this.entity = entity;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EntityStepOnData))
			return false;
		
		EntityStepOnData data = (EntityStepOnData) obj;
		return data.entity.equals(entity) && data.collisionRectName.equals(collisionRectName);
	}
	
}
