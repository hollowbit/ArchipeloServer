package net.hollowbit.archipeloserver.items;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.movementanimation.types.KnockbackMovementAnimation;
import net.hollowbit.archipeloshared.HitCalculator;
import net.hollowbit.archipeloshared.UseTypeSettings;

public abstract class UseType {
	
	/**
	 * Uses an item on tapping action button. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful.
	 * @param item
	 * @param user
	 * @param time
	 * @return
	 */
	public abstract UseTypeSettings useItemTap (Item item, Player user, long time);
	
	/**
	 * Use item after action button held. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful, or not held down long enough.
	 * @param item
	 * @param user
	 * @param duration
	 * @param time
	 * @return
	 */
	public abstract UseTypeSettings useItemHold (Item item, Player user, float duration, long time);
	
	/**
	 * Use an item on double tapping action button. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful.
	 * @param item
	 * @param user
	 * @param delta Time between both presses.
	 * @param time
	 * @return
	 */
	public abstract UseTypeSettings useItemDoubleTap (Item item, Player user, float delta, long time);
	
	/**
	 * Re-usable method to deal damage (or heal) with a specified item
	 * @param item
	 * @param user
	 * @param time
	 * @param knockback
	 */
	protected void damageWithItem (Item item, Player user, long time, boolean knockback) {
		ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) user.getMap().getEntities();
		for (Entity entity : entitiesOnMap) {
			if (entity == user || !entity.getEntityType().isHittable())
				continue;
			
			//Check for entity hits
			if (HitCalculator.didEntityHitEntityRects(user.getFootX(), user.getFootY(), entity.getCollisionRects(time), item.getType().hitRange, user.getLocation().getDirection())) {
				if (knockback) {
					if (entity instanceof LivingEntity)
						((LivingEntity) entity).addMovementAnimation(new KnockbackMovementAnimation((LivingEntity) entity, user.getLocation().getDirection(), user.getStatsManager().getKnockback(item), Entity.DAMAGE_FLASH_DURATION));
				}
				entity.heal(-((int) user.getStatsManager().hit(item)));
			}
		}
	}
	
}
