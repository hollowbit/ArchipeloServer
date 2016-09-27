package net.hollowbit.archipeloserver.network;

import org.java_websocket.WebSocket;

import net.hollowbit.archipeloserver.ArchipeloServer;

public abstract class Packet {
	
	public int packetType;
	
	public Packet (int type) {
		this.packetType = type;
	}
	
	public void send (WebSocket conn) {
		ArchipeloServer.getServer().getNetworkManager().sendPacket(this, conn);
	}
	
}
