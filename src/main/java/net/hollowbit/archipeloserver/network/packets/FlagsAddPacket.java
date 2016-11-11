package net.hollowbit.archipeloserver.network.packets;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class FlagsAddPacket extends Packet {

	public ArrayList<String> flags;
	
	public FlagsAddPacket () {
		super(PacketType.FLAGS_ADD);
	}
	
	public FlagsAddPacket (ArrayList<String> flags) {
		this();
		this.flags = flags;
	}
	
	public FlagsAddPacket (String flag) {
		this();
		this.flags = new ArrayList<String>();
		this.flags.add(flag);
	}

}
