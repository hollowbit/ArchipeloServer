package net.hollowbit.archipeloserver.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloshared.ItemTypeData;

public enum ItemType {
	
	BODY("body"),
	PANTS_BASIC("pants_basic"),
	BOOTS_BASIC("boots_basic"),
	SHIRT_BASIC("shirt_basic"),
	GLOVES_BASIC("gloves_basic"),
	SHOULDERPADS_BASIC("shoulderpads_basic"),
	HAIR1("hair1"),
	FACE1("face1")/*,
	SWORD("sword"),
	POTION_SMALL("potion_small", new HealthPotionUseType())*/;
	
	public static final int NO_EQUIP_TYPE = -1;
	public static final int EQUIP_INDEX_BOOTS = 0;
	public static final int EQUIP_INDEX_PANTS = 1;
	public static final int EQUIP_INDEX_SHIRT = 2;
	public static final int EQUIP_INDEX_GLOVES = 3;
	public static final int EQUIP_INDEX_SHOULDERPADS = 4;
	public static final int EQUIP_INDEX_BODY = 5;
	public static final int EQUIP_INDEX_FACE = 6;
	public static final int EQUIP_INDEX_HAIR = 7;
	public static final int EQUIP_INDEX_HAT = 8;
	public static final int EQUIP_INDEX_USABLE = 9;
	
	public static final float WALK_ANIMATION_LENGTH = 0.15f;
	public static final float ROLL_ANIMATION_LENGTH = 0.08f;
	public static final float SPRINT_ANIMATION_LENGTH = 0.11f;
	public static final int WEARABLE_SIZE = 32;
	
	public String id;
	public String name;
	public String desc;
	public int iconX, iconY;
	public int minDamage;
	public int maxDamage;
	public int maxStackSize;
	public float critMultiplier;
	public float critChance;
	public int durability;
	public int equipType;
	public boolean buff;
	public boolean ammo;
	public boolean consumable;
	public boolean material;
	public int numOfStyles;
	public int numOfUseAnimations;
	public float useAnimationLength;
	
	private UseType useType;
	
	private ItemType (String id) {
		this(id, null);
	}
	
	private ItemType (String id, UseType useType) {
		Json json = new Json();
		
		//Get info from item json
		InputStream in = getClass().getResourceAsStream("/items/" + id + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String fileString = "";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				fileString += line;
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ItemTypeData data = json.fromJson(ItemTypeData.class, fileString);
		
		this.id = data.id;
		this.name = data.name;
		this.desc = data.desc;
		this.iconX = data.iconX;
		this.iconY = data.iconY;
		this.minDamage = data.minDamage;
		this.maxDamage = data.maxDamage;
		this.maxStackSize = data.maxStackSize;
		this.critMultiplier = data.critMultiplier;
		this.critChance = data.critChance;
		this.durability = data.durability;
		this.equipType = data.equipType;
		this.buff = data.buff;
		this.ammo = data.ammo;
		this.consumable = data.consumable;
		this.material = data.material;
		this.numOfStyles = data.numOfStyles;
		this.numOfUseAnimations = data.numOfUseAnimations;
		this.useAnimationLength = data.useAnimationLength;
		
		if (equipType == EQUIP_INDEX_USABLE)
			this.useType = useType;
		else
			useType = null;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public UseType getUseType () {
		return useType;
	}
	
	private static HashMap<String, ItemType> itemTypes;
	static {
		itemTypes = new HashMap<String, ItemType>();
		
		for (ItemType type : ItemType.values())
			itemTypes.put(type.id, type);
	}
	
	public static ItemType getItemTypeByItem (Item item) {
		return itemTypes.get(item.id);
	}
	
}
