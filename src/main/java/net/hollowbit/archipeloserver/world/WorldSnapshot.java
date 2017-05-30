package net.hollowbit.archipeloserver.world;

import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.network.packets.WorldSnapshotPacket;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.MapSnapshot;

public class WorldSnapshot {
	
	public static final int TYPE_INTERP = 0;
	public static final int TYPE_CHANGES = 1;
	public static final int TYPE_FULL = 2;
	
	public long timeCreatedMillis;
	public int time;
	public int type;
	public HashMap<String, EntitySnapshot> entitySnapshots;
	public MapSnapshot mapSnapshot;
	
	public WorldSnapshot (World world, Map map, int type) {
		this.time = world.getTime();
		this.type = type;
		
		entitySnapshots = new HashMap<String, EntitySnapshot>();
		switch(type) {
		case TYPE_INTERP:
			for (Entity entity : map.getEntityManager().duplicateEntityList()) {
				entitySnapshots.put(entity.getName(), entity.getInterpSnapshot());
			}
			break;
		case TYPE_CHANGES:
			mapSnapshot = map.getChangesSnapshot();
			for (Entity entity : map.getEntityManager().duplicateEntityList()) {
				if (!entity.getChangesSnapshot().isEmpty())
					entitySnapshots.put(entity.getName(), entity.getChangesSnapshot());
			}
			break;
		case TYPE_FULL:
			mapSnapshot = map.getFullSnapshot();
			for (Entity entity : map.getEntityManager().duplicateEntityList()) {
				entitySnapshots.put(entity.getName(), entity.getFullSnapshot());
			}
			timeCreatedMillis = System.currentTimeMillis();
			break;
		}
		
		timeCreatedMillis = System.currentTimeMillis();
	}
	
	public WorldSnapshotPacket getPacket () {
		return new WorldSnapshotPacket(this);
	}
	
	public void clear () {
		for (EntitySnapshot snapshot: entitySnapshots.values()) {
			snapshot.clear();
		}
		
		mapSnapshot.clear();
	}
	
}
