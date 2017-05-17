package net.hollowbit.archipeloserver.items;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.UseTypeSettings;

public interface UseType {
	
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
	
}
