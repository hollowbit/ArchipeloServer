package net.hollowbit.archipeloserver.form;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.FormData;

public abstract class Form {
	
	protected String id;
	protected FormType type;
	
	public void create (FormData formData, FormType formType) {
		this.id = formData.id;
		this.type = formType;
	}
	
	public abstract void interactWith (Player player, HashMap<String, String> data); 
	public abstract void close ();
	public abstract boolean canInteractWith (Player player);
	
	public String getId () {
		return id;
	}
	
	public FormType getType () {
		return type;
	}
	
}