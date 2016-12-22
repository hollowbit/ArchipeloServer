package net.hollowbit.archipeloserver.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.FormDataPacket;
import net.hollowbit.archipeloserver.network.packets.FormInteractPacket;
import net.hollowbit.archipeloserver.network.packets.FormRequestPacket;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.events.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.FormData;

public class FormManager implements PacketHandler, EventHandler {
	
	private HashMap<String, Form> forms;
	private LinkedList<Form> formsList;
	private Map map;
	
	public FormManager (Map map) {
		this.map = map;
		forms = new HashMap<String, Form>();
		formsList = new LinkedList<Form>();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
		this.addToEventManager();
	}
	
	/**
	 * Add a form to the manager. Required in order to get player interactions.
	 * @param form
	 */
	public void addForm (Form form) {
		this.forms.put(form.getId(), form);
		this.formsList.add(form);
	}
	
	public Form getForm (String id) {
		return forms.get(id);
	}
	
	/**
	 * Removes a form. It is best practice to use this on all no longer used for to release them from memory
	 * @param form
	 */
	public void removeForm (Form form) {
		this.forms.remove(form.getId());
		this.formsList.remove(form);
	}

	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (packet.packetType == PacketType.FORM_INTERACT) {
			Player player = map.getWorld().getPlayerByAddress(address);
			if (player.getLocation().getMap() == map) {
				FormInteractPacket formInteractPacket = (FormInteractPacket) packet;
				Form form = this.getForm(formInteractPacket.id);
				
				if (form.canInteractWith(player))
					form.interactWith(player, formInteractPacket.command, formInteractPacket.data);
				return true;
			}
		} else if (packet.packetType == PacketType.FORM_REQUEST) {
			Player player = map.getWorld().getPlayerByAddress(address);
			if (player.getLocation().getMap() == map) {
				FormRequestPacket formRequestPacket = (FormRequestPacket) packet;
				if (FormType.getFormTypeById(formRequestPacket.type).requestable) {
					String id = formRequestPacket.type + "FormFor" + player.getName();
					
					if (!forms.containsKey(id)) {//If the player doesn't have an inventory open
						formRequestPacket.data.put("player", player.getName());
						RequestableForm form = (RequestableForm) FormType.createFormByFormData(new FormData(formRequestPacket.type, id, formRequestPacket.data), this);
						this.addForm(form);
						player.sendPacket(new FormDataPacket(form.getFormDataForClient()));
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onPlayerLeave (PlayerLeaveEvent event) {
		if (event.getPlayer().getMap() != this.map)
			return false;
		
		ArrayList<Form> formsToRemove = new ArrayList<Form>();
		for (Form form : formsList) {
			if (form.getType().requestable) {
				RequestableForm requestableForm = (RequestableForm) form;
				if (requestableForm.getPlayer() == event.getPlayer())
					formsToRemove.add(requestableForm);
			}
		}
		
		for (Form form : formsToRemove) {
			this.removeForm(form);
		}
		return true;
	}
	
	/**
	 * Properly disposes of this form manager.
	 */
	public void dispose () {
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
		this.removeFromEventManager();
	}
	
	/**
	 * Print out all form IDs. For debugging purposes.
	 */
	public void print () {
		System.out.println("Forms in FormManager:");
		for (Form form : formsList) {
			System.out.println("\t- " + form.getId());
		}
	}
	
}
