package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class MessagePacket extends Packet {
	
	String message;
	
	public MessagePacket (String message) {
		super(PacketType.MESSAGE);
		this.message = message;
	}
	
	@Override
	public String toString () {
		return message;
	}
	
}
