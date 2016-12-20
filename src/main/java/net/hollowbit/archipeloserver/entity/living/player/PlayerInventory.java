package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.inventory.FixedInventory;
import net.hollowbit.archipeloserver.tools.inventory.InfiniteInventory;
import net.hollowbit.archipeloserver.tools.inventory.Inventory;

public class PlayerInventory {

	public static final int DISPLAY_EQUIP_SIZE = 10;
	
	public static final int INVENTORY_SIZE = 20;
	public static final int INVENTORY_ROW_LENGTH = 5;
	
	public static final int NUM_INVENTORIES = 9;
	public static final int MAIN_INVENTORY = 0;
	public static final int BANK_INVENTORY = 1;
	public static final int EQUIPPED_INVENTORY = 2;
	public static final int COSMETIC_INVENTORY = 3;
	public static final int UNEDITABLE_EQUIP_INVENTORY = 4;
	public static final int WEAPON_EQUIP_INVENTORY = 5;
	public static final int CONSUMABLES_EQUIP_INVENTORY = 6;
	public static final int BUFFS_EQUIP_INVENTORY = 7;
	public static final int AMMO_EQUIP_INVENTORY = 8;
	
	Player player;
	FixedInventory main;
	FixedInventory uneditableEquipped;
	FixedInventory equipped;
	FixedInventory cosmetic;
	InfiniteInventory bank;
	
	FixedInventory weapon;
	FixedInventory consumables;
	FixedInventory buffs;
	FixedInventory ammo;
	Inventory[] inventoriesInArray;
	
	public PlayerInventory (Player player, Item[] mainInventory, Item[] uneditableEquippedInventory, Item[] equippedInventory, Item[] cosmeticInventory, ArrayList<Item> bankInventory, Item[] weaponInventory, Item[] consumablesInventory, Item[] buffsInventory, Item[] ammoInventory) {
		this.player = player;
		this.main = new FixedInventory(mainInventory);
		this.uneditableEquipped = new FixedInventory(uneditableEquippedInventory);
		this.equipped = new FixedInventory(equippedInventory);
		this.cosmetic = new FixedInventory(cosmeticInventory);
		this.bank = new InfiniteInventory(bankInventory);
		this.weapon = new FixedInventory(weaponInventory);
		this.consumables = new FixedInventory(consumablesInventory);
		this.buffs = new FixedInventory(buffsInventory);
		this.ammo = new FixedInventory(ammoInventory);
		
		inventoriesInArray = new Inventory[NUM_INVENTORIES];
		inventoriesInArray[0] = main;
		inventoriesInArray[1] = bank;
		inventoriesInArray[2] = equipped;
		inventoriesInArray[3] = cosmetic;
		inventoriesInArray[4] = uneditableEquipped;
		inventoriesInArray[5] = weapon;
		inventoriesInArray[6] = consumables;
		inventoriesInArray[7] = buffs;
		inventoriesInArray[8] = ammo;
	}
	
	/**
	 * Adds an item to the main inventory. Overflow gets put in banks.
	 * Returns true if bank was used.
	 * @param item
	 * @return
	 */
	public boolean add (Item item) {
		boolean bankUsed = addNoUpdate(item);
		inventoryUpdated(MAIN_INVENTORY);
		if (bankUsed)
			inventoryUpdated(BANK_INVENTORY);
		return bankUsed;
	}
	
