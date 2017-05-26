package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerBankAddEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryAddEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryMoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.editable.PlayerInventoryRemoveEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerInventoryChangeEvent;
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
		Inventory oldInventory = main.duplicate();
		
		Item remainder = addNoUpdate(item);
		inventoryUpdated(oldInventory, MAIN_INVENTORY);
		
		if (remainder != null) {
			addToBank(remainder);
			return true;
		} else
			return false;
	}
	
	/**
	 * Adds a list of items to inventory. Returns whether bank was used at least once.
	 * @param items
	 * @return
	 */
	public boolean addAll (ArrayList<Item> items) {
		Inventory oldInventory = main.duplicate();
		ArrayList<Item> remainingItems = new ArrayList<Item>();
		for (Item item : items) {
			Item remainder = this.addNoUpdate(item);
			
			if (remainder != null)
				remainingItems.add(remainder);
		}
		inventoryUpdated(oldInventory, MAIN_INVENTORY);
		
		if (remainingItems.size() > 0) {
			addAllToBank(remainingItems);
			return true;
		} else
			return false;
	}
	
	/**
	 * Adds to an inventory but does not call update handler.
	 * @param item
	 * @return
	 */
	private Item addNoUpdate (Item item) {
		PlayerInventoryAddEvent event = new PlayerInventoryAddEvent(player, item);
		event.trigger();
		
		if (event.wasCanceled()) {
			event.close();
			return null;
		}
		
		item = event.getItem();
		Item returnItem = main.add(item);
		event.close();
		return returnItem;
	}
	
	/**
	 * Add an item to the player's bank.
	 * @param item
	 */
	public void addToBank (Item item) {
		Inventory oldInventoryBank = bank.duplicate();
		addToBankNoUpdate(item);
		inventoryUpdated(oldInventoryBank, BANK_INVENTORY);
	}
	
	private void addAllToBank (ArrayList<Item> items) {
		Inventory oldInventoryBank = bank.duplicate();
		for (Item item : items) {
			addToBankNoUpdate(item);
		}
		inventoryUpdated(oldInventoryBank, BANK_INVENTORY);
	}
	
	/**
	 * Add an item to the player's bank but don't call update yet.
	 * @param item
	 */
	private void addToBankNoUpdate (Item item) {
		PlayerBankAddEvent event = new PlayerBankAddEvent(player, item);
		event.trigger();
		
		if (event.wasCanceled()) {
			event.close();
			return;
		}
		
		item = event.getItem();
		
		bank.add(item);
		event.close();
	}
	
	/**
	 * Remove an item from the player's main inventory.
	 * @param item
	 * @return
	 */
	public boolean remove (Item item) {
		PlayerInventoryRemoveEvent event = new PlayerInventoryRemoveEvent(player, item);
		event.trigger();
		
		if (event.wasCanceled()) {
			event.close();
			return false;
		}
		
		item = event.getItem();
		
		Inventory oldInventory = main.duplicate();
		boolean successful = main.remove(item);
		if (successful)
			inventoryUpdated(oldInventory, MAIN_INVENTORY);
		event.close();
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
		
		Item fromItem = inventoriesInArray[fromInventory].getRawStorage()[fromSlot];
		if (fromItem == null)
			return false;
		
		PlayerInventoryMoveEvent event = new PlayerInventoryMoveEvent(player, toSlot, fromSlot, toInventory, fromInventory);
		event.trigger();
		
		if (event.wasCanceled()) {
			event.close();
			return false;
		}
		
		toInventory = event.getToInventoryId();
		fromInventory = event.getFromInventoryId();
		toSlot = event.getToSlot();
		fromSlot = event.getFromSlot();
		
		if (fromInventory == toInventory) {
			Inventory oldInventory = inventoriesInArray[fromInventory].duplicate();
			if (toInventory == EQUIPPED_INVENTORY || toInventory == COSMETIC_INVENTORY) {
				if (fromItem.getType().equipType != toSlot) {
					event.cancel();
					event.close();
					return false;
				}
			}
			inventoriesInArray[fromInventory].move(fromSlot, toSlot);
			inventoryUpdated(oldInventory, fromInventory);
		} else {
			Inventory oldFromInventory = inventoriesInArray[fromInventory].duplicate();
			Inventory oldToInventory = inventoriesInArray[toInventory].duplicate();
			
			fromItem = inventoriesInArray[fromInventory].removeFromSlot(fromSlot);
			
			//Make sure items are only being put in correct slots
			if (toInventory == WEAPON_EQUIP_INVENTORY) {
				if (fromItem.getType().equipType != ItemType.EQUIP_INDEX_USABLE) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					event.cancel();
					event.close();
					return false;
				}
			} else if (toInventory == AMMO_EQUIP_INVENTORY) {
				if (!fromItem.getType().ammo) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					event.cancel();
					event.close();
					return false;
				}
			} else if (toInventory == BUFFS_EQUIP_INVENTORY) {
				if (!fromItem.getType().buff) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					event.cancel();
					event.close();
					return false;
				}
			} else if (toInventory == CONSUMABLES_EQUIP_INVENTORY) {
				if (!fromItem.getType().consumable) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					event.cancel();
					event.close();
					return false;
				}
			} else if (toInventory == EQUIPPED_INVENTORY || toInventory == COSMETIC_INVENTORY) {
				if (fromItem.getType().equipType != toSlot) {
					inventoriesInArray[fromInventory].setSlot(fromSlot, fromItem);
					event.cancel();
					event.close();
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

			inventoryUpdated(oldFromInventory, fromInventory);
			inventoryUpdated(oldToInventory, toInventory);
		}
		
		if (fromInventory == EQUIPPED_INVENTORY || fromInventory == COSMETIC_INVENTORY || fromInventory == WEAPON_EQUIP_INVENTORY || toInventory == EQUIPPED_INVENTORY || toInventory == COSMETIC_INVENTORY || toInventory == WEAPON_EQUIP_INVENTORY)
			player.updateDisplayInventory();
		
		event.close();
		return true;
	}
	
	/**
	 * Event for when an inventory is updated
	 * @param inventoryId
	 */
	private void inventoryUpdated (Inventory oldInventory, int inventoryId) {
		if (inventoryId == EQUIPPED_INVENTORY || inventoryId == COSMETIC_INVENTORY)
			player.getChangesSnapshot().putString("displayInventory", getDisplayInventoryJson());
		
		PlayerInventoryChangeEvent event = new PlayerInventoryChangeEvent(player, oldInventory, inventoriesInArray[inventoryId], inventoryId);
		event.trigger();
	}
	
	public String getDisplayInventoryJson () {
		try {
			return StaticTools.getJson().toJson(getDisplayInventory(uneditableEquipped.getRawStorage(), equipped.getRawStorage(), cosmetic.getRawStorage(), weapon.getRawStorage()));
		} catch (Exception e) {
			return "";
		}
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
	
	public Inventory getInventoryById (int id) {
		return inventoriesInArray[id];
	}
	
}
