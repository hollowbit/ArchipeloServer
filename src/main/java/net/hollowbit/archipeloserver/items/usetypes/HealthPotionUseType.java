package net.hollowbit.archipeloserver.items.usetypes;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.UseType;

public class HealthPotionUseType implements UseType {
	
	//Example of item use code
	
	@Override
	public boolean useItem(Item item, Player user) {
		/*if (user.isFullHealth())
			return false;
		else {
			user.addHealth(item.getType().minDamage);
			return true;
		}*/
		return true;
	}

}
