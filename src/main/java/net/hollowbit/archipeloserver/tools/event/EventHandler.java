package net.hollowbit.archipeloserver.tools.event;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.event.events.editable.*;
import net.hollowbit.archipeloserver.tools.event.events.readonly.*;

public interface EventHandler {
	
	public default boolean onEntityMove (EntityMoveEvent event) {return false;}
	public default boolean onEntityTeleport (EntityTeleportEvent event) {return false;}
	public default boolean onPlayerJoin (PlayerJoinEvent event) {return false;}
	public default boolean onPlayerLeave (PlayerLeaveEvent event) {return false;}
	public default boolean onPlayerBankAdd (PlayerBankAddEvent event) {return false;}
	public default boolean onPlayerInventoryAdd (PlayerInventoryAddEvent event) {return false;}
	public default boolean onPlayerInventoryChanged (PlayerInventoryChangeEvent event) {return false;}
	public default boolean onPlayerInventoryRemove (PlayerInventoryRemoveEvent event) {return false;}
	public default boolean onPlayerInventoryMove (PlayerInventoryMoveEvent event) {return false;}
	public default boolean onPlayerStatsChange (PlayerStatsChangeEvent event) {return false;}
	public default boolean onEntityInteraction (EntityInteractionEvent event) {return false;}
	public default boolean onEntityDeath (EntityDeathEvent event) {return false;}
	public default boolean onEntityHeal (EntityHealEvent event) {return false;}
	
	public default void addToEventManager(EventType... typesToRegister) {
		ArchipeloServer.getServer().getEventManager().add(this);
		
		//Register event handler to event types with default priority
		for (EventType type : typesToRegister)
			this.registerEventPriority(type, EventHandlerPriority.DEFAULT);
	}
	
	public default void removeFromEventManager() {
		ArchipeloServer.getServer().getEventManager().remove(this);
	}
	
	public default void registerEventPriority(EventType type, EventHandlerPriority priority) {
		ArchipeloServer.getServer().getEventManager().registerPriority(this, type, priority);
	}
	
	public class DefaultEventHandler implements EventHandler {
		
		public DefaultEventHandler() {
			//Add all event types to be handled
			this.addToEventManager(EventType.values());
		}
		
		public boolean onEntityMove (EntityMoveEvent event) {System.out.println("EventHandler.java Event Triggered: Entity Move"); return false;}
		public boolean onEntityTeleport (EntityTeleportEvent event) {System.out.println("EventHandler.java Event Triggered: Entity Teleport"); return false;}
		public boolean onPlayerJoin (PlayerJoinEvent event) {System.out.println("EventHandler.java Event Triggered: Player Join"); return false;}
		public boolean onPlayerLeave (PlayerLeaveEvent event) {System.out.println("EventHandler.java Event Triggered: Player Leave"); return false;}
		public boolean onPlayerBankAdd (PlayerBankAddEvent event) {System.out.println("EventHandler.java Event Triggered: Player Bank Add"); return false;}
		public boolean onPlayerInventoryAdd (PlayerInventoryAddEvent event) {System.out.println("EventHandler.java Event Triggered: Player Inventory Add"); return false;}
		public boolean onPlayerInventoryChanged (PlayerInventoryChangeEvent event) {System.out.println("EventHandler.java Event Triggered: Player Inventory Changed"); return false;}
		public boolean onPlayerInventoryRemove (PlayerInventoryRemoveEvent event) {System.out.println("EventHandler.java Event Triggered: Player Inventory Removed"); return false;}
		public boolean onPlayerInventoryMove (PlayerInventoryMoveEvent event) {System.out.println("EventHandler.java Event Triggered: Player Inventory Move"); return false;}
		public boolean onPlayerStatsChange (PlayerStatsChangeEvent event) {System.out.println("EventHandler.java Event Triggered: Player Stats Change"); return false;}
		public boolean onEntityInteraction (EntityInteractionEvent event) {System.out.println("EventHandler.java Event Triggered: Entity Interaction Occured"); return false;}
		public boolean onEntityDeath (EntityDeathEvent event) {System.out.println("EventHandler.java Event Triggered: Entity Death Occured"); return false;}
		public boolean onEntityHeal (EntityHealEvent event) {System.out.println("EventHandler.java Event Triggered: Entity Heal Occured"); return false;}
		
	}
	
}