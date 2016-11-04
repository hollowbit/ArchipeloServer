package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.NpcDialogPacket;
import net.hollowbit.archipeloserver.network.packets.NpcDialogRequestPacket;
import net.hollowbit.archipeloserver.tools.ExecutableManager.ExecutionCommand;
import net.hollowbit.archipeloserver.tools.NpcDialogManager.NpcDialog;

public class PlayerNpcDialogManager implements PacketHandler {
	
	private Player player;
	private ArrayList<String> allowedLinks;
	private Entity lastSender;
	
	public PlayerNpcDialogManager (Player player) {
		this.player = player;
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}

	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (address.equals(player.getAddress())) {//Make sure it belongs to this player
			if (packet.packetType == PacketType.NPC_DIALOG_REQUEST) {
				NpcDialogRequestPacket npcDialogRequestPacket = (NpcDialogRequestPacket) packet;
				
				if (!allowedLinks.contains(npcDialogRequestPacket.messageId))//If link requested by player is not allowed, don't send response
					return true;

				NpcDialog dialog = ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(npcDialogRequestPacket.messageId);
				
				//While conditions are met on all dialogs, get the next one and test
				while (player.getFlagsManager().hasFlag(dialog.cond))
					dialog = ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(dialog.change);
				
				handleMessage(lastSender, dialog);
				player.sendPacket(new NpcDialogPacket());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send dialog using id
	 * @param messageId
	 */
	public void sendNpcDialog (Entity sender, String messageId) {
		handleMessage(sender, ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(messageId));
		player.sendPacket(new NpcDialogPacket(messageId));
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
	private void handleMessage (Entity sender, NpcDialog npcDialog) {
		this.lastSender = sender;
		
		//Execute commands from packet
		for (ExecutionCommand command : npcDialog.execCommands)
			command.execute(sender, player);
	}
	
}
