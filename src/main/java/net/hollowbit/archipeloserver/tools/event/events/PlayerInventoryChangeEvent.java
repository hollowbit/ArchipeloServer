package net.hollowbit.archipeloserver.tools.event.events;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.ReadOnlyEvent;
import net.hollowbit.archipeloserver.tools.inventory.Inventory;

public class PlayerInventoryChangeEvent extends ReadOnlyEvent {

	private Player player;
	private Inventory oldInventory;
	private Inventory inventory;
	private int inventoryId;

	public PlayerInventoryChangeEvent(Player player, Inventory oldInventory, Inventory inventory, int inventoryId) {
		super(EventType.PlayerInventoryChange);
		this.player = player;
		this.oldInventory = oldInventory;
		this.inventory = inventory;
		this.inventoryId = inventoryId;
	}

	public Player getPlayer() {
		return player;
	}

	public Inventory getOldInventory() {
		return oldInventory;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public int getInventoryId() {
		return inventoryId;
	}
	
	public PlayerInventory getPlayerInventory () {
		return player.getInventory();
	}

}
