package net.hollowbit.archipeloserver.items;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.UseTypeSettings;

public interface UseType {
	
	/**
	 * Uses an item on tapping action button. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful.
	 * @param item
	 * @param user
	 * @return
	 */
	public abstract UseTypeSettings useItemTap (Item item, Player user);
	
	/**
	 * Use item after action button held. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful, or not held down long enough.
	 * @param item
	 * @param user
	 * @param duration
	 * @return
	 */
	public abstract UseTypeSettings useItemHold (Item item, Player user, float duration);
	
	/**
	 * Use an item on double tapping action button. Returns a UseTypeSettings object with information about animations and sounds.
	 * Could return null if unsuccessful.
	 * @param item
	 * @param user
	 * @param delta Time between both presses.
	 * @return
	 */
	public abstract UseTypeSettings useItemDoubleTap (Item item, Player user, float delta);
	
}
