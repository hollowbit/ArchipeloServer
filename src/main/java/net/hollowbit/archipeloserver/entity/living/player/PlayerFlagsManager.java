package net.hollowbit.archipeloserver.entity.living.player;

import java.util.ArrayList;
import java.util.HashSet;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.FlagsAddPacket;

public class PlayerFlagsManager {
	
	private HashSet<String> flags;
	private Json json;
	private Player player;
	
	/**
	 * Manages player flags. Player flags are used to determine if a player has triggered a certain event already.
	 * @param flagsJson
	 */
	public PlayerFlagsManager (String[] flagsData, Player player) {
		this.player = player;
		json = new Json();
		
		flags = new HashSet<String>();
		for (String flag : flagsData)
			flags.add(flag);
	}
	
	/**
	 * Returns the json array of flags ready to save in the database
	 * @return
	 */
	public String getFlagsJson () {
		String[] flagsArray = new String[flags.size()];
		
		int i = 0;
		for (String flag : flags) {
			flagsArray[i] = flag;
			i++;
		}
		
		return json.toJson(flagsArray);
	}
	
	/**
	 * Creates a list of flags to send to player
	 * @return
	 */
	public ArrayList<String> getFlagsList() {
		ArrayList<String> flags = new ArrayList<String>();
		
		for (String flag : this.flags)
			flags.add(flag);
		return flags;
	}
	
	/**
	 * Checks if player has a specified flag
	 * @param flag
	 * @return
	 */
	public boolean hasFlag (String flag) {
		return flags.contains(flag);
	}
	
	/**
	 * Adds a flag to list
	 * @param flag
	 */
	public void addFlag (String flag) {
		this.flags.add(flag);
		
		player.sendPacket(new FlagsAddPacket(flag));
	}
	
}
