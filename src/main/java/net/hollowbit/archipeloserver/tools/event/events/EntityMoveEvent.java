package net.hollowbit.archipeloserver.tools.event.events;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.tools.event.CancelableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;

/**
 * Move event for living entities.
 * @author Nathanael
 *
 */
public class EntityMoveEvent extends CancelableEvent {
	
	private LivingEntity entity;
	private Vector2 oldPos, newPos;

	public EntityMoveEvent(LivingEntity entity, Vector2 oldPos, Vector2 newPos) {
		super(EventType.EntityMove);
		this.entity = entity;
		this.oldPos = oldPos;
		this.newPos = newPos;
	}
	
	public LivingEntity getEntity () {
		return entity;
	}

	public Vector2 getOldPos() {
		return oldPos;
	}

	public Vector2 getNewPos() {
		return newPos;
	}

	public void setNewPos(Vector2 newPos) {
		this.newPos = newPos;
	}
	
}
