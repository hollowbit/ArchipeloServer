package net.hollowbit.archipeloserver.network;

public enum LogoutReason {
	
	NONE(0, ""),
	KICK(1, "was kicked for"),
	LEAVE(2,  "has left the game.");
	
	public int reason = 0;
	public String message = "";
	
	private LogoutReason (int reason, String message) {
		this.reason = reason;
		this.message = message;
	}
	
	public String toString () {
		return message;
	}
	
	public static LogoutReason get (int reason) {
		for (LogoutReason logoutReason : LogoutReason.values()) {
			if (logoutReason.reason == reason)
				return logoutReason;
		}
		return NONE;
	}
	
}
