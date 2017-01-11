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
	private static final String MIN_DAMAGE = "minDamage";
	private static final String MAX_DAMAGE = "maxDamage";
	private static final String DEFENSE = "defense";
	private static final String DAMAGE_MULTIPLIER = "damageMultiplier";
	private static final String DEFENSE_MULTIPLIER = "defenseMultiplier";
	private static final String CRIT_MULTIPLIER = "critMultiplier";
	private static final String CRIT_CHANCE = "critChance";
	
	@Override
	public void create(FormData formData, FormType formType, FormManager formManager) {
		this.addToEventManager();
		super.create(formData, formType, formManager);
	}
	
	@Override
	public FormData getFormDataForClient () {
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(SPEED, "" + player.getStatsManager().getSpeed());
		data.put(MIN_DAMAGE, "" + player.getStatsManager().getMinDamage());
		data.put(MAX_DAMAGE, "" + player.getStatsManager().getMaxDamage());
		data.put(DEFENSE, "" + player.getStatsManager().getDefense());
		data.put(DAMAGE_MULTIPLIER, "" + player.getStatsManager().getDamageMultiplier());
		data.put(DEFENSE_MULTIPLIER, "" + player.getStatsManager().getDefenseMultiplier());
		data.put(CRIT_MULTIPLIER, "" + player.getStatsManager().getCritMultiplier());
		data.put(CRIT_CHANCE, "" + player.getStatsManager().getCritChance());
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
