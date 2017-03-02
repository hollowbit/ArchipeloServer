package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.EntityMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.Map;

public class Computer extends LifelessEntity implements EventHandler {
	
	protected boolean on;
	protected Player user;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.on = false;
		this.user = null;
		ArchipeloServer.getServer().getEventManager().add(this);
	}
	
	@Override
	public void interactFrom(Entity entity, String collisionRectName, EntityInteraction interactionType) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (interactionType == EntityInteraction.HIT) {
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
		ArchipeloServer.getServer().getEventManager().remove(this);
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
		user.stopMovement();
		this.user = user;
		this.on = true;
		this.changes.putBoolean("on", on);
	}
	
	public void turnOff () {
		this.animationManager.change("off");
		this.user = null;
		this.on = false;
		this.changes.putBoolean("on", on);
	}

	@Override
	public EntityAnimationObject animationCompleted (String animationId) {
		return null;
	}
	
}
