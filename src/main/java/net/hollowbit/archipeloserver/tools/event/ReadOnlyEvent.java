package net.hollowbit.archipeloserver.tools.event;

import net.hollowbit.archipeloserver.ArchipeloServer;

public class ReadOnlyEvent extends Event {

	public ReadOnlyEvent (EventType type) {
		super(type);
	}
	
	@Override
	public Event trigger() {
		ArchipeloServer.getServer().getEventManager().triggerEvent(this, true);
		return ArchipeloServer.getServer().getEventManager().triggerEvent(this, false);
	}

}