	/**
	 * Adds to an inventory but does not call update handler
	 * @param item
	 * @return
	 */
	private boolean addNoUpdate (Item item) {
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
			if (this.addNoUpdate(item))
				bankUsed = true;
		}
		inventoryUpdated(MAIN_INVENTORY);
		if (bankUsed)
			inventoryUpdated(BANK_INVENTORY);
		return bankUsed;
	}
	
	public boolean remove (Item item) {
		boolean successful = main.remove(item);
		if (successful)
			inventoryUpdated(MAIN_INVENTORY);
		return successful;
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
	 * @param toInventory Can be same as fromInventory to move within one inventory
	 * @return
	 */
	public boolean move (int fromSlot, int toSlot, int fromInventory, int toInventory) {
		if (fromInventory >= inventoriesInArray.length || toInventory >= inventoriesInArray.length)
			return false;
		
		if (fromInventory == UNEDITABLE_EQUIP_INVENTORY || toInventory == UNEDITABLE_EQUIP_INVENTORY)
			return false;
		
		if (!inventoriesInArray[fromInventory].doesSlotExists(fromSlot) || !inventoriesInArray[toInventory].doesSlotExists(toSlot))
			return false;
		
		/*if ((fromInventory == MAIN_INVENTORY && toInventory == BANK_INVENTORY) || (fromInventory == BANK_INVENTORY && toInventory == MAIN_INVENTORY)) {
			//TODO Handle situation where player tries to deposit or withdraw without an ATM nearby
		}*/
			
		if (fromInventory == toInventory) {
			inventoriesInArray[fromInventory].move(fromSlot, toSlot);
			inventoryUpdated(fromInventory);
		} else {
			Item fromItem = inventoriesInArray[fromInventory].removeFromSlot(fromSlot);
			if (fromItem == null)
				return false;
			
			//Make sure items are only being put in correct slots
			if (toInventory == WEAPON_EQUIP_INVENTORY) {
				if (fromItem.getType().equipType != ItemType.EQUIP_INDEX_USABLE) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					return false;
				}
			} else if (toInventory == AMMO_EQUIP_INVENTORY) {
				if (!fromItem.getType().ammo) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					return false;
				}
			} else if (toInventory == BUFFS_EQUIP_INVENTORY) {
				if (!fromItem.getType().buff) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					return false;
				}
			} else if (toInventory == CONSUMABLES_EQUIP_INVENTORY) {
				if (!fromItem.getType().consumable) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					return false;
				}
			} else if (toInventory == EQUIPPED_INVENTORY || toInventory == COSMETIC_INVENTORY) {
				if (fromItem.getType().equipType != toSlot) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					return false;
				}
			}

			//If it's the bank, just add the item
			if (toInventory == BANK_INVENTORY) {
				inventoriesInArray[toInventory].add(fromItem);
			} else {
				Item toItem = inventoriesInArray[toInventory].removeFromSlot(toSlot);
				if (toItem != null)
					inventoriesInArray[fromInventory].setSlot(fromSlot, toItem);
				
				inventoriesInArray[toInventory].setSlot(toSlot, fromItem);
			}

			inventoryUpdated(fromInventory);
			inventoryUpdated(toInventory);
		}
		
		if (fromInventory == EQUIPPED_INVENTORY || fromInventory == COSMETIC_INVENTORY || toInventory == EQUIPPED_INVENTORY || toInventory == COSMETIC_INVENTORY)
			player.updateDisplayInventory();
		
		return true;
	}
	
	/**
	 * Event for when an inventory is updated
	 * @param inventoryId
	 */
	private void inventoryUpdated (int inventoryId) {
		if (inventoryId == EQUIPPED_INVENTORY || inventoryId == COSMETIC_INVENTORY)
			player.getChangesSnapshot().putString("displayInventory", getDisplayInventoryJson());
	}
	
	public String getDisplayInventoryJson () {
		return StaticTools.getJson().toJson(getDisplayInventory(uneditableEquipped.getRawStorage(), equipped.getRawStorage(), cosmetic.getRawStorage(), weapon.getRawStorage()), Item[].class);
	}
	
	public FixedInventory getMainInventory () {
		return main;
	}
	
	public FixedInventory getUneditableEquippedInventory () {
		return uneditableEquipped;
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
	
	public FixedInventory getWeaponInventory () {
		return weapon;
	}
	
	public FixedInventory getConsumablesInventory () {
		return consumables;
	}
	
	public FixedInventory getBuffsInventory () {
		return buffs;
	}
	
	public FixedInventory getAmmoInventory () {
		return ammo;
	}
	
	public static Item[] getDisplayInventory (Item[] uneditableEquipped, Item[] equipped, Item[] cosmetic, Item[] weapon) {
		Item[] displayInventory = new Item[DISPLAY_EQUIP_SIZE];
		
		int[] indexes = new int[]{0, 6, 7};
		for (int i = 0; i < uneditableEquipped.length; i++) {
			displayInventory[indexes[i]] = uneditableEquipped[i];
		}

		indexes = new int[]{1, 2, 3, 4, 5, 8};
		for (int i = 0; i < equipped.length; i++) {
			if (cosmetic[i] != null)
				displayInventory[indexes[i]] = cosmetic[i];
			else
				displayInventory[indexes[i]] = equipped[i];
		}
		
		displayInventory[9] = weapon[0];
		return displayInventory;
	}
	
}
