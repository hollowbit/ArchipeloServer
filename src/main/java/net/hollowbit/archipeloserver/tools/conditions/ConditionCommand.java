package net.hollowbit.archipeloserver.tools.conditions;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;

public class ConditionCommand {
	
	public String id;
	public HashMap<String, String> args;
	
	public boolean isConditionMet (Player player) {
		return ArchipeloServer.getServer().getConditionManager().isConditionMet(this, player);
	}
	
}