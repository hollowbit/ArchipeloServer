package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class EntityRemovePacket extends Packet {
	
	public String name;
	
	public EntityRemovePacket (Entity entity) {
		super(PacketType.ENTITY_REMOVE);
		name = entity.getName();
	}
	
}
