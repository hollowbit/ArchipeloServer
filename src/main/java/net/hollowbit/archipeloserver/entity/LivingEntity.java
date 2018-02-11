package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.living.movementanimation.MovementAnimation;
import net.hollowbit.archipeloserver.entity.living.movementanimation.MovementAnimationManager;
import net.hollowbit.archipeloserver.tools.entity.EntityStepOnData;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityDeathEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityTeleportEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Direction;
import net.hollowbit.archipeloshared.EntitySnapshot;

public abstract class LivingEntity extends Entity implements EventHandler {
	
	private LinkedList<EntityStepOnData> entitiesSteppedOn;
	public static final float DIAGONAL_FACTOR = (float) Math.sqrt(2);
	private float lastSpeed;
	protected MovementAnimationManager movementAnimationManager;
	
	@Override
	public void create(String name, int style, Location location, EntityType entityType) {
		super.create(name, style, location, entityType);
		entitiesSteppedOn = new LinkedList<EntityStepOnData>();
		lastSpeed = entityType.getSpeed();
		this.movementAnimationManager = new MovementAnimationManager();
		this.addToEventManager(EventType.EntityTeleport, EventType.EntityMove, EventType.EntityDeath);
	}
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		entitiesSteppedOn = new LinkedList<EntityStepOnData>();
		lastSpeed = entityType.getSpeed();
		this.movementAnimationManager = new MovementAnimationManager();
		this.addToEventManager(EventType.EntityTeleport, EventType.EntityMove);
	}
	
	@Override
	public void tick20(float deltaTime) {
		super.tick20(deltaTime);
		for (EntityStepOnData entityStepOnData : duplicateEntitiesStepList()) {
			this.interactWith(entityStepOnData.entity, entityStepOnData.theirCollisionRectName, entityStepOnData.yourCollisionRectName, EntityInteractionType.STEP_CONTINUAL);
		}
		
		//If the speed changes, update it on the clients
		if (this.getSpeed() != lastSpeed) {
			changes.putFloat("speed", this.getSpeed());
			lastSpeed = this.getSpeed();
		}
	}
	
	@Override
	public void tick60(float deltaTime) {
		super.tick60(deltaTime);
		
		//Apply movement animations if any were execute.
		movementAnimationManager.tick60(deltaTime);
	}
	
	/**
	 * Add a movement animation to this entity to be performed.
	 * @param animation
	 */
	public void addMovementAnimation(MovementAnimation animation) {
		this.movementAnimationManager.addAnimation(animation);
	}
	
	private synchronized void addEntityToStepList (EntityStepOnData entityStepOnData) {
		entitiesSteppedOn.add(entityStepOnData);
	}
	
	private synchronized void removeAllEntityFromStepList (LinkedList<EntityStepOnData> entityStepOnDatas) {
		entitiesSteppedOn.removeAll(entityStepOnDatas);
	}
	
	private synchronized void removeEntityFromStepList (EntityStepOnData entityStepOnData) {
		entitiesSteppedOn.remove(entityStepOnData);
	}
	
	private synchronized ArrayList<EntityStepOnData> duplicateEntitiesStepList () {
		ArrayList<EntityStepOnData> entitiesStepList = new ArrayList<EntityStepOnData>();
		entitiesStepList.addAll(entitiesSteppedOn);
		return entitiesStepList;
	}
	
	@Override
	public void remove() {
		this.removeFromEventManager();
		super.remove();
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
	 * Proper way to move an entity using the default speed.
	 * @param direction
	 * @param deltaTime
	 * @param checkCollisions
	 * @param speed
	 * @return
	 */
	public boolean move (Direction direction, float deltaTime, boolean checkCollisions) {
		return this.move(direction, deltaTime, checkCollisions, getSpeed());
	}
	
	/**
	 * Proper way to move an entity
	 * @param direction
	 * @param deltaTime
	 * @param checkCollisions
	 * @param speed
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public boolean move (Direction direction, float deltaTime, boolean checkCollisions, float speed) {
		//Calculate new position
		Vector2 newPos = new Vector2(location.pos);
		Vector2 newPosVertical = new Vector2(location.pos);
		switch (direction) {
		case UP:
			newPosVertical.add(0, (float) (deltaTime * speed));
			break;
		case UP_LEFT:
		case UP_RIGHT:
			newPosVertical.add(0, (float) (deltaTime * speed / LivingEntity.DIAGONAL_FACTOR));
			break;
			
		case DOWN:
			newPosVertical.add(0, (float) (-deltaTime * speed));
			break;
		case DOWN_LEFT:
		case DOWN_RIGHT:
			newPosVertical.add(0, (float) (-deltaTime * speed / LivingEntity.DIAGONAL_FACTOR));
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
			newPosHorizontal.add((float) (-deltaTime * speed), 0);
			break;
		case UP_LEFT:
		case DOWN_LEFT:
			newPosHorizontal.add((float) (-deltaTime * speed / LivingEntity.DIAGONAL_FACTOR), 0);
			break;
		case RIGHT:
			newPosHorizontal.add((float) (deltaTime * speed), 0);
			break;
		case UP_RIGHT:
		case DOWN_RIGHT:
			newPosHorizontal.add((float) (deltaTime * speed / LivingEntity.DIAGONAL_FACTOR), 0);
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
		EntityMoveEvent event = new EntityMoveEvent(this, location.pos, newPos);//trigger move event
		event.trigger();
		if (event.wasCancelled()) {
			event.close();
			return false;
		} else {
			newPos = event.getNewPos();//Set new pos with new one from event
			location.set(newPos);
			event.close();
			return true;
		}
	}
	
	@Override
	public boolean onEntityMove(EntityMoveEvent event) {
		if (event.getEntity() == this && !event.wasCancelled()) {
			//Update step on list of entity
			ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) location.getMap().getEntityManager().duplicateEntityList();
			for (Entity entity : entitiesOnMap) {
				if (entity == this)
					continue;
				
				//Check which rects are being stepped on currently
				ArrayList<EntityStepOnData> rectsSteppedOn = new ArrayList<EntityStepOnData>();
				for (CollisionRect entityRect : entity.getCollisionRects()) {
					for (CollisionRect thisRect : this.getCollisionRects(event.getNewPos())) {
						if (thisRect.collidesWith(entityRect)) {
							rectsSteppedOn.add(new EntityStepOnData(entityRect.name, thisRect.name, entity));
						}
					}
				}
				
				
				//See which step on data is new to initiate a STEP_ON event
				for (EntityStepOnData data : rectsSteppedOn) {
					boolean contains = false;
					for (EntityStepOnData data2 : entitiesSteppedOn) {
						if (data.entity == data2.entity && data.theirCollisionRectName.equals(data2.theirCollisionRectName)) {
							contains = true;
							break;
						}
					}
					
					if (!contains) {
						addEntityToStepList(data);
						this.interactWith(data.entity, data.theirCollisionRectName, data.yourCollisionRectName, EntityInteractionType.STEP_ON);
					}
				}
				
				//See which step on data is not there to initiate a STEP_OFF event
				LinkedList<EntityStepOnData> entitiesStepOnToRemove = new LinkedList<EntityStepOnData>();
				for (EntityStepOnData data : entitiesSteppedOn) {
					if (data.entity != entity)
						continue;
					
					boolean contains = false;
					for (EntityStepOnData data2 : rectsSteppedOn) {
						if (data.entity == data2.entity && data.theirCollisionRectName.equals(data2.theirCollisionRectName)) {
							contains = true;
							break;
						}
					}
					
					if (!contains || entity.getMap() != this.getMap()) {//If there is an entry for the same entity that is not in rectsSteppedOn, remove it and do a step off event
						entitiesStepOnToRemove.add(data);
						this.interactWith(data.entity, data.theirCollisionRectName, data.yourCollisionRectName, EntityInteractionType.STEP_OFF);
					}
				}
				removeAllEntityFromStepList(entitiesStepOnToRemove);
			}
			return true;
		}
		return EventHandler.super.onEntityMove(event);
	}
	
	@Override
	protected void interactFrom(Entity entity, String yourCollisionRectName, String theirCollisionRectName, EntityInteractionType interactionType) {
		super.interactFrom(entity, yourCollisionRectName, theirCollisionRectName, interactionType);
		
		if (interactionType == EntityInteractionType.STEP_OFF) {
			
			//Find the entity step data and remove it since the entity and its collision no longer collides with ours
			for (EntityStepOnData entityStepData : entitiesSteppedOn) {
				if (entity == entityStepData.entity && yourCollisionRectName.equals(entityStepData.yourCollisionRectName) && theirCollisionRectName.equals(entityStepData.theirCollisionRectName)) {
					this.removeEntityFromStepList(entityStepData);
					break;
				}
			}
			
		} else if (interactionType == EntityInteractionType.STEP_ON) {
			
			//Find if there is no entity step data and add it if it doesn't already exist
			boolean found = false;
			for (EntityStepOnData entityStepData : entitiesSteppedOn) {
				if (entity == entityStepData.entity && yourCollisionRectName.equals(entityStepData.yourCollisionRectName) && theirCollisionRectName.equals(entityStepData.theirCollisionRectName)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				this.addEntityToStepList(new EntityStepOnData(theirCollisionRectName, yourCollisionRectName, entity));
			}
		}
	}
	
	@Override
	public boolean onEntityTeleport(EntityTeleportEvent event) {
		if (event.getEntity() == this) {
			//If changing map, clear all entities from step on list
			if (!event.wasCancelled() && event.isNewMap())
				entitiesSteppedOn.clear();
			return true;
		}
		return EventHandler.super.onEntityTeleport(event);
	}
	
	@Override
	public boolean onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() == this) {
			if (!event.wasCancelled())
				movementAnimationManager.clearAll();
			return true;
		}
		return EventHandler.super.onEntityDeath(event);
	}
	
	protected boolean doesCurrentPositionCollideWithMap () {
		/*for (CollisionRect rect : getCollisionRects(location.pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect, this)) {
				return true;
			}
		}*/
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
