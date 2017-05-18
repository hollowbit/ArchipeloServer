package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class PlayerStatsPacket extends Packet {
	
	public float health = 0;
	
	public PlayerStatsPacket() {
		super(PacketType.PLAYER_STATS);
	}

}
