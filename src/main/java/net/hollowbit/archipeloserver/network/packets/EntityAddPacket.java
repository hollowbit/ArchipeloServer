package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloshared.EntitySnapshot;

public class EntityAddPacket extends Packet {
	
	public EntitySnapshot snapshot;
	
	public EntityAddPacket (Entity entity) {
		super(PacketType.ENTITY_ADD);
		snapshot = entity.getFullSnapshot();
	}
	
}
