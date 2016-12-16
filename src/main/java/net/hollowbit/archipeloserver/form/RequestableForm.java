package net.hollowbit.archipeloserver.form;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.FormData;

public abstract class RequestableForm extends Form {
	
	Player player;
	
	@Override
	public void create (FormData formData, FormType formType) {
		player = ArchipeloServer.getServer().getWorld().getPlayer(formData.data.get("player"));
		super.create(formData, formType);
	}
	
	@Override
	public void interactWith (Player player, HashMap<String, String> data) {
		
	}
	
	public abstract FormData getFormDataForClient ();

	@Override
	public void close () {
		
	}

	@Override
	public boolean canInteractWith (Player player) {
		return this.player == player;
	}

}
