package net.hollowbit.archipeloserver.tools.event.events;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.ReadOnlyEvent;

public class PlayerStatsChangeEvent extends ReadOnlyEvent {
	
	private Player player;
	private float speed;
	private int minDamage;
	private int maxDamage;
	private int defense;
	private float damageMultiplier;
	private float defenseMultiplier;
	private float speedMultiplier;
	private float critMultiplier;
	private int critChance;
	
	public PlayerStatsChangeEvent (Player player, float speed, int minDamage, int maxDamage, int defense, float damageMultiplier, float defenseMultiplier, float speedMultiplier, float critMultiplier, int critChance) {
		super(EventType.PlayerStatsChange);
		this.player = player;
		this.speed = speed;
		this.minDamage = minDamage;
		this.maxDamage = maxDamage;
		this.defense = defense;
		this.damageMultiplier = damageMultiplier;
		this.defenseMultiplier = defenseMultiplier;
		this.speedMultiplier = speedMultiplier;
		this.critMultiplier = critMultiplier;
		this.critChance = critChance;
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

	public int getNewMinDamage() {
		return minDamage;
	}

	public void setNewMinDamage(int minDamage) {
		this.minDamage = minDamage;
	}

	public int getNewMaxDamage() {
		return maxDamage;
	}

	public void setNewMaxDamage(int maxDamage) {
		this.maxDamage = maxDamage;
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

	public float getNewCritMultiplier() {
		return critMultiplier;
	}

	public void setNewCritMultiplier(float critMultiplier) {
		this.critMultiplier = critMultiplier;
	}

	public int getNewCritChance() {
		return critChance;
	}

	public void setNewCritChance(int critChance) {
		this.critChance = critChance;
	}
	
	

}
