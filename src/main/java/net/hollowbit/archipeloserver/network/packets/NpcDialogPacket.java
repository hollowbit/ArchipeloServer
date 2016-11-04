package net.hollowbit.archipeloserver.network.packets;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.tools.npcdialogs.NpcDialog;

public class NpcDialogPacket extends Packet {
	
	public String name;
	public ArrayList<String> messages;
	public boolean interruptable = false;
	
	public boolean usesId = false;
	
	public NpcDialogPacket () {
		super(PacketType.NPC_DIALOG);
	}
	
	public NpcDialogPacket (NpcDialog dialog) {
		this();
		this.name = dialog.id;
		this.messages = dialog.choices;
		this.usesId = true;
	}
	
	public NpcDialogPacket (String name, ArrayList<String> messages, boolean interruptable) {
		this();
		this.name = name;
		this.messages = messages;
		this.interruptable = interruptable;
		this.usesId = false;
	}

}
