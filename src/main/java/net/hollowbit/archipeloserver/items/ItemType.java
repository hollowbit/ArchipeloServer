package net.hollowbit.archipeloserver.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.items.ItemUseAnimation.IllegalItemUseAnimationDataException;
import net.hollowbit.archipeloserver.items.usetypes.BasicWeaponUseType;
import net.hollowbit.archipeloshared.ItemTypeData;

public enum ItemType {
	
	BODY("body"),
	PANTS_BASIC("pants_basic"),
	BOOTS_BASIC("boots_basic"),
	SHIRT_BASIC("shirt_basic"),
	/*GLOVES_BASIC("gloves_basic"),
	SHOULDERPADS_BASIC("shoulderpads_basic"),*/
	HAIR1("hair1"),
	FACE1("face1"),
	BLOBBY_ASHES("blobby_ashes"),
	SPEAR_BASIC("spear_basic", new BasicWeaponUseType()),
	ASSISTANT_GENERAL("assistant_general", new BasicWeaponUseType()),
	SOUL_DISRUPTOR("soul_disruptor", new BasicWeaponUseType()),
	SPIRIT_DISRUPTOR("spirit_disruptor", new BasicWeaponUseType()),
	DEMONS_TONGUE("demons_tongue", new BasicWeaponUseType())/*,
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
	
	public static final int WEARABLE_SIZE = 32;
	
	public String id;
	public int iconSize;
	public int iconX, iconY;
	public int maxStackSize;
	public int durability;
	public int equipType;
	public boolean buff;
	public boolean ammo;
	public boolean consumable;
	public boolean material;
	public int numOfStyles;
	public boolean renderUsingColor;
	public String[][] sounds;
	
	public int minDamage;
	public int maxDamage;
	public int defense;
	public float damageMultiplier = 1;
	public float defenseMultiplier = 1;
	public float speedMultiplier = 1;
	public float critMultiplier;
	public int critChance;
	public int hitRange;
	
	public ItemUseAnimation[] usableAnimations;
	
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
		
		this.id = id;
		this.iconSize = data.iconSize;
		this.minDamage = data.minDamage;
		this.maxDamage = data.maxDamage;
		this.defense = data.defense;
		this.damageMultiplier = data.damageMultiplier;
		this.defenseMultiplier = data.defenseMultiplier;
		this.speedMultiplier = data.speedMultiplier;
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
		this.sounds = data.sounds;
		this.hitRange = data.hitRange;
		
		this.usableAnimations = new ItemUseAnimation[data.useAnimData.length];
		for (int i = 0; i < usableAnimations.length; i++) {
			try {
				usableAnimations[i] = new ItemUseAnimation(this, data.useAnimData[i]);
			} catch (IllegalItemUseAnimationDataException e) {
				System.out.println(e.getMessage());
			}
		}
		
		if (equipType == EQUIP_INDEX_USABLE)
			this.useType = useType;
		else
			useType = null;
	}
	
	private void loadSounds() {
		for (int i = 0; i < numOfStyles; i++) {
			for (int u = 0; u < sounds[0].length; u++)
				ArchipeloServer.getServer().getSoundManager().addSound(sounds[i][u]);
		}
	}
	
	public String getSoundById(int style, int id) {
		if (style >= 0 && style < numOfStyles && id >= 0 && id < sounds[0].length)
			return sounds[style][id];
		return "";
	}
	
	public int getNumOfUseAnimationTypes() {
		return usableAnimations.length;
	}
	
	public float getUseAnimationLength(int animationType) {
		return usableAnimations[animationType % getNumOfUseAnimationTypes()].getTotalRuntime();
	}
	
	public ItemUseAnimation getUseAnimationByUseType(int useType) {
		return usableAnimations[useType % getNumOfUseAnimationTypes()];
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
	
	public static void loadAssets() {
		for (ItemType type : ItemType.values())
			type.loadSounds();
	}
	
}
