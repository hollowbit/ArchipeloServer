package net.hollowbit.archipeloserver.tools;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.items.Item;

public class Inventory {
	
	public static int ROW_LENGTH = 9;
	
	Item[] storage;
	
	public Inventory (int size) {
		this.storage = new Item[size];
	}
	
	/**
	 * Add an item to the next available inventory slot if available.
	 * Any quantity is accepted.
	 * Returns leftovers that couldn't fit in inventory.
	 */
	public Item add (Item item) {
		ArrayList<Integer> slotsWithSameItem = new ArrayList<Integer>();
		for (int i = 0; i < storage.length; i++) {//Build list of slots with same item
			if (isSlotEmpty(i))
				continue;
			
			if (storage[i].isSameTypeAndStyle(item))
				slotsWithSameItem.add(i);
		}
		
		for (int slot : slotsWithSameItem) {
			Item storageItem = storage[slot];
			
			while (storageItem.quantity < storageItem.getType().maxStackSize && item.quantity > 0) {
				storageItem.quantity++;
				item.quantity--;
			}
			
			if (item.quantity <= 0)//Item done adding, return null
				return null;
		}
		
		int nextSlot = getNextEmptySlot();
		while (nextSlot != -1) {
			storage[nextSlot] = new Item(item.getType(), item.style, 0);
			Item storageItem = storage[nextSlot];
			
			while (storageItem.quantity < storageItem.getType().maxStackSize && item.quantity > 0) {
				storageItem.quantity++;
				item.quantity--;
			}
			
			if (item.quantity <= 0)//Item done adding, return null
				return null;
			
			nextSlot = getNextEmptySlot();
		}

		return item;//If this point reached, there is still some stuff left in item so return the rest
	}
	
	public boolean remove (Item item) {
		return remove(item, true);
	}
	
	/**
	 * Removes an item  if available, any quantity accepted.
	 * Returns if successful.
	 * @param item
	 * @return
	 */
	public boolean remove (Item item, boolean ignoreStyle) {
		if (!hasItem(item, ignoreStyle))
			return false;
		
		for (int i = storage.length - 1; i >= 0 && item.quantity > 0; i--) {//Remove item starting from end
			Item storageItem = storage[i];
			
			if (storageItem == null)
				continue;
			
			boolean same = false;
			if (ignoreStyle)
				same = storageItem.isSameType(item);
			else
				same = storageItem.isSameTypeAndStyle(item);
			
			if (same) {
				int numToRemove = 0;
				while (storageItem.quantity > 0 && item.quantity > 0) {//Keep removing items from slot until it is empty or remove item is empty
					numToRemove++;
					storageItem.quantity--;
					item.quantity--;
				}
				
				System.out.println("Removed " + numToRemove + "  from  " + i + "  Left: " + item.quantity);
			}
		}
		
		clean();
		return true;
	}
	
	/**
	 * Removes all slots with items of quantities of 0 or less
	 */
	public void clean () {
		for (int i = 0; i < storage.length; i++) {
			Item item = storage[i];
			if (item == null)
				continue;
			
			if (item.quantity <= 0)
				storage[i] = null;
		}
	}
	
	/**
	 * Moves items from on slot to another. Will stack them if they are the same type and style
	 * Returns if successful
	 * @param fromSlot
	 * @param toSlot
	 * @return
	 */
	public boolean move (int fromSlot, int toSlot) {
		if (!slotExists(toSlot) || isSlotEmpty(fromSlot))
			return false;
		
		Item fromItem = storage[fromSlot];
		
		if (isSlotEmpty(toSlot)) {
			storage[toSlot] = fromItem;
			storage[fromSlot] = null;
		} else {
			Item toItem = storage[toSlot];
			if (fromItem.isSameTypeAndStyle(fromItem)) {
				while (toItem.quantity <= toItem.getType().maxStackSize && fromItem.quantity >= 0) {
					toItem.quantity++;
					fromItem.quantity--;
				}
				
				storage[fromSlot] = null;
				
				if (fromItem.quantity >= 0)
					add(fromItem);
			} else {
				storage[toSlot] = fromItem;
				storage[fromSlot] = toItem;
			}
		}
		return true;
	}
	
	/**
	 * Returns if a slot exists within inventory.
	 * @param slot
	 * @return
	 */
	public boolean slotExists (int slot) {
		return slotExists(slot, storage);
	}
	
	private boolean slotExists (int slot, Item[] inventory) {
		return slot < inventory.length && slot >= 0;
	}
	
	public boolean isSlotEmpty (int slot) {
		if (slotExists(slot))
			return storage[slot] == null;
		else
			return true;
	}
	
	/**
	 * Returns if a particular slot has an item, taking into account the size
	 * @param slow
	 * @param item
	 * @return
	 */
	public boolean slotHasItem (int slot, Item item) {
		return slotHasItem(slot, item, storage);
	}
	
	private boolean slotHasItem (int slot, Item item, Item[] inventory) {
		if (!slotExists(slot, inventory))
			return false;
		
		Item itemInSlot = inventory[slot];
		
		if (item.id != itemInSlot.id)//Not same id
			return false;
		
		if (item.quantity > itemInSlot.quantity)
			return false;
		else 
			return true;
	}
	
	/**
	 * Summarizes the inventory by stacking all items of same type together
	 * @return
	 */
	private Item[] getInventorySummary (boolean ignoreStyle) {
		ArrayList<Item> items = new ArrayList<Item>();
		for (int i = 0; i < storage.length; i++) {
			Item item = storage[i];
			
			if (item == null)
				continue;
			
			boolean itemFound = false;
			for (Item itemInList : items) {//Search list to see if item of same type was found;
				if (itemInList == null)
					continue;
				
				boolean same = false;
				if (ignoreStyle)
					same =  itemInList.isSameType(item);
				else
					same = itemInList.isSameTypeAndStyle(item);
				
				if (same) {
					itemInList.quantity += item.quantity;
					itemFound = true;
					break;
				}
			}
			
			if (!itemFound)
				items.add(new Item(item.getType(), item.style, item.quantity));
		}
		Item[] itemsArray = new Item[storage.length];
		return items.toArray(itemsArray);
	}
	
	/**
	 * Returns if the inventory has this item
	 * @param item
	 * @return
	 */
	public boolean hasItem (Item item, boolean ignoreStyle) {
		Item[] inventorySummary = getInventorySummary(ignoreStyle);
		for (int i = 0; i < inventorySummary.length; i++) {
			if (slotHasItem(i, item, inventorySummary))
				return true;
		}
		return false;
	}
	
	/**
	 * Gets next available inventory slot. Returns -1 if inventory is full
	 * @return
	 */
	public int getNextEmptySlot () {
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null)
				return i;
		}
		return -1;
	}
	
	public boolean isInventoryFull () {
		return getNextEmptySlot() == -1;
	}
	
}
