package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class ChatMessagePacket extends Packet {

	public String prefix;
	public String message;
	public String sender;
	
	public ChatMessagePacket () {
		super(PacketType.CHAT_MESSAGE);
	}
	
	public ChatMessagePacket (String prefix, String message, String sender) {
		this();
		this.prefix = prefix;
		this.message = message;
		this.sender = sender;
	}

}
