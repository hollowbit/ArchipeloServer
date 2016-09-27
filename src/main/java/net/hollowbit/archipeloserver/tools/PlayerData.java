package net.hollowbit.archipeloserver.tools;

import net.hollowbit.archipeloserver.items.Item;

public class PlayerData {
	
	public String uuid;
	public String name;
	public byte[] hashedPassword;
	public byte[] salt;
	public float x, y;
	public String island, map;
	public Item[] equippedInventory;
	public Item[] inventory;
	public boolean hasCreatedPlayer;
	
}
