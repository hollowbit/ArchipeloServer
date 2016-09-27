package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class ControlsPacket extends Packet {
		
	public boolean[] controls;

	public ControlsPacket () {
		super(PacketType.CONTROLS);
	}
	
}
