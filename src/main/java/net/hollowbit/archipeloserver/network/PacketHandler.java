package net.hollowbit.archipeloserver.network;

public interface PacketHandler {
	
	public abstract boolean handlePacket (Packet packet, String address);
	
}
