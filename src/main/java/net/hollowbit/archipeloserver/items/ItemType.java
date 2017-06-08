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
	
	BODY,
	PANTS_BASIC,
	BOOTS_BASIC,
	SHIRT_BASIC,
	/*GLOVES_BASIC,
	SHOULDERPADS_BASIC,*/
	HAIR1,
	FACE1,
	BLOBBY_ASHES,
	SPEAR_BASIC(new BasicWeaponUseType()),
	ASSISTANT_GENERAL(new BasicWeaponUseType()),
	SOUL_DISRUPTOR(new BasicWeaponUseType()),
	SPIRIT_DISRUPTOR(new BasicWeaponUseType()),
	DEMONS_TONGUE(new BasicWeaponUseType()),
	FANN_KATANA(new BasicWeaponUseType()),
	HYLIAN_BROADSWORD(new BasicWeaponUseType()),
	LITTLE_RED(new BasicWeaponUseType()),
	FALLEN_ANGEL_BLADE(new BasicWeaponUseType()),
	TRAINING_SWORD(new BasicWeaponUseType()),
	ANGEL_BLADE(new BasicWeaponUseType()),
	ANGELIC_CLAYMORE(new BasicWeaponUseType()),
	BEAST_FURY(new BasicWeaponUseType()),
	DESERT_RAPIER(new BasicWeaponUseType()),
	DRAGONS_BREATH(new BasicWeaponUseType()),
	IRON_BULDGE(new BasicWeaponUseType()),
	IRON_HATCHET(new BasicWeaponUseType()),
	IRON_SCEPTER(new BasicWeaponUseType()),
	MIGHTY_HAMMER(new BasicWeaponUseType()),
	THE_FAIRY_MAN(new BasicWeaponUseType()),
	WORLD_BREAKER(new BasicWeaponUseType());
	
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
	
	public int knockback;
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
	
	private ItemType () {
		this(null);
	}
	
	private ItemType (UseType useType) {
		Json json = new Json();
		this.id = this.name().toLowerCase();
		
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
		
		this.iconSize = data.iconSize;
		this.knockback = data.knockback;
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
