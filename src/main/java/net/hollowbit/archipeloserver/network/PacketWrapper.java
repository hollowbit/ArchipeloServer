package net.hollowbit.archipeloserver.network;

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
