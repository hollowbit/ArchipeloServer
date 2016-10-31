package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class NpcDialogRequestPacket extends Packet {
	
	public String messageId;
	
	public NpcDialogRequestPacket () {
		super(PacketType.NPC_DIALOG_REQUEST);
	}

}
