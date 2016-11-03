package net.hollowbit.archipeloserver.network.packets;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class NpcDialogPacket extends Packet {

	public String messageId;
	public ArrayList<String> links;
	
	public String name;
	public ArrayList<String> messages;
	
	public boolean usesId = false;
	
	public NpcDialogPacket () {
		super(PacketType.NPC_DIALOG);
	}
	
	public NpcDialogPacket (String messageId) {
		this();
		this.messageId = messageId;
		this.links = ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(messageId).choices;
		this.usesId = true;
	}
	
	public NpcDialogPacket (String name, ArrayList<String> messages) {
		this();
		this.name = name;
		this.messages = messages;
		this.usesId = false;
	}

}
