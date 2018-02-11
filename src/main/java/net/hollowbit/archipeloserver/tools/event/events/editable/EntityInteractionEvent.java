package net.hollowbit.archipeloserver.tools.event.events.editable;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.tools.event.EditableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;

public class EntityInteractionEvent extends EditableEvent {

	private Entity executor;
	private Entity target;
	private String rectNameWith;
	private String rectNameFrom;
	private EntityInteractionType type;
	
	public EntityInteractionEvent(Entity executor, Entity target, String rectNameWith, String rectNameFrom, EntityInteractionType type) {
		super(EventType.EntityInteraction);
		this.executor = executor;
		this.target = target;
		this.rectNameWith = rectNameWith;
		this.rectNameFrom = rectNameFrom;
		this.type = type;
	}

	public Entity getExecutor() {
		return executor;
	}

	public Entity getTarget() {
		return target;
	}

	public String getTargetRectNameWith() {
		return rectNameWith;
	}

	public String getTargetRectNameFrom() {
		return rectNameFrom;
	}

	public EntityInteractionType getInteractionType() {
		return type;
	}
	
}
