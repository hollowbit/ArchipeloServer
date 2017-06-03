package net.hollowbit.archipeloserver.form.forms;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.form.FormManager;
import net.hollowbit.archipeloserver.form.FormType;
import net.hollowbit.archipeloserver.form.RequestableForm;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerInventoryChangeEvent;
import net.hollowbit.archipeloshared.FormData;

public class InventoryForm extends RequestableForm implements EventHandler {
	
	private static final String KEY_MAIN_INVENTORY = "mainInventory";
	private static final String KEY_EQUIPPED_INVENTORY = "equippedInventory";
	private static final String KEY_COSMETIC_INVENTORY = "cosmeticInventory";
	private static final String KEY_WEAPON_INVENTORY = "weaponInventory";
	private static final String KEY_CONSUMABLES_INVENTORY = "consumablesInventory";
	private static final String KEY_BUFFS_INVENTORY = "buffsInventory";
	private static final String KEY_AMMO_INVENTORY = "ammoInventory";

	private static final String KEY_FROM_SLOT = "fromSlot";
	private static final String KEY_TO_SLOT = "toSlot";
	private static final String KEY_FROM_INVENTORY = "fromInventory";
	private static final String KEY_TO_INVENTORY = "toInventory";
	
	@Override
	public void create (FormData formData, FormType formType, FormManager formManager) {
		super.create(formData, formType, formManager);
		this.addToEventManager(EventType.PlayerInventoryChange);
	}

	@Override
	public void interactWith (Player player, String command, HashMap<String, String> data) {
		super.interactWith(player, command, data);
		
		int fromSlot = 0;
		int toSlot = 0;
		int fromInventory = 0;
		int toInventory = 0;
		
		switch (command) {
		case "move":
			fromSlot = Integer.parseInt(data.get(KEY_FROM_SLOT));
			toSlot = Integer.parseInt(data.get(KEY_TO_SLOT));
			fromInventory = Integer.parseInt(data.get(KEY_FROM_INVENTORY));
			toInventory = Integer.parseInt(data.get(KEY_TO_INVENTORY));
			
			player.getInventory().move(fromSlot, toSlot, fromInventory, toInventory);
			this.updateClient();
			break;
		case "delete":
			fromSlot = Integer.parseInt(data.get(KEY_FROM_SLOT));
			fromInventory = Integer.parseInt(data.get(KEY_FROM_INVENTORY));
			
			player.getInventory().deleteItemsInSlot(fromSlot, fromInventory);
			this.updateClient();
			break;
		}
	}

	@Override
	public void close () {
		this.removeFromEventManager();
	}
	
	@Override
	public boolean onPlayerInventoryChanged(PlayerInventoryChangeEvent event) {
		if (event.getPlayer() == player) {
			this.updateClient();
			return true;
		}
		
		return EventHandler.super.onPlayerInventoryChanged(event);
	}

	@Override
	public boolean canInteractWith (Player player) {
		return super.canInteractWith(player);
	}

	@Override
	public FormData getFormDataForClient () {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(KEY_MAIN_INVENTORY, player.getInventory().getMainInventory().getJson());
		data.put(KEY_EQUIPPED_INVENTORY, player.getInventory().getEquippedInventory().getJson());
		data.put(KEY_COSMETIC_INVENTORY, player.getInventory().getCosmeticInventory().getJson());
		
		data.put(KEY_WEAPON_INVENTORY, player.getInventory().getWeaponInventory().getJson());
		data.put(KEY_CONSUMABLES_INVENTORY, player.getInventory().getConsumablesInventory().getJson());
		data.put(KEY_BUFFS_INVENTORY, player.getInventory().getBuffsInventory().getJson());
		data.put(KEY_AMMO_INVENTORY, player.getInventory().getAmmoInventory().getJson());
		return new FormData(type.id, id, data);
	}

}
