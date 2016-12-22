package net.hollowbit.archipeloserver.tools.event.events;

import java.sql.Date;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.event.Event;
import net.hollowbit.archipeloserver.tools.event.EventType;

/**
 * Event on player join. No editable parameters.
 * @author Nathanael
 *
 */
public class PlayerJoinEvent extends Event {
	
	private Player player;
	private Date timeJoined;
	
	public PlayerJoinEvent (Player player) {
		super(EventType.PlayerJoin);
		this.player = player;
		this.timeJoined = new Date(System.currentTimeMillis());
	}

	public Player getPlayer() {
		return player;
	}

	public Date getTimeJoined() {
		return timeJoined;
	}
	
}
