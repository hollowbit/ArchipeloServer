package net.hollowbit.archipeloserver.network.packets;

import java.util.HashMap;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.world.WorldSnapshot;

public class WorldSnapshotPacket extends Packet {
	
	public double timeCreatedMillis;
	public int time;
	public int type;
	public HashMap<String, String> entitySnapshots;
	public String mapSnapshot;
	
	public WorldSnapshotPacket (WorldSnapshot snapshot) {
		super(PacketType.WORLD_SNAPSHOT);
		timeCreatedMillis = (double) snapshot.timeCreatedMillis;
		time = snapshot.time;
		type = snapshot.type;
		entitySnapshots = new HashMap<String, String>();
		Json json = new Json();
		for (Entry<String, EntitySnapshot> entry : snapshot.entitySnapshots.entrySet()) {
			entitySnapshots.put(entry.getKey(), json.toJson(entry.getValue()));
		}
		mapSnapshot = json.toJson(snapshot.mapSnapshot);
	}

}
