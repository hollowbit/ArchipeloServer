package net.hollowbit.archipeloserver.items.usetypes;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.UseType;
import net.hollowbit.archipeloshared.UseTypeSettings;

public class TestSwordUseType extends UseType {

	@Override
	public UseTypeSettings useItemTap(Item item, Player user, long time) {
		this.damageWithItem(item, user, time, true);
		//int useAnimation = user.getRandom().nextInt(item.getType().numOfUseAnimations);
		return new UseTypeSettings(0, 0, false);
	}

	@Override
	public UseTypeSettings useItemHold(Item item, Player user, float duration, long time) {
		return null;
	}

	@Override
	public UseTypeSettings useItemDoubleTap(Item item, Player user, float delta, long time) {
		return null;
	}

}
