package net.hollowbit.archipeloserver.form;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.FormDataPacket;
import net.hollowbit.archipeloshared.FormData;

public abstract class Form {
	
	protected String id;
	protected FormType type;
	protected FormManager formManager;
	
	public void create (FormData formData, FormType formType, FormManager formManager) {
		this.id = formData.id;
		this.type = formType;
		this.formManager = formManager;
	}
	
	/**
	 * Interact with the form. Please specify a valid command. "close" works on all forms.
	 * @param player
	 * @param command
	 * @param data
	 */
	public void interactWith (Player player, String command, HashMap<String, String> data) {
		if (command.endsWith("close")) {//If close command sent, close the form
			formManager.removeForm(this);
		}
	}
	
	public abstract void close ();
	
	/**
	 * Determines if the specified player can interact with this form at this instant.
	 * @param player
	 * @return
	 */
	public abstract boolean canInteractWith (Player player);
	
	/**
	 * Sends updated form to client
	 */
	protected void updateClient (Player player) {
		player.sendPacket(new FormDataPacket(getFormDataForClient()));
	}
	
	public abstract FormData getFormDataForClient ();
	
	public String getId () {
		return id;
	}
	
	public FormType getType () {
		return type;
	}
	
}