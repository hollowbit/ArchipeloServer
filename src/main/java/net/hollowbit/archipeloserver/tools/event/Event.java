package net.hollowbit.archipeloserver.tools.event;

import net.hollowbit.archipeloserver.ArchipeloServer;

public abstract class Event {
	
	private EventType type;
	private boolean handled = false;
	
	public Event (EventType type) {
		this.type = type;
	}
	
	/**
	 * Trigger this event
	 */
	public Event trigger () {
		return ArchipeloServer.getServer().getEventManager().triggerEvent(this);
	}

	public EventType getType() {
		return type;
	}
	
	public boolean wasHandled () {
		return handled;
	}
	
	public void setHandled (boolean handled) {
		this.handled = handled;
	}
	
	public boolean wasCanceled () {
		return false;
	}
	
}
