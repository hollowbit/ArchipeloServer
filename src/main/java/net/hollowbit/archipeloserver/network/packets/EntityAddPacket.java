package net.hollowbit.archipeloserver.network.packets;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class EntityAddPacket extends Packet {
	
	public String username;
	public String type;
	public int style;
	public HashMap<String, String> properties;
	
	public EntityAddPacket (Entity entity) {
		super(PacketType.ENTITY_ADD);
		EntitySnapshot snapshot = entity.getFullSnapshot();
		username = snapshot.name;
		type = snapshot.entityType;
		style = snapshot.style;
		properties = snapshot.properties;
	}
	
}
