package net.hollowbit.archipeloserver.tools.inventory;

import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.StaticTools;

/**
 * Inventory with a fixed size that cannot be changed
 * @author Nathanael
 *
 */
public class FixedInventory extends Inventory {

	Item[] storage;
	
	public FixedInventory (int size) {
		this.storage = new Item[size];
	}
	
	public FixedInventory (Item[] startItems) {
		this.storage = startItems;
	}
	
	public FixedInventory (Inventory inventoryToDuplicate) {
		this.storage = new Item[inventoryToDuplicate.getRawStorage().length];
		
		for (int i = 0; i < this.storage.length; i++) {
			this.storage[i] = new Item(inventoryToDuplicate.getRawStorage()[i]);
		}
	}

	@Override
	public Item add (Item item) {
		for (int i = 0; i < storage.length; i++) {//Build list of slots with same item
			Item storageItem = storage[i];
			
			if (storageItem == null)
				continue;
			
			if (storage[i].isSameTypeAndStyle(item)) {
				int spaceLeftInSlot = storageItem.getType().maxStackSize - storageItem.quantity;
				
				if (item.quantity > spaceLeftInSlot) {
					storageItem.quantity += spaceLeftInSlot;
					item.quantity -= spaceLeftInSlot;
				} else {
					storageItem.quantity += item.quantity;
					item.quantity = 0;
				}
				
				if (item.quantity <= 0)//Item done adding, return null
					return null;
			}
		}
		
		int nextSlot = getNextEmptySlot();
		while (nextSlot != -1) {
			storage[nextSlot] = new Item(item);
			Item storageItem = storage[nextSlot];
			
			int spaceInSlot = storageItem.getType().maxStackSize;
			
			if (item.quantity > spaceInSlot) {
				storageItem.quantity = spaceInSlot;
				item.quantity -= spaceInSlot;
			} else {
				storageItem.quantity = item.quantity;
				item.quantity = 0;
			}
			
			if (item.quantity <= 0)//Item done adding, return null
				return null;
			
			nextSlot = getNextEmptySlot();
		}

		return item;//If this point reached, there is still some stuff left in item so return the rest
	}

	@Override
	public boolean remove(Item item, boolean ignoreStyle) {
		if (!hasItem(item, ignoreStyle))
			return false;

		for (int i = storage.length - 1; i >= 0 && item.quantity > 0; i--) {//Remove item starting from end
			Item storageItem = storage[i];
			
			if (storageItem == null)
				continue;
			
			if (storageItem.isSame(item, ignoreStyle)) {
				if (storageItem.quantity < item.quantity) {
					item.quantity -= storageItem.quantity;
					storageItem.quantity = 0;
				} else {
					storageItem.quantity -= item.quantity;
					item.quantity = 0;
				}
			}
		}
		
		clean();
		return true;
	}

	@Override
	public boolean move(int fromSlot, int toSlot, boolean ignoreStyle) {
		if (!doesSlotExists(toSlot) || isSlotEmpty(fromSlot))
			return false;
		
		Item fromItem = storage[fromSlot];
		
		if (isSlotEmpty(toSlot)) {
			storage[toSlot] = fromItem;
			storage[fromSlot] = null;
		} else {
			Item toItem = storage[toSlot];
			
			if (fromItem.isSame(toItem, ignoreStyle)) {
				int spaceLeftInSlot = toItem.getType().maxStackSize - toItem.quantity;
				if (fromItem.quantity > spaceLeftInSlot) {
					toItem.quantity += spaceLeftInSlot;
					fromItem.quantity -= spaceLeftInSlot;
					storage[fromSlot] = null;
					add(fromItem);
				} else {
					toItem.quantity += fromItem.quantity;
					fromItem.quantity = 0;
				}
			} else {
				storage[toSlot] = fromItem;
				storage[fromSlot] = toItem;
			}
		}
		clean();
		return true;
	}

	@Override
	public boolean hasItem(Item item, boolean ignoreStyle) {
		int quantityFound = 0;
		for (int i = 0; i < storage.length; i++) {
			Item storageItem = storage[i];
			if (storageItem.isSame(storageItem, ignoreStyle)) {
				quantityFound += storageItem.quantity;
				
				if (quantityFound >= storageItem.quantity)
					break;
			}
		}
		
		return quantityFound >= item.quantity;
	}

	@Override
	public boolean isInventoryFull() {
		return getNextEmptySlot() == -1;
	}

	@Override
	public boolean isSlotEmpty(int slot) {
		if (doesSlotExists(slot))
			return storage[slot] == null;
		else
			return true;
	}

	@Override
	protected void clean() {
		for (int i = 0; i < storage.length; i++) {
			Item item = storage[i];
			if (item == null)
				continue;
			
			if (item.quantity <= 0)
				storage[i] = null;
		}
	}
	
	@Override
	public boolean doesSlotExists (int slot) {
		return slot < storage.length && slot >= 0;
	}
	
	/**
	 * Gets next available inventory slot. Returns -1 if inventory is full
	 * @return
	 */
	private int getNextEmptySlot () {
		for (int i = 0; i < storage.length; i++) {
			if (storage[i] == null)
				return i;
		}
		return -1;
	}

	@Override
	public String getJson () {
		return StaticTools.getJson().toJson(storage);
	}

	@Override
	public Item setSlot (int slot, Item item) {
		if (!doesSlotExists(slot))
			return null;
		
		Item replacedItem = removeFromSlot(slot);
		storage[slot] = item;
		return replacedItem;
	}

	@Override
	public Item removeFromSlot (int slot) {
		if (!doesSlotExists(slot))
			return null;
		
		Item item = storage[slot];
		storage[slot] = null;
		return item;
	}

	@Override
	public Item[] getRawStorage() {
		return storage;
	}

	@Override
	public Inventory duplicate () {
		return new FixedInventory(this);
	}
	
}
