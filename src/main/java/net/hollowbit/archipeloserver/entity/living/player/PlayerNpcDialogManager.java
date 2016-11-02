package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.NpcDialogPacket;
import net.hollowbit.archipeloserver.network.packets.NpcDialogRequestPacket;

public class PlayerNpcDialogManager implements PacketHandler {
	
	private Player player;
	
	public PlayerNpcDialogManager (Player player) {
		this.player = player;
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}

	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (address.equals(player.getAddress())) {//Make sure it belongs to this player
			if (packet.packetType == PacketType.NPC_DIALOG_REQUEST) {
				NpcDialogRequestPacket npcDialogRequestPacket = (NpcDialogRequestPacket) packet;
				handleMessage(npcDialogRequestPacket.messageId);
				//player.sendPacket(new NpcDialogPacket(npcDialogRequestPacket.messageId));
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send dialog using id
	 * @param messageId
	 */
	public void sendNpcDialog (String messageId, ArrayList<String> choices, ArrayList<String> links) {
		handleMessage(messageId);
		player.sendPacket(new NpcDialogPacket(messageId, choices, links));
	}
	
	/**
	 * Send plain message
	 * @param name
	 * @param messages
	 */
	public void sendNpcDialog (String name, ArrayList<String> messages) {
		player.sendPacket(new NpcDialogPacket(name, messages));
	}
	
	/**
	 * This is run when a certain dialog is shown
	 * @param messageId
	 */
	private void handleMessage (String messageId) {
		
	}
	
}
