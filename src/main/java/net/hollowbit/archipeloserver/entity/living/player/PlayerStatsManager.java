package net.hollowbit.archipeloserver.entity.living.player;

import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.PlayerInventoryChangeEvent;
import net.hollowbit.archipeloserver.tools.event.events.PlayerStatsChangeEvent;

public class PlayerStatsManager implements EventHandler {

	public static final float ROLLING_SPEED_SCALE = 3.0f;
	public static final float SPRINTING_SPEED_SCALE = 1.4f;
	
	private Player player;
	
	private float speed;
	//private int minDamage;
	//private int maxDamage;
	private int defense;
	private float damageMultiplier;
	private float defenseMultiplier;
	private float speedMultiplier;
	//private float critMultiplier;
	//private int critChance;
	private int maxHealth;
	
	public PlayerStatsManager (Player player) {
		this.player = player;
		this.addToEventManager();
		this.update();
	}
	
	public void update () {
		Item weapon = player.getInventory().getWeaponInventory().getRawStorage()[0];
		Item[] equipped = player.getInventory().getEquippedInventory().getRawStorage();
		Item[] buffs = player.getInventory().getBuffsInventory().getRawStorage();

		//Set speed to default player speed for now
		speed = EntityType.PLAYER.getSpeed();
		maxHealth = EntityType.PLAYER.getMaxHealth();
		
		//Calculate defense
		this.defense = 0;
		for (Item item : equipped) {
			if (item != null)
				defense += item.getType().defense;
		}
		for (Item item : buffs) {
			if (item != null)
				defense += item.getType().defense;
		}
		if (weapon != null)
			defense += weapon.getType().defense;
		
		//Calculate damage multiplier
		damageMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				damageMultiplier *= item.getType().damageMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				damageMultiplier *= item.getType().damageMultiplier;
		}
		if (weapon != null)
			damageMultiplier *= weapon.getType().damageMultiplier;
		
		//Calculate defense multiplier
		defenseMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				defenseMultiplier *= item.getType().defenseMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				defenseMultiplier *= item.getType().defenseMultiplier;
		}
		if (weapon != null)
			defenseMultiplier *= weapon.getType().defenseMultiplier;
		
		//Calculate speed multiplier
		speedMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				speedMultiplier *= item.getType().speedMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				speedMultiplier *= item.getType().speedMultiplier;
		}
		if (weapon != null)
			speedMultiplier *= weapon.getType().speedMultiplier;
		
		PlayerStatsChangeEvent event = new PlayerStatsChangeEvent(player, speed, defense, damageMultiplier, defenseMultiplier, speedMultiplier);
		event.trigger();
		
		//Update speed for players
		player.getChangesSnapshot().putFloat("playerSpeed", this.speed * this.speedMultiplier);
	}
	
	/**
	 * Returns a random hit value depending on min and max damage and damage multiplier also considering critical hits.
	 * Use this when player attacks.
	 * @param item
	 * @return
	 */
	public float hit (Item item) {
		float damage = StaticTools.getRandom().nextInt(item.getType().maxDamage + 1 - item.getType().minDamage) + item.getType().minDamage;
		
		if (StaticTools.getRandom().nextInt(100) < item.getType().critChance)//Checks for a crit hit
			damage *= item.getType().critMultiplier;
		
		return damage * damageMultiplier;
	}
	
	public float getSpeed (boolean isSprinting, boolean isRolling) {
		if (isRolling)
			return getBaseSpeed() * ROLLING_SPEED_SCALE;
		else if (isSprinting)
			return getSpeed() * SPRINTING_SPEED_SCALE;
		else
			return getSpeed();
	}
	
	public float getSpeed () {
		return speed * speedMultiplier;
	}
	
	public float getBaseSpeed () {
		return speed;
	}

	public int getBaseDefense() {
		return defense;
	}
	
	public float getDefense () {
		return defense * this.damageMultiplier;
	}

	public float getDamageMultiplier() {
		return damageMultiplier;
	}

	public float getDefenseMultiplier() {
		return defenseMultiplier;
	}

	public float getSpeedMultiplier() {
		return speedMultiplier;
	}
	
	public float getHealth() {
		return player.getHealth();
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	@Override
	public boolean onPlayerInventoryChanged (PlayerInventoryChangeEvent event) {
		if (event.getPlayer() == player) {
			if (event.getInventoryId() == PlayerInventory.BUFFS_EQUIP_INVENTORY || event.getInventoryId() == PlayerInventory.EQUIPPED_INVENTORY || event.getInventoryId() == PlayerInventory.WEAPON_EQUIP_INVENTORY) {
				update();
				return true;
			}
		}
		return EventHandler.super.onPlayerInventoryChanged(event);
	}
	
	public void dispose () {
		this.removeFromEventManager();
	}
	
}
