package net.hollowbit.archipeloserver.items;

import com.badlogic.gdx.graphics.Color;

import net.hollowbit.archipeloserver.entity.living.Player;

public class Item {
	
	public static final int DEFAULT_COLOR = Color.rgba8888(new Color(1, 1, 1, 1));
	
	public String id;
	public int color = DEFAULT_COLOR;
	public int durability = 1;
	public int style = 0;
	
	public Item () {}
	
	public Item (ItemType type) {
		this.id = type.id;
	}
	
	public ItemType getType () {
		return ItemType.getItemTypeByItem(this);
	}
	
	public boolean use (Player user) {
		return ItemType.getItemTypeByItem(this).getUseType().useItem(this, user);
	}
	
}
