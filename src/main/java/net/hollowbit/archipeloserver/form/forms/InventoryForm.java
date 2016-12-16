package net.hollowbit.archipeloserver.form.forms;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.form.FormType;
import net.hollowbit.archipeloserver.form.RequestableForm;
import net.hollowbit.archipeloshared.FormData;

public class InventoryForm extends RequestableForm {

	@Override
	public void create (FormData formData, FormType formType) {
		super.create(formData, formType);
	}

	@Override
	public void interactWith (Player player, HashMap<String, String> data) {
		
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
		
		return new FormData(type.id, id, data);
	}

}
