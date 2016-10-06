package net.hollowbit.archipeloserver.tools;

import java.sql.Date;

import net.hollowbit.archipeloserver.items.Item;

public class PlayerData {
	
	public String uuid;
	public String name;
	public String bhUuid;
	public float x, y;
	public String island, map;
	public Item[] equippedInventory;
	public Item[] inventory;
	public Date lastPlayed, creationDate;
	
}
