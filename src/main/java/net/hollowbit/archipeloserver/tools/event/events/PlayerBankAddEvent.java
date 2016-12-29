package net.hollowbit.archipeloserver.tools.event.events;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.event.CancelableEvent;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.inventory.InfiniteInventory;

public class PlayerBankAddEvent extends CancelableEvent {
	
	private Player player;
	private Item item;

	public PlayerBankAddEvent (Player player, Item item) {
		super(EventType.PlayerBankAdd);
		this.player = player;
		this.item = item;
	}

	public Item getItem () {
		return item;
	}

	public void setItem (Item item) {
		this.item = item;
	}

	public Player getPlayer () {
		return player;
	}

	public InfiniteInventory getBank () {
		return player.getInventory().getBankInventory();
	}

}
