package net.hollowbit.archipeloserver.items;

import net.hollowbit.archipeloserver.network.Packet;

public class PacketWrapper {
	
	public String address;
	public Packet packet;
	public long time;
	
	public PacketWrapper (String address, Packet packet) {
		this.address = address;
		this.packet = packet;
		this.time = System.currentTimeMillis();
	}
	
}
