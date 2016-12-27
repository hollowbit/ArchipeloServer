package net.hollowbit.archipeloserver.tools.event.events;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.event.CancelableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.inventory.Inventory;

public class PlayerInventoryAddEvent extends CancelableEvent {
	
	private Player player;
	private Item item;
	
	public PlayerInventoryAddEvent (Player player, Item item) {
		super(EventType.PlayerInventoryAdd);
		this.player = player;
		this.item = item;
	}

	public Item getItem () {
		return item;
	}

	public void setItem (Item item) {
		this.item = item;
	}

	public Player getPlayer () {
		return player;
	}
	
	public PlayerInventory getPlayerInventory () {
		return player.getInventory();
	}
	
	public Inventory getInventory () {
		return player.getInventory().getMainInventory();
	}

}
