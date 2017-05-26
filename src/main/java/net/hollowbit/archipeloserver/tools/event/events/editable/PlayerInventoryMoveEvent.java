package net.hollowbit.archipeloserver.tools.event.events.editable;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.event.EditableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.inventory.Inventory;

public class PlayerInventoryMoveEvent extends EditableEvent {

	private Player player;
	private int toSlot, fromSlot;
	private int toInventory, fromInventory;
	
	public PlayerInventoryMoveEvent () {
		super(EventType.PlayerInventoryMove);
	}

	public PlayerInventoryMoveEvent (Player player, int toSlot, int fromSlot, int toInventory, int fromInventory) {
		super(EventType.PlayerInventoryMove);
		this.player = player;
		this.toSlot = toSlot;
		this.fromSlot = fromSlot;
		this.toInventory = toInventory;
		this.fromInventory = fromInventory;
	}

	public int getToSlot () {
		return toSlot;
	}

	public void setToSlot (int toSlot) {
		if (editingPrevented)
			return;
		
		if (getToInventory().doesSlotExists(toSlot))
			this.toSlot = toSlot;
	}

	public int getFromSlot () {
		return fromSlot;
	}

	public void setFromSlot (int fromSlot) {
		if (editingPrevented)
			return;
		
		if (getFromInventory().doesSlotExists(fromSlot))
			this.fromSlot = fromSlot;
	}

	public PlayerInventory getInventory () {
		return player.getInventory();
	}

	public void setToInventory (int toInventory) {
		if (editingPrevented)
			return;
		
		this.toInventory = toInventory;
	}

	public void setFromInventory (int fromInventory) {
		if (editingPrevented)
			return;
		
		this.fromInventory = fromInventory;
	}

	public Player getPlayer () {
		return player;
	}
	
	public Item getToItem () {
		return getToInventory().getRawStorage()[toSlot];
	}
	
	public Item getFromItem () {
		return getFromInventory().getRawStorage()[fromSlot];
	}
	
	public boolean isToSlotEmpty () {
		return getToInventory().isSlotEmpty(toSlot);
	}
	
	public Inventory getToInventory () {
		return player.getInventory().getInventoryById(toInventory);
	}
	
	public Inventory getFromInventory () {
		return player.getInventory().getInventoryById(fromInventory);
	}
	
	public int getToInventoryId () {
		return toInventory;
	}
	
	public int getFromInventoryId () {
		return fromInventory;
	}

}
