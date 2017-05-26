package net.hollowbit.archipeloserver.tools.event;

/**
 * Priority of event handlers.
 * Must be at least HIGH in order to cancel events.
 * @author vedi0boy
 *
 */
public enum EventHandlerPriority {
	
	NONE(null, false),//No priority. Will not handle the event of that type
	
	READONLY_LOW(null, false),
	READONLY_NORMAL(READONLY_LOW, false),
	READONLY_HIGH(READONLY_NORMAL, false),
	
	EDITABLE_LOW(null, true),
	EDITABLE_NORMAL(EDITABLE_LOW, true),
	EDITABLE_HIGH(EDITABLE_NORMAL, true);
	
	protected EventHandlerPriority nextLowest;
	protected boolean canEditEvents;
	
	private EventHandlerPriority(EventHandlerPriority nextLowest, boolean canCancelEvents) {
		this.nextLowest = nextLowest;
		this.canEditEvents = canCancelEvents;
	}
	
	public EventHandlerPriority getNextLowest() {
		return nextLowest;
	}
	
	public boolean canEditEvents() {
		return canEditEvents;
	}
	
	public static final EventHandlerPriority HIGHEST_EDITABLE = EDITABLE_HIGH;
	public static final EventHandlerPriority HIGHEST_READONLY = READONLY_HIGH;
	public static final EventHandlerPriority DEFAULT = READONLY_NORMAL;
	
}
