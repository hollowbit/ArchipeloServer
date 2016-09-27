package net.hollowbit.archipeloserver.network.packets;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.world.WorldSnapshot;

public class WorldSnapshotPacket extends Packet {
	
	public double timeCreatedMillis;
	public int time;
	public int type;
	public ArrayList<String> entitySnapshots;
	public String mapSnapshot;
	
	public WorldSnapshotPacket (WorldSnapshot snapshot) {
		super(PacketType.WORLD_SNAPSHOT);
		timeCreatedMillis = (double) snapshot.timeCreatedMillis;
		time = snapshot.time;
		type = snapshot.type;
		entitySnapshots = new ArrayList<String>();
		Json json = new Json();
		for (EntitySnapshot entitySnapshot : snapshot.entitySnapshots) {
			entitySnapshots.add(json.toJson(entitySnapshot));
		}
		mapSnapshot = json.toJson(snapshot.mapSnapshot);
	}

}
