package net.hollowbit.archipeloserver.form.forms;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.form.FormManager;
import net.hollowbit.archipeloserver.form.FormType;
import net.hollowbit.archipeloserver.form.RequestableForm;
import net.hollowbit.archipeloshared.FormData;

public class InventoryForm extends RequestableForm {
	
	private static final String KEY_MAIN_INVENTORY = "mainInventory";
	private static final String KEY_EQUIPPED_INVENTORY = "equippedInventory";
	private static final String KEY_COSMETIC_INVENTORY = "cosmeticInventory";

	private static final String KEY_FROM_SLOT = "fromSlot";
	private static final String KEY_TO_SLOT = "toSlot";
	private static final String KEY_FROM_INVENTORY = "fromInventory";
	private static final String KEY_TO_INVENTORY = "toInventory";
	
	@Override
	public void create (FormData formData, FormType formType, FormManager formManager) {
		super.create(formData, formType, formManager);
	}

	@Override
	public void interactWith (Player player, String command, HashMap<String, String> data) {
		super.interactWith(player, command, data);
		switch (command) {
		case "move":
			int fromSlot = Integer.parseInt(data.get(KEY_FROM_SLOT));
			int toSlot = Integer.parseInt(data.get(KEY_TO_SLOT));
			int fromInventory = Integer.parseInt(data.get(KEY_FROM_INVENTORY));
			int toInventory = Integer.parseInt(data.get(KEY_TO_INVENTORY));
			
			player.getInventory().move(fromSlot, toSlot, fromInventory, toInventory);
			break;
		}
	}

	@Override
	public void close () {
		
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
		return new FormData(type.id, id, data);
	}

}
