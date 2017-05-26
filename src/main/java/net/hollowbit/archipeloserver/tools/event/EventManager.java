package net.hollowbit.archipeloserver.tools.event;

import java.util.HashMap;
import java.util.LinkedList;

import net.hollowbit.archipeloserver.tools.event.events.editable.EntityDeathEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityInteractionEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityTeleportEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerBankAddEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryAddEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryRemoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerInventoryChangeEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerJoinEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerStatsChangeEvent;

public class EventManager {
	
	private LinkedList<EventHandler> eventHandlers;
	private HashMap<EventHandler, HashMap<EventType, EventHandlerPriority>> priorityMap;
	
	public EventManager () {
		eventHandlers = new LinkedList<EventHandler>();
		priorityMap = new HashMap<EventHandler, HashMap<EventType, EventHandlerPriority>>();
	}
	
	/**
	 * DO NOT CALL DIRECTLY, call EventHandler.add() instead.
	 * Add an event handler to manager so it can handle events
	 * @param eventHandler
	 */
	public void add (EventHandler eventHandler) {
		eventHandlers.add(eventHandler);
		priorityMap.put(eventHandler, new HashMap<EventType, EventHandlerPriority>());
	}
	
	/**
	 * DO NOT CALL DIRECTLY, call EventHandler.remove() instead.
	 * Remove an event handler from the manager to release it from memory.
	 * @param eventHandler
	 */
	public void remove (EventHandler eventHandler) {
		eventHandlers.remove(eventHandler);
		priorityMap.remove(eventHandler);
	}
	
	/**
	 * DO NOT CALL DIRECTLY, call EventHandler.registerPriority() instead.
	 * This registers the priority an event handler has over a specific event type.
	 * @param eventHandler
	 * @param type
	 * @param priority
	 */
	public void registerPriority (EventHandler eventHandler, EventType type, EventHandlerPriority priority) {
		HashMap<EventType, EventHandlerPriority> handlerPriorities = priorityMap.get(eventHandler);
		if (handlerPriorities != null)
			handlerPriorities.put(type, priority);
	}
	
	/**
	 * Get the priority a handler has for a certain event type.
	 * May return EventHandlerPriority.NONE if the event handler is not registered to handle this type of event.
	 * @param eventHandler
	 * @param type
	 * @return
	 */
	private EventHandlerPriority getPriorityOfHandler (EventHandler eventHandler, EventType type) {
		HashMap<EventType, EventHandlerPriority> handlerPriorities = priorityMap.get(eventHandler);
		
		if (handlerPriorities == null)//This could happen is the event was removed on another thread
			return EventHandlerPriority.NONE;
		
		EventHandlerPriority priority = handlerPriorities.get(type);
		if (priority == null)
			return EventHandlerPriority.NONE;
		else
			return priority;
	}
	
	private LinkedList<EventHandler> cloneHandlerList() {
		LinkedList<EventHandler> handlersClone = new LinkedList<EventHandler>();
		handlersClone.addAll(eventHandlers);
		return handlersClone;
	}
	
	/**
	 * Trigger an event to be handled by the handlers.
	 * @param event
	 * @param editable
	 */
	public Event triggerEvent (Event event, boolean editable) {
		boolean handled = false;
		LinkedList<EventHandler> clonedHandlerList = cloneHandlerList();
		
		EventHandlerPriority priority = null;
		if (editable)
			priority = EventHandlerPriority.HIGHEST_EDITABLE;
		else
			priority = EventHandlerPriority.HIGHEST_READONLY;
		
		//Prevent editing of editable events if editable is false
		if (!editable) {
			if (event instanceof EditableEvent)
				((EditableEvent) event).preventEditing();
		}
		
		while (priority != null) {
			for (EventHandler eventHandler : clonedHandlerList) {
				//Only handle event with this handler if it has the correct priority
				if (getPriorityOfHandler(eventHandler, event.getType()) != priority)
					continue;
				
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
				case PlayerBankAdd:
					if (eventHandler.onPlayerBankAdd((PlayerBankAddEvent) event))
						handled = true;
					break;
				case PlayerInventoryAdd:
					if (eventHandler.onPlayerInventoryAdd((PlayerInventoryAddEvent) event))
						handled = true;
					break;
				case PlayerInventoryChange:
					if (eventHandler.onPlayerInventoryChanged((PlayerInventoryChangeEvent) event))
						handled = true;
					break;
				case PlayerInventoryMove:
					if (eventHandler.onPlayerInventoryMove((PlayerInventoryMoveEvent) event))
						handled = true;
					break;
				case PlayerInventoryRemove:
					if (eventHandler.onPlayerInventoryRemove((PlayerInventoryRemoveEvent) event))
						handled = true;
					break;
				case PlayerStatsChange:
					if (eventHandler.onPlayerStatsChange((PlayerStatsChangeEvent) event))
						handled = true;
					break;
				case EntityInteraction:
					if (eventHandler.onEntityInteraction((EntityInteractionEvent) event))
						handled = true;
					break;
				case EntityDeath:
					if (eventHandler.onEntityDeath((EntityDeathEvent) event))
						handled = true;
					break;
				}
			}
			
			//Go to next lowest priority
			priority = priority.getNextLowest();
		}
		
		event.setHandled(handled);
		return event;
	}
	
}
