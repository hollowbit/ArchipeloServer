package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class PositionCorrectionPacket extends Packet {
	
	public float x;
	public float y;
	public int id;
	
	public PositionCorrectionPacket() {
		super(PacketType.POSITION_CORRECTION);
	}
	
	public PositionCorrectionPacket(float x, float y, int id) {
		this();
		this.x = x;
		this.y = y;
		this.id = id;
	}

}
