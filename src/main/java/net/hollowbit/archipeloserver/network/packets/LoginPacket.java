package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class LoginPacket extends Packet {
	
	public static final int RESULT_LOGIN_SUCCESSFUL = 0;
	public static final int RESULT_LOGIN_ERROR = 1;
	public static final int RESULT_BAD_VERSION = 2;
	
	public String email;
	public String password;
	public int result;
	public String version;
	
	public LoginPacket () {
		super(PacketType.LOGIN);
	}
	
	public LoginPacket (int result) {
		super(PacketType.LOGIN);
		this.result = result;
	}
	
}
