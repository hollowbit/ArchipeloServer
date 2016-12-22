package net.hollowbit.archipeloserver.tools.event.events;

import java.sql.Date;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.tools.event.Event;
import net.hollowbit.archipeloserver.tools.event.EventType;

/**
 * Event for when players leave. Reason and alt can be modified.
 * @author Nathanael
 *
 */
public class PlayerLeaveEvent extends Event {
	
	private Player player;
	private LogoutReason reason;
	private String reasonAlt;
	private Date timeLeft;
	
	public PlayerLeaveEvent(Player player, LogoutReason reason, String reasonAlt) {
		super(EventType.PlayerLeave);
		this.player = player;
		this.reason = reason;
		this.reasonAlt = reasonAlt;
		this.timeLeft = new Date(System.currentTimeMillis());
	}

	public Player getPlayer() {
		return player;
	}

	public LogoutReason getReason() {
		return reason;
	}

	public void setReason (LogoutReason reason) {
		this.reason = reason;
	}

	public String getReasonAlt() {
		return reasonAlt;
	}

	public void setReasonAlt (String reasonAlt) {
		this.reasonAlt = reasonAlt;
	}

	public Date getTimeLeft() {
		return timeLeft;
	}
	
}
