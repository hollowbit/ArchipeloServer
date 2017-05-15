package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.tools.entity.EntityStepOnData;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.events.EntityMoveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Direction;
import net.hollowbit.archipeloshared.EntitySnapshot;

public abstract class LivingEntity extends Entity {
	
	private HashSet<EntityStepOnData> entitiesSteppedOn;
	public static final double DIAGONAL_FACTOR = Math.sqrt(2);
	private float lastSpeed;
	
	@Override
	public void create(String name, int style, Location location, EntityType entityType) {
		super.create(name, style, location, entityType);
		entitiesSteppedOn = new HashSet<EntityStepOnData>();
		lastSpeed = entityType.getSpeed();
	}
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		entitiesSteppedOn = new HashSet<EntityStepOnData>();
		lastSpeed = entityType.getSpeed();
	}
	
	@Override
	public void tick20(float deltaTime) {
		super.tick20(deltaTime);
		for (EntityStepOnData entityStepOnData : duplicateEntitiesStepList()) {
			this.interactWith(entityStepOnData.entity, entityStepOnData.collisionRectName, EntityInteractionType.STEP_CONTINUAL);
		}
		
		//If the speed changes, update it on the clients
		if (this.getSpeed() != lastSpeed) {
			changes.putFloat("speed", this.getSpeed());
			lastSpeed = this.getSpeed();
		}
	}
	
	private synchronized void addEntityToStepList (EntityStepOnData entityStepOnData) {
		entitiesSteppedOn.add(entityStepOnData);
	}
	
	private synchronized void removeAllEntityFromStepList (LinkedList<EntityStepOnData> entityStepOnDatas) {
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
	 * Proper way to move an entity in its current direction.
	 * @param deltaTime
	 * @param checkCollisions
	 * @return
	 */
	public boolean move(float deltaTime, boolean checkCollisions) {
		return this.move(location.direction, deltaTime, checkCollisions);
	}
	
	/**
	 * Proper way to move an entity
	 * @param direction
	 * @param deltaTime
	 * @param checkCollisions
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public boolean move (Direction direction, float deltaTime, boolean checkCollisions) {
		//Calculate new position
		Vector2 newPos = new Vector2(location.pos);
		Vector2 newPosVertical = new Vector2(location.pos);
		switch (direction) {
		case UP:
			newPosVertical.add(0, (float) (deltaTime * getSpeed()));
			break;
		case UP_LEFT:
		case UP_RIGHT:
			newPosVertical.add(0, (float) (deltaTime * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
			break;
			
		case DOWN:
			newPosVertical.add(0, (float) (-deltaTime * getSpeed()));
			break;
		case DOWN_LEFT:
		case DOWN_RIGHT:
			newPosVertical.add(0, (float) (-deltaTime * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
			break;
		}
		
		boolean collidesWithMap = false;
		if (checkCollisions) {
			for (CollisionRect rect : getCollisionRects(newPosVertical)) {//Checks to make sure no collision rect is intersecting with map
				if (location.getMap().collidesWithMap(rect, this)) {
					collidesWithMap = true;
					break;
				}
			}
		}
		
		if(!collidesWithMap || doesCurrentPositionCollideWithMap()) {
			newPos.set(newPosVertical);
		}
		
		Vector2 newPosHorizontal = new Vector2(newPos);
		switch (direction) {
		case LEFT:
			newPosHorizontal.add((float) (-deltaTime * getSpeed()), 0);
			break;
		case UP_LEFT:
		case DOWN_LEFT:
			newPosHorizontal.add((float) (-deltaTime * getSpeed() / LivingEntity.DIAGONAL_FACTOR), 0);
			break;
		case RIGHT:
			newPosHorizontal.add((float) (deltaTime * getSpeed()), 0);
			break;
		case UP_RIGHT:
		case DOWN_RIGHT:
			newPosHorizontal.add((float) (deltaTime * getSpeed() / LivingEntity.DIAGONAL_FACTOR), 0);
			break;
		}
		
		collidesWithMap = false;
		if (checkCollisions) {
			for (CollisionRect rect : getCollisionRects(newPosHorizontal)) {//Checks to make sure no collision rect is intersecting with map
				if (location.getMap().collidesWithMap(rect, this)) {
					collidesWithMap = true;
					break;
				}
			}
		}
		
		if (!collidesWithMap || doesCurrentPositionCollideWithMap()) {
			newPos.set(newPosHorizontal);
		}
		
		//Create event and check
		EntityMoveEvent event = (EntityMoveEvent) new EntityMoveEvent(this, location.pos, newPos).trigger();//trigger move event
		
		if (event.wasCanceled())
			return false;
		
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
					if (thisRect.collidesWith(entityRect)) {
						rectsSteppedOn.add(new EntityStepOnData(entityRect.name, entity));
					}
				}
			}
			
			//See which step on data is new to initiate a STEP_ON event
			for (EntityStepOnData data : rectsSteppedOn) {
				boolean contains = false;
				for (EntityStepOnData data2 : entitiesSteppedOn) {
					if (data.entity == data2.entity && data.collisionRectName.equals(data2.collisionRectName)) {
						contains = true;
						break;
					}
				}
				
				if (!contains) {
					addEntityToStepList(data);
					this.interactWith(data.entity, data.collisionRectName, EntityInteractionType.STEP_ON);
				}
			}
			
			//See which step on data is new to initiate a STEP_OFF event
			LinkedList<EntityStepOnData> entitiesStepOnToRemove = new LinkedList<EntityStepOnData>();
			for (EntityStepOnData data : entitiesSteppedOn) {
				if (data.entity != entity)
					continue;
				
				boolean contains = false;
				for (EntityStepOnData data2 : rectsSteppedOn) {
					if (data.entity == data2.entity && data.collisionRectName.equals(data2.collisionRectName)) {
						contains = true;
						break;
					}
				}
				
				if (!contains || entity.getMap() != this.getMap()) {//If there is an entry for the same entity that is not in rectsSteppedOn, remove it and do a step off event
					entitiesStepOnToRemove.add(data);
					this.interactWith(data.entity, data.collisionRectName, EntityInteractionType.STEP_OFF);
				}
			}
			removeAllEntityFromStepList(entitiesStepOnToRemove);
		}
		return true;
	}
	
	protected boolean doesCurrentPositionCollideWithMap () {
		for (CollisionRect rect : getCollisionRects(location.pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect, this)) {
				return true;
			}
		}
		return false;
	}
	
	public abstract boolean isMoving();
	
	@Override
	public boolean isAlive() {
		return true;
	}
	
	public float getSpeed() {
		return entityType.getSpeed();
	}

}
