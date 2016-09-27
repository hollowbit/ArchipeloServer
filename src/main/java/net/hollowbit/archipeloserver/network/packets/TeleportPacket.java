package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class TeleportPacket extends Packet {
	
	public String username;
	public float x, y;
	public int direction;
	public boolean newMap;
	
	public TeleportPacket (String name, float x, float y, int direction, boolean newMap) {
		super(PacketType.TELEPORT);
		this.username = name;
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.newMap = newMap;
	}
	
}
