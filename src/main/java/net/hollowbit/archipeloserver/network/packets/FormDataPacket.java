package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloshared.FormData;

public class FormDataPacket extends Packet {
	
	public FormData data;
	
	public FormDataPacket () {
		super(PacketType.FORM_DATA);
	}
	
	public FormDataPacket (FormData formData) {
		this();
		this.data = formData;
	}
	
}
