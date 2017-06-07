package net.hollowbit.archipeloserver.tools.event.events.editable;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.tools.event.EditableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;

public class EntityHealEvent extends EditableEvent {
	
	protected float amount;
	protected Entity entity;
	protected Entity healer;
	
	public EntityHealEvent(float amount, Entity entity, Entity healer) {
		super(EventType.EntityHeal);
		this.amount = amount;
		this.entity = entity;
		this.healer = healer;
	}
	
	/**
	 * May be negative if the entity was damaged.
	 * @return
	 */
	public float getAmount() {
		return amount;
	}
	
	/**
	 * May be negative to damage the entity
	 * @param amount
	 */
	public void setAmount(float amount) {
		if (!editingPrevented)
			this.amount = amount;
	}
	
	public Entity getHealer() {
		return healer;
	}

	public void setHealer(Entity healer) {
		if (!editingPrevented)
			this.healer = healer;
	}

	public Entity getEntity() {
		return entity;
	}

}
