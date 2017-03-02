package net.hollowbit.archipeloserver.tools.event.events;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.tools.event.CancelableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;

public class EntityInteractionEvent extends CancelableEvent {

	private Entity executor;
	private Entity target;
	private String rectName;
	private EntityInteractionType type;
	
	public EntityInteractionEvent(Entity executor, Entity target, String rectName, EntityInteractionType type) {
		super(EventType.EntityInteraction);
		this.executor = executor;
		this.target = target;
		this.rectName = rectName;
		this.type = type;
	}

	public Entity getExecutor() {
		return executor;
	}

	public Entity getTarget() {
		return target;
	}

	public String getTargetRectName() {
		return rectName;
	}

	public EntityInteractionType getInteractionType() {
		return type;
	}
	
}
