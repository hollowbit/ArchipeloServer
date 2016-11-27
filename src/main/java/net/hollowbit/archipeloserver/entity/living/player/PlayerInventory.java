package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.inventory.FixedInventory;
import net.hollowbit.archipeloserver.tools.inventory.InfiniteInventory;
import net.hollowbit.archipeloserver.tools.inventory.Inventory;

public class PlayerInventory {
	
	public static final int INVENTORY_SIZE = 20;
	
	public static final int MAIN_INVENTORY = 0;
	public static final int BANK_INVENTORY = 1;
	public static final int EQUIPPED_INVENTORY = 2;
	public static final int COSMETIC_INVENTORY = 3;
	
	Player player;
	FixedInventory main;
	FixedInventory equipped;
	FixedInventory cosmetic;
	InfiniteInventory bank;
	Inventory[] inventoriesInArray;
	
	public PlayerInventory (Player player, Item[] mainInventory, Item[] equippedInventory, Item[] cosmeticInventory, ArrayList<Item> bankInventory) {
		this.player = player;
		this.main = new FixedInventory(mainInventory);
		this.equipped = new FixedInventory(equippedInventory);
		//this.cosmetic = new FixedInventory(cosmeticInventory);
		//this.bank = new InfiniteInventory(bankInventory);
		
		inventoriesInArray = new Inventory[4];
		inventoriesInArray[0] = main;
		inventoriesInArray[1] = bank;
		//inventoriesInArray[2] = equipped;
		//inventoriesInArray[3] = cosmetic;
	}
	
	/**
	 * Adds an item to the main inventory. Overflow gets put in banks.
	 * Returns true if bank was used.
	 * @param item
	 * @return
	 */
	public boolean add (Item item) {
		Item remainder = main.add(item);
		
		if (remainder == null)
			return false;
		else {
			bank.add(remainder);
			return true;
		}
	}
	
	/**
	 * Adds a list of items to inventory. Returns whether bank was used at least once.
	 * @param items
	 * @return
	 */
	public boolean addAll (ArrayList<Item> items) {
		boolean bankUsed = false;
		for (Item item : items) {
			if (this.add(item))
				bankUsed = true;
		}
		return bankUsed;
	}
	
	public boolean remove (Item item) {
		return main.remove(item);
	}
	
	/**
	 * Determines if the player has a specific item in main inventory.
	 * @param item
	 * @return
	 */
	public boolean hasItem (Item item) {
		return main.hasItem(item);
	}
	
	/**
	 * Determines if the player has a specific item in any inventory at all.
	 * @param item
	 * @return
	 */
	public boolean hasItemAtAll (Item item) {
		return main.hasItem(item) || bank.hasItem(item) || equipped.hasItem(item) || cosmetic.hasItem(item);
	}
	
	/**
	 * Move item from on inventory to another. Returns whether successful or not.
	 * @param fromSlot
	 * @param toSlot
	 * @param fromInventory
	 * @param toInventory
	 * @return
	 */
	public boolean move (int fromSlot, int toSlot, int fromInventory, int toInventory) {
		if (fromInventory >= inventoriesInArray.length || toInventory >= inventoriesInArray.length)
			return false;
		
		if (!inventoriesInArray[fromInventory].doesSlotExists(fromSlot) || !inventoriesInArray[toInventory].doesSlotExists(toSlot))
			return false;
			
		if (fromInventory == toInventory)
			inventoriesInArray[fromInventory].move(fromSlot, toSlot);
		else {
			Item fromItem = inventoriesInArray[fromInventory].removeFromSlot(fromSlot);
			if (fromItem == null)
				return false;
			
			Item toItem = inventoriesInArray[toInventory].removeFromSlot(toSlot);
			if (toItem != null)
				inventoriesInArray[fromInventory].setSlot(fromSlot, toItem);
			
			inventoriesInArray[toInventory].setSlot(toSlot, fromItem);
		}
		return true;
	}
	
	public FixedInventory getMainInventory () {
		return main;
	}
	
	public FixedInventory getEquippedInventory () {
		return equipped;
	}
	
	public FixedInventory getCosmeticInventory () {
		return cosmetic;
	}
	
	public InfiniteInventory getBankInventory () {
		return bank;
	}
	
}
