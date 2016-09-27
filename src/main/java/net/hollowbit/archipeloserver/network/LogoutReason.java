package net.hollowbit.archipeloserver.network;

public enum LogoutReason {
	
	NONE(0, ""),
	KICK(1, "Was kicked for:");
	
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
