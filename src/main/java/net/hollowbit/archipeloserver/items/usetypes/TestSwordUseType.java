package net.hollowbit.archipeloserver.items.usetypes;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.UseType;
import net.hollowbit.archipeloshared.HitCalculator;
import net.hollowbit.archipeloshared.UseTypeSettings;

public class TestSwordUseType implements UseType {

	@Override
	public UseTypeSettings useItemTap(Item item, Player user, long time) {
		ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) user.getMap().getEntities();
		for (Entity entity : entitiesOnMap) {
			if (entity == user || !entity.getEntityType().isHittable())
				continue;
			
			//Check for entity hits
			if (HitCalculator.didEntityHitEntityRects(user.getCenterPoint().x, user.getCenterPoint().y, entity.getCollisionRects(time), item.getType().hitRange, user.getLocation().getDirection())) {
				entity.heal(-((int) user.getStatsManager().hit(item)));
			}
		}
		
		int useAnimation = user.getRandom().nextInt(item.getType().numOfUseAnimations);
		return new UseTypeSettings(useAnimation, 0, true);
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
