package net.hollowbit.archipeloserver.network.packets;

import java.util.HashMap;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class FormRequestPacket extends Packet {
	
	public String type;
	public HashMap<String, String> data;
	
	public FormRequestPacket () {
		super(PacketType.FORM_REQUEST);
	}

}
