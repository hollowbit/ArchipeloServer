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
import net.hollowbit.archipeloserver.tools.executables.ExecutionCommand;
import net.hollowbit.archipeloserver.tools.npcdialogs.NpcDialog;

public class PlayerNpcDialogManager implements PacketHandler {
	
	private Player player;
	private ArrayList<String> allowedLinks;
	private Entity lastSender;
	
	public PlayerNpcDialogManager (Player player) {
		this.player = player;
		allowedLinks = new ArrayList<String>();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}

	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (address.equals(player.getAddress())) {//Make sure it belongs to this player
			if (packet.packetType == PacketType.NPC_DIALOG_REQUEST) {
				NpcDialogRequestPacket npcDialogRequestPacket = (NpcDialogRequestPacket) packet;
				
				if (!allowedLinks.contains(npcDialogRequestPacket.messageId))//If link requested by player is not allowed, don't send response
					return true;
				
				sendNpcDialog(lastSender, npcDialogRequestPacket.messageId);
				return true;
			}
		}
		return false;
	}
	
	public void dispose () {
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
	}
	
	/**
	 * Send dialog using id
	 * @param messageId
	 */
	public void sendNpcDialog (Entity sender, String messageId) {
		NpcDialog dialog = ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(messageId);
		
		//While conditions are met on all dialogs, get the next one and test
		while (dialog != null && dialog.cond != null && dialog.cond.isConditionMet(player))
			dialog = ArchipeloServer.getServer().getNpcDialogManager().getNpcDialogById(dialog.change);
		
		if (dialog == null)//Don't send if somehow we reached a null dialog
			return;
		System.out.println("PlayerNpcDialogManager.java   " + dialog.id + "  " + messageId);
		handleMessage(sender, dialog);
		player.sendPacket(new NpcDialogPacket(dialog));
	}
	
	/**
	 * Send plain message
	 * @param name
	 * @param messages
	 * @param interruptable Can player move to exit dialog?
	 */
	public void sendNpcDialog (String name, ArrayList<String> messages, boolean interruptable) {
		player.sendPacket(new NpcDialogPacket(name, messages, interruptable));
	}
	
	/**
	 * This is run when a certain dialog is shown
	 * @param messageId
	 */
	private void handleMessage (Entity sender, NpcDialog npcDialog) {
		this.lastSender = sender;
		
		allowedLinks.clear();
		if (npcDialog.choices != null)
			allowedLinks.addAll(npcDialog.choices);//Add choices for current npc dialog to allowed choices by player
		
		//Execute commands from packet
		for (ExecutionCommand command : npcDialog.exec)
			command.execute(sender, player);
	}
	
}
