package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class Computer extends LifelessEntity implements EventHandler {
	
	protected boolean on;
	protected Player user;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.on = false;
		this.user = null;
		this.addToEventManager(EventType.PlayerLeave);
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteractionType interactionType) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (interactionType == EntityInteractionType.HIT) {
				if (!on)
					turnOn(p);
				else
					turnOff();
			}
		}
		super.interactFrom(entity, collisionRectName, interactionType);
	}
	
	@Override
	public void remove(){
		super.remove();
		this.removeFromEventManager();
	}
	
	@Override
	public boolean onEntityMove(EntityMoveEvent event) {
		if (user == null)
			return false;
		
		if (event.getEntity().equals(user)) {
			turnOff();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onPlayerLeave(PlayerLeaveEvent event) {
		if (event.getPlayer() == user) {
			turnOff();
			return true;
		}
		return false;
	}
	
	public void turnOn (Player user) {
		this.animationManager.change("on");
		this.user.setMovementEnabled(false);
		this.user = user;
		this.on = true;
		this.changes.putBoolean("on", on);
	}
	
	public void turnOff () {
		this.animationManager.change("off");
		this.user.setMovementEnabled(true);
		this.user = null;
		this.on = false;
		this.changes.putBoolean("on", on);
	}

	@Override
	public EntityAnimationObject animationCompleted (String animationId) {
		return null;
	}
	
}
