package net.hollowbit.archipeloserver.form;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.FormData;

public abstract class RequestableForm extends Form {
	
	protected Player player;
	
	@Override
	public void create (FormData formData, FormType formType, FormManager formManager) {
		player = ArchipeloServer.getServer().getWorld().getPlayer(formData.data.get("player"));
		super.create(formData, formType, formManager);
	}
	
	@Override
	public void interactWith (Player player, String command, HashMap<String, String> data) {
		super.interactWith(player, command, data);
	}
	
	protected void updateClient () {
		super.updateClient(player);
	}
	
	@Override
	public void remove() {
		super.remove();
	}
	
	/**
	 * This allows player to remove this form. Only use in Player.java
	 */
	public void removeSafe () {
		super.remove();
	}

	@Override
	public void close () {
		
	}

	@Override
	public boolean canInteractWith (Player player) {
		return this.player == player;
	}
	
	public Player getPlayer () {
		return player;
	}

}
