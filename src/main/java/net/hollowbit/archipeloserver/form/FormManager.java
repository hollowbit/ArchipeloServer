package net.hollowbit.archipeloserver.form;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.FormDataPacket;
import net.hollowbit.archipeloserver.network.packets.FormInteractPacket;
import net.hollowbit.archipeloserver.network.packets.FormRequestPacket;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.FormData;

public class FormManager implements PacketHandler {
	
	private HashMap<String, Form> forms;
	private Map map;
	
	public FormManager (Map map) {
		this.map = map;
		forms = new HashMap<String, Form>();
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}
	
	public void addForm (Form form) {
		this.forms.put(form.getId(), form);
	}
	
	public Form getForm (String id) {
		return forms.get(id);
	}
	
	public void removeForm (Form form) {
		this.removeForm(form.getId());
	}
	
	public void removeForm (String id) {
		forms.remove(id);
	}

	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (packet.packetType == PacketType.FORM_INTERACT) {
			Player player = map.getWorld().getPlayerByAddress(address);
			if (player.getLocation().getMap() == map) {
				FormInteractPacket formInteractPacket = (FormInteractPacket) packet;
				Form form = this.getForm(formInteractPacket.id);
				
				if (form.canInteractWith(player)) {
					if (formInteractPacket.close) {
						form.close();
						this.removeForm(formInteractPacket.id);
					} else {
						form.interactWith(player, formInteractPacket.data);
					}
				}
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
						RequestableForm form = (RequestableForm) FormType.createFormByFormData(new FormData(formRequestPacket.type, id, formRequestPacket.data));
						this.addForm(form);
						player.sendPacket(new FormDataPacket(form.getFormDataForClient()));
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public void dispose () {
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
	}
	
}
