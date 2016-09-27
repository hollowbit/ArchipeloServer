package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class PopupTextPacket extends Packet {
	
	public enum Type { NORMAL }
	
	public String text;
	public int type;
	
	public PopupTextPacket (String text, Type type) {
		super(PacketType.POPUP_TEXT);
		this.text = text;
		this.type = type.ordinal();
	}
	
}
