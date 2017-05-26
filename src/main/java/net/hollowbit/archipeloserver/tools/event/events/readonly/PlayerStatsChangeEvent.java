package net.hollowbit.archipeloserver.tools.event.events.readonly;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.ReadOnlyEvent;

public class PlayerStatsChangeEvent extends ReadOnlyEvent {
	
	private Player player;
	private float speed;
	private int defense;
	private float damageMultiplier;
	private float defenseMultiplier;
	private float speedMultiplier;
	
	public PlayerStatsChangeEvent (Player player, float speed, int defense, float damageMultiplier, float defenseMultiplier, float speedMultiplier) {
		super(EventType.PlayerStatsChange);
		this.player = player;
		this.speed = speed;
		this.defense = defense;
		this.damageMultiplier = damageMultiplier;
		this.defenseMultiplier = defenseMultiplier;
		this.speedMultiplier = speedMultiplier;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public float getNewSpeed() {
		return speed;
	}

	public void setNewSpeed(float speed) {
		this.speed = speed;
	}

	public int getNewDefense() {
		return defense;
	}

	public void setNewDefense(int defense) {
		this.defense = defense;
	}

	public float getNewDamageMultiplier() {
		return damageMultiplier;
	}

	public void setNewDamageMultiplier(float damageMultiplier) {
		this.damageMultiplier = damageMultiplier;
	}

	public float getNewDefenseMultiplier() {
		return defenseMultiplier;
	}

	public void setNewDefenseMultiplier(float defenseMultiplier) {
		this.defenseMultiplier = defenseMultiplier;
	}

	public float getNewSpeedMultiplier() {
		return speedMultiplier;
	}

	public void setNewSpeedMultiplier(float speedMultiplier) {
		this.speedMultiplier = speedMultiplier;
	}
	
	

}
