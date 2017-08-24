package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class WorldSnapshotPacket extends Packet {
	
	public static final int NUM_OF_CHUNKS = 9;//Must be a perfect square
	public static final int NUM_OF_CHUNKS_WIDE = (int) Math.sqrt(NUM_OF_CHUNKS);//Must be a perfect square
	
	public static final int TYPE_INTERP = 0;
	public static final int TYPE_CHANGES = 1;
	public static final int TYPE_FULL = 2;
	
	public double timeCreatedMillis;
	public boolean newMap = false;
	public int time;
	public int type = 0;
	public String mapSnapshot;
	public String[] chunks;
	public String[] entities;
	
	public WorldSnapshotPacket() {
		super(PacketType.WORLD_SNAPSHOT);
	}
	
	public WorldSnapshotPacket(long timeCreatedMillis, int time, int type) {
		this();
		this.timeCreatedMillis = (double) timeCreatedMillis;
		this.time = time;
		this.type = type;
		this.chunks = new String[NUM_OF_CHUNKS];
		this.entities = new String[NUM_OF_CHUNKS];
	}

}
