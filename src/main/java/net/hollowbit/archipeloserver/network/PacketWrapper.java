package net.hollowbit.archipeloserver.network;

public class PacketWrapper {
	
	public String address;
	public Packet packet;
	
	public PacketWrapper (String address, Packet packet) {
		this.address = address;
		this.packet = packet;
	}
	
}
