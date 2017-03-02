package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.tools.entity.EntityStepOnData;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.events.EntityMoveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;

public abstract class LivingEntity extends Entity {
	
	private LinkedList<EntityStepOnData> entitiesSteppedOn;
	public static final double DIAGONAL_FACTOR = Math.sqrt(2);
	
	@Override
	public void create(String name, int style, Location location, EntityType entityType) {
		super.create(name, style, location, entityType);
		entitiesSteppedOn = new LinkedList<EntityStepOnData>();
	}
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		entitiesSteppedOn = new LinkedList<EntityStepOnData>();
	}
	
	@Override
	public void tick20(float deltaTime) {
		super.tick20(deltaTime);
		for (EntityStepOnData entityStepOnData : duplicateEntitiesStepList()) {
			this.interactWith(entityStepOnData.entity, entityStepOnData.collisionRectName, EntityInteraction.STEP_CONTINUAL);
		}
	}
	
	private synchronized void addEntityToStepList (EntityStepOnData entityStepOnData) {
		entitiesSteppedOn.add(entityStepOnData);
	}
	
	private synchronized void removeAllEntityFromStepList (ArrayList<EntityStepOnData> entityStepOnDatas) {
		entitiesSteppedOn.removeAll(entityStepOnDatas);
	}
	
	private synchronized ArrayList<EntityStepOnData> duplicateEntitiesStepList () {
		ArrayList<EntityStepOnData> entitiesStepList = new ArrayList<EntityStepOnData>();
		entitiesStepList.addAll(entitiesSteppedOn);
		return entitiesStepList;
	}
	
	@Override
	public EntitySnapshot getInterpSnapshot() {
		EntitySnapshot snapshot = super.getInterpSnapshot();
		snapshot.putFloat("x", getX());
		snapshot.putFloat("y", getY());
		return snapshot;
	}
	
	/**
	 * Proper way to move an entity
	 * @param newPos
	 */
	public void move (Vector2 newPos) {
		EntityMoveEvent event = (EntityMoveEvent) new EntityMoveEvent(this, location.pos, newPos).trigger();//trigger move event
		
		if (event.wasCanceled())
			return;
		
		newPos = event.getNewPos();//Set new pos with new one from event
		location.set(newPos);
		
		ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) location.getMap().getEntityManager().duplicateEntityList();
		for (Entity entity : entitiesOnMap) {
			if (entity == this)
				continue;
			
			//Check which rects are being stepped on currently
			ArrayList<EntityStepOnData> rectsSteppedOn = new ArrayList<EntityStepOnData>();
			for (CollisionRect entityRect : entity.getCollisionRects()) {
				for (CollisionRect thisRect : this.getCollisionRects()) {
					if (thisRect.collidesWith(entityRect))
						rectsSteppedOn.add(new EntityStepOnData(entityRect.name, entity));
				}
			}
			
			//See which step on data is new to initiate a STEP_ON event
			for (EntityStepOnData data : rectsSteppedOn) {
				if (!entitiesSteppedOn.contains(data)) {
					addEntityToStepList(data);
					this.interactWith(data.entity, data.collisionRectName, EntityInteraction.STEP_ON);
				}
			}
			
			//See which step on data is new to initiate a STEP_OFF event
			ArrayList<EntityStepOnData> entitiesStepOnToRemove = new ArrayList<EntityStepOnData>();
			for (EntityStepOnData data : entitiesSteppedOn) {
				if ((!rectsSteppedOn.contains(data) && data.entity.equals(entity)) || !entity.getMap().equals(this.getMap())) {//If there is an entry for the same entity that is not in rectsSteppedOn, remvoe it and do a step off event
					entitiesStepOnToRemove.add(data);
					this.interactWith(data.entity, data.collisionRectName, EntityInteraction.STEP_OFF);
				}
			}
			removeAllEntityFromStepList(entitiesStepOnToRemove);
		}
	}
	
	@Override
	public boolean isAlive() {
		return true;
	}

}
