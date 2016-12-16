package net.hollowbit.archipeloserver.network.packets;

import java.util.HashMap;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class FormInteractPacket extends Packet {
	
	public String id;
	public HashMap<String, String> data;
	public boolean close = false;
	
	public FormInteractPacket () {
		super(PacketType.FORM_INTERACT);
	}
}
