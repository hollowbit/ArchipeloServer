package net.hollowbit.archipeloserver.entity.living.player;

import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.PlayerInventoryChangedEvent;

public class PlayerStatsManager implements EventHandler {

	public static final float ROLLING_SPEED_SCALE = 3.0f;
	public static final float SPRINTING_SPEED_SCALE = 1.4f;
	
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
		this.speed = EntityType.PLAYER.getSpeed();
		
		//Calculate min/max damage
		if (weapon != null) {
			this.minDamage = weapon.getType().minDamage;
			this.maxDamage = weapon.getType().maxDamage;
		} else {
			this.minDamage = 0;
			this.maxDamage = 0;
		}
		
		//Calculate defense
		this.defense = 0;
		for (Item item : equipped) {
			if (item != null)
				this.defense += item.getType().defense;
		}
		for (Item item : buffs) {
			if (item != null)
				this.defense += item.getType().defense;
		}
		if (weapon != null)
			this.defense += weapon.getType().defense;
		
		//Calculate damage multiplier
		this.damageMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				this.damageMultiplier *= item.getType().damageMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				this.damageMultiplier *= item.getType().damageMultiplier;
		}
		if (weapon != null)
			this.damageMultiplier *= weapon.getType().damageMultiplier;
		
		//Calculate defense multiplier
		this.defenseMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				this.defenseMultiplier *= item.getType().defenseMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				this.defenseMultiplier *= item.getType().defenseMultiplier;
		}
		if (weapon != null)
			this.defenseMultiplier *= weapon.getType().defenseMultiplier;
		
		//Calculate speed multiplier
		this.speedMultiplier = 1;
		for (Item item : equipped) {
			if (item != null)
				this.speedMultiplier *= item.getType().speedMultiplier;
		}
		for (Item item : buffs) {
			if (item != null)
				this.speedMultiplier *= item.getType().speedMultiplier;
		}
		if (weapon != null)
			this.speedMultiplier *= weapon.getType().speedMultiplier;
		
		//Calculate crit stuff
		if (weapon != null) {
			this.critMultiplier = weapon.getType().critMultiplier;
			this.critChance = weapon.getType().critChance;
		} else {
			this.critMultiplier = 1f;
			this.critChance = 0;
		}
		
		//Update speed for players
		player.getChangesSnapshot().putFloat("speed", this.speed * speedMultiplier);
	}
	
	/**
	 * Returns a random hit value depending on min and max damage and damage multiplier also considering critical hits.
	 * Use this when player attacks.
	 * @return
	 */
	public float hit () {
		float damage = StaticTools.getRandom().nextInt(maxDamage + 1 - minDamage) + minDamage;
		
		if (StaticTools.getRandom().nextInt(100) < critChance)//Checks for a crit hit
			damage *= critMultiplier;
		
		return damage * damageMultiplier;
	}
	
	public float getSpeed (boolean isSprinting, boolean isRolling) {
		if (isRolling)
			return speed * ROLLING_SPEED_SCALE * speedMultiplier;
		else if (isSprinting)
			return speed * SPRINTING_SPEED_SCALE * speedMultiplier;
		else
			return speed * speedMultiplier;
	}
	
	public float getSpeed () {
		return speed * speedMultiplier;
	}
	
	public float getBaseSpeed () {
		return speed;
	}

	public int getMinDamage() {
		return minDamage;
	}

	public int getMaxDamage() {
		return maxDamage;
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

	public float getCritMultiplier() {
		return critMultiplier;
	}

	public int getCritChance() {
		return critChance;
	}
	
	@Override
	public boolean onPlayerInventoryChanged (PlayerInventoryChangedEvent event) {
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
