package net.hollowbit.archipeloserver.tools.event;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.event.events.*;

public interface EventHandler {
	
	public default boolean onEntityMove (EntityMoveEvent event) {return false;}
	public default boolean onEntityTeleport (EntityTeleportEvent event) {return false;}
	public default boolean onPlayerJoin (PlayerJoinEvent event) {return false;}
	public default boolean onPlayerLeave (PlayerLeaveEvent event) {return false;}
	
	public default void addToEventManager() {
		ArchipeloServer.getServer().getEventManager().add(this);
	}
	
	public default void removeFromEventManager() {
		ArchipeloServer.getServer().getEventManager().remove(this);
	}
	
}