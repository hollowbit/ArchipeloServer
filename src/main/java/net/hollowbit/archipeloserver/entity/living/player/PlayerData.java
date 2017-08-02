package net.hollowbit.archipeloserver.entity.living.player;

import java.sql.Date;

import net.hollowbit.archipeloserver.items.Item;

public class PlayerData {
	
	public String id;
	public String name;
	public String bhUuid;
	public float x, y;
	public String map;
	public float health;
	public float respawnX, respawnY;
	public String respawnMap;
	public Item[] uneditableEquippedInventory;
	public Item[] equippedInventory;
	public Item[] cosmeticInventory;
	public Item[] bankInventory;
	public Item[] inventory;
	public Item[] weaponInventory;
	public Item[] consumablesInventory;
	public Item[] buffsInventory;
	public Item[] ammoInventory;
	public Date lastPlayed, creationDate;
	public String[] flags;
	
}
