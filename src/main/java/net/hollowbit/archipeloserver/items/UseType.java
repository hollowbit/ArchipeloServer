package net.hollowbit.archipeloserver.items;

import net.hollowbit.archipeloserver.entity.living.Player;

public interface UseType {
	
	public abstract boolean useItem (Item item, Player user);//Return whether it was successful or not to determine if animation should be played
	
}
