package net.hollowbit.archipeloserver.tools.event;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.tools.event.events.*;

public class EventManager {
	
	private ArrayList<EventHandler> eventHandlers;
	
	public EventManager () {
		eventHandlers = new ArrayList<EventHandler>();
	}
	
	/**
	 * DO NOT CALL DIRECTLY, call EventHandler.add() instead.
	 * Add an event handler to manager so it can handle events
	 * @param eventHandler
	 */
	public void add (EventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}
	
	/**
	 * DO NOT CALL DIRECTLY, call EventHandler.remove() instead.
	 * Remove an event handler from the manager to release it from memory.
	 * @param eventHandler
	 */
	public void remove (EventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
	}
	
	/**
	 * Trigger an event to be handled by the handlers.
	 * @param event
	 */
	public Event triggerEvent (Event event) {
		boolean handled = false;
		for (EventHandler eventHandler : eventHandlers) {
			switch (event.getType()) {
			case EntityMove:
				if (eventHandler.onEntityMove((EntityMoveEvent) event))
					handled = true;
				break;
			case PlayerJoin:
				if (eventHandler.onPlayerJoin((PlayerJoinEvent) event))
					handled = true;
				break;
			case PlayerLeave:
				if (eventHandler.onPlayerLeave((PlayerLeaveEvent) event))
					handled = true;
				break;
			case EntityTeleport:
				if (eventHandler.onEntityTeleport((EntityTeleportEvent) event))
					handled = true;
				break;
			}
		}
		event.setHandled(handled);
		return event;
	}
	
}
