package net.hollowbit.archipeloserver.form.forms;

import java.util.HashMap;

import net.hollowbit.archipeloserver.form.FormManager;
import net.hollowbit.archipeloserver.form.FormType;
import net.hollowbit.archipeloserver.form.RequestableForm;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.PlayerStatsChangeEvent;
import net.hollowbit.archipeloshared.FormData;

public class PlayerStatsForm extends RequestableForm implements EventHandler {
	
	private static final String SPEED = "speed";
	private static final String DEFENSE = "defense";
	private static final String DAMAGE_MULTIPLIER = "damageMultiplier";
	private static final String DEFENSE_MULTIPLIER = "defenseMultiplier";
	
	@Override
	public void create(FormData formData, FormType formType, FormManager formManager) {
		this.addToEventManager();
		super.create(formData, formType, formManager);
	}
	
	@Override
	public FormData getFormDataForClient () {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(SPEED, "" + player.getStatsManager().getSpeed());
		data.put(DEFENSE, "" + player.getStatsManager().getDefense());
		data.put(DAMAGE_MULTIPLIER, "" + player.getStatsManager().getDamageMultiplier());
		data.put(DEFENSE_MULTIPLIER, "" + player.getStatsManager().getDefenseMultiplier());
		return new FormData(type.id, id, data);
	}
	
	@Override
	public void close () {
		this.removeFromEventManager();
		super.close();
	}
	
	@Override
	public boolean onPlayerStatsChange (PlayerStatsChangeEvent event) {
		if (event.getPlayer() == player) {
			this.updateClient();
			return true;
		}
		
		return false;
	}

}
