package net.hollowbit.archipeloserver.entity.components;

import net.hollowbit.archipeloserver.entity.EntityComponent;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.EntityDeathEvent;
import net.hollowbit.archipeloserver.tools.event.events.EntityMoveEvent;
import net.hollowbit.archipeloshared.Direction;

public class MonsterFollowComponent extends EntityComponent implements EventHandler {
	
	public static final int DEFAULT_ACTIVATION_DISTANCE = 100;//pixels
	public static final int DEFAULT_DEACTIVATION_DISTANCE = 150;//pixels
	
	protected LivingEntity livingEntity;
	protected int activationZoneDist;
	protected int deactivationZoneDist;
	protected Player target;
	protected boolean moving;
	
	public MonsterFollowComponent(LivingEntity entity, int activationZoneDistance, int deactivationZoneDistance) {
		super(entity);
		this.livingEntity = entity;
		this.activationZoneDist = activationZoneDistance;
		this.deactivationZoneDist = deactivationZoneDistance;
		this.target = null;
		this.moving = false;
		this.addToEventManager();
		this.scanForTarget();
	}
	
	@Override
	public void remove() {
		this.removeFromEventManager();
		super.remove();
	}
	
	@Override
	public void tick60(float deltaTime) {
		if (target != null) {
			float dX = (entity.getFootX() - target.getFootX());
			float dY = (entity.getFootY() - target.getFootY());
			
			if (dX * dX + dY * dY <= target.getCollisionRects()[0].width) {//Within attack distance, then attack the target
				moving = false;
			} else {
				double angle = Math.toDegrees(Math.atan2(dY, dX));
				
				//Calculate direction to move entity based on angle to player
				if (angle < 22.5 && angle > -22.5) {
					livingEntity.setDirection(Direction.LEFT);
				} else if (angle >= 22.5 && angle < 67.5) {
					livingEntity.setDirection(Direction.DOWN_LEFT);
				} else if (angle >= 67.5 && angle < 112.5) {
					livingEntity.setDirection(Direction.DOWN);
				} else if (angle >= 112.5 && angle < 157.5) {
					livingEntity.setDirection(Direction.DOWN_RIGHT);
				} else if (angle <= -22.5 && angle > -67.5) {
					livingEntity.setDirection(Direction.UP_LEFT);
				} else if (angle <= -67.5 && angle > -112.5) {
					livingEntity.setDirection(Direction.UP);
				} else if (angle <= -112.5 && angle > -157.5) {
					livingEntity.setDirection(Direction.UP_RIGHT);
				} else {
					livingEntity.setDirection(Direction.RIGHT);
				}
				livingEntity.move(deltaTime, true);//Move the entity in that direction
				moving = true;
			}
		} else {
			moving = false;
		}
		super.tick60(deltaTime);
	}
	
	/**
	 * Returns whether this entity is currently moving.
	 * @return
	 */
	public boolean isMoving() {
		return moving;
	}
	
	@Override
	public boolean onEntityMove(EntityMoveEvent event) {
		if (event.getEntity() == target) {
			//Check if target left deactivation distance
			float dX = (entity.getFootX() - event.getEntity().getFootX());
			float dY = (entity.getFootY() - event.getEntity().getFootY());
			if (dX * dX + dY * dY > this.deactivationZoneDist * this.deactivationZoneDist)
				this.target = null;//If so, remove target
			return true;
		} else {
			if (target == null) {//If we don't have a target and entity is a player on the same map
				if (event.getEntity().isPlayer() && event.getEntity().getMap() == livingEntity.getMap()) {
					//Check if within activation distance
					float dX = (entity.getFootX() - event.getEntity().getFootX());
					float dY = (entity.getFootY() - event.getEntity().getFootY());
					if (dX * dX + dY * dY < this.activationZoneDist * this.activationZoneDist)//If within, set as target
						this.target = (Player) event.getEntity();
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() == target) {
			target = null;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the target is within a distance.
	 * Returns false it not within distance of there is no target.
	 * @param distance
	 * @return
	 */
	public boolean isTargetWithinDistance(int distance) {
		if (target == null)
			return false;
		
		float dX = (entity.getFootX() - target.getFootX());
		float dY = (entity.getFootY() - target.getFootY());
		return (dX * dX + dY * dY < distance * distance);
	}
	
	/**
	 * Will heal the target.
	 * @param distance
	 * @param amount
	 */
	public void healTarget(float amount) {
		if (target != null)
			target.heal(amount, entity);
	}
	
	/**
	 * Scans for a new player to follow.
	 */
	protected void scanForTarget() {
		for (Player player : entity.getMap().getPlayers()) {
			float dX = (entity.getFootX() - player.getFootX());
			float dY = (entity.getFootY() - player.getFootY());
			if (dX * dX + dY * dY < this.activationZoneDist * this.activationZoneDist)//If within, set as target
				this.target = player;
		}
	}

}
