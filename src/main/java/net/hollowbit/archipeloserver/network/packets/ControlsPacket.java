package net.hollowbit.archipeloserver.network.packets;

import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketType;

public class ControlsPacket extends Packet {
	
	public String c;
	public int id;
	
	//Only used in client
	public float x = 0, y = 0;
	public long time = 0;

	public ControlsPacket () {
		super(PacketType.CONTROLS);
	}
	
	public boolean[] parse() {
		boolean[] controls = new boolean[c.length()];
		for (int i = 0; i < controls.length; i++) {
			controls[i] = (c.charAt(i) == '0' ? false : true);
		}
		return controls;
	}
	
}
