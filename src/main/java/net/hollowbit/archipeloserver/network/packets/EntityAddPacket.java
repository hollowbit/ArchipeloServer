package net.hollowbit.archipeloserver.network.packets;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class EntityAddPacket extends Packet {
	
	public String name;
	public String type;
	public String anim;
	public String animMeta;
	public float animTime;
	public HashMap<String, String> properties;
	
	public EntityAddPacket (Entity entity) {
		super(PacketType.ENTITY_ADD);
		EntitySnapshot snapshot = entity.getFullSnapshot();
		name = snapshot.name;
		type = snapshot.type;
		properties = snapshot.properties;
		anim = snapshot.anim;
		animMeta = snapshot.animMeta;
		animTime = snapshot.animTime;
	}
	
}
