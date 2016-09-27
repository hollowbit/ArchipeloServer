package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class LogoutPacket extends Packet {
	
	public static final int REASON_NONE = 0;
	public static final int REASON_KICK = 1;
	
	public int reason = 0;
	public String alt;//Alt reason, defined by user. Can be used to say why a player was kicked.
	
	public LogoutPacket () {
		super(PacketType.LOGOUT);
	}
	
	public LogoutPacket (int reason) {
		this(reason, "");
	}
	
	public LogoutPacket (int reason, String alt) {
		super(PacketType.LOGOUT);
		this.reason = reason;
		this.alt = alt;
	}
	
}
