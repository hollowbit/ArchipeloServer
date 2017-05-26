package net.hollowbit.archipeloserver.tools.event.events.editable;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.tools.event.EditableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;

public class EntityDeathEvent extends EditableEvent {
	
	private Entity entity;
	private Entity killer;
	private float oldHealth;
	private float newHealth;
	private boolean newHealthSet;
	
	public EntityDeathEvent(Entity entity, Entity killer, float oldHealth, float newHealth) {
		super(EventType.EntityDeath);
		this.entity = entity;
		this.killer = killer;
		this.oldHealth = oldHealth;
		this.newHealth = newHealth;
		this.newHealthSet = false;
	}

	public float getNewHealth() {
		return newHealth;
	}
	
	/**
	 * Must be more than 0.
	 * Will only set health if this even is canceled.
	 * @param newHealth
	 */
	public void setNewHealth(float newHealth) {
		if (editingPrevented)
			return;
		
		if (newHealth > 0) {
			this.newHealthSet = true;
			this.newHealth = newHealth;
		}
	}

	public Entity getEntity() {
		return entity;
	}
	
	public boolean hasKiller() {
		return killer != null;
	}
	
	public Entity getKiller() {
		return killer;
	}

	public float getOldHealth() {
		return oldHealth;
	}
	
	public boolean isNewHealthSet() {
		return newHealthSet;
	}

}
