package net.hollowbit.archipeloserver.items;

import com.badlogic.gdx.graphics.Color;

import net.hollowbit.archipeloserver.entity.living.Player;

public class Item {
	
	public static final int DEFAULT_COLOR = Color.rgba8888(new Color(1, 1, 1, 1));
	
	public String id;
	public int color = DEFAULT_COLOR;
	public int durability = 1;
	public int style = 0;
	public int quantity = 1;
	
	public Item () {}
	
	public Item (ItemType type) {
		this.id = type.id;
	}
	
	public Item (ItemType type, int style) {
		this.id = type.id;
		this.style = style;
	}
	
	public Item (ItemType type, int style, int quantity) {
		this.id = type.id;
		this.style = style;
		this.quantity = quantity;
	}
	
	public ItemType getType () {
		return ItemType.getItemTypeByItem(this);
	}
	
	public boolean use (Player user) {
		return ItemType.getItemTypeByItem(this).getUseType().useItem(this, user);
	}
	
	/**
	 * Returns if item is same type
	 * @param item
	 * @return
	 */
	public boolean isSameType (Item item) {
		return this.id.equals(item.id);
	}
	
	/**
	 * Returns if item is same type and style
	 * @param item
	 * @return
	 */
	public boolean isSameTypeAndStyle (Item item) {
		return isSameType(item) && this.style == item.style;
	}
	
}
