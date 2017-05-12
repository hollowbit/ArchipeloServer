package net.hollowbit.archipeloserver.items.usetypes;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.UseType;
import net.hollowbit.archipeloshared.UseTypeSettings;

public class TestSwordUseType implements UseType {

	@Override
	public UseTypeSettings useItemTap(Item item, Player user) {
		int useAnimation = user.getRandom().nextInt(item.getType().numOfUseAnimations);
		return new UseTypeSettings(useAnimation, 0, true);
	}

	@Override
	public UseTypeSettings useItemHold(Item item, Player user, float duration) {
		return null;
	}

	@Override
	public UseTypeSettings useItemDoubleTap(Item item, Player user, float delta) {
		return null;
	}

}
