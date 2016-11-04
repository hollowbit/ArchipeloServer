package net.hollowbit.archipeloserver.tools;

import java.util.HashMap;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;

public class ConditionManager {
	
	private HashMap<String, Condition> conditions;
	private Json json;
	
	public ConditionManager () {
		conditions = new HashMap<String, Condition>();
		json = new Json();
		load();
	}
	
	public boolean isConditionMet (String id, Player player, HashMap<String, String> arguments) {
		if (conditions.containsKey(id.toUpperCase()))
			return conditions.get(id.toUpperCase()).isMet(player, arguments);
		else {
			ArchipeloServer.getServer().getLogger().caution("Unspecified condition being used: " + id.toUpperCase());
			return false;
		}
	}
	
	public boolean isConditionMet (String conditionCommandJson, Player player) {
		ConditionCommand command = parseCommandStirng(conditionCommandJson);
		return this.isConditionMet(command, player);
	}
	
	public boolean isConditionMet (ConditionCommand conditionCommand, Player player) {
		if (conditions.containsKey(conditionCommand.id.toUpperCase()))
			return conditions.get(conditionCommand.id.toUpperCase()).isMet(player, conditionCommand.args);
		else {
			ArchipeloServer.getServer().getLogger().caution("Unspecified condition being used: " + conditionCommand.id.toUpperCase());
			return false;
		}
	}
	
	public ConditionCommand parseCommandStirng (String conditionCommandJson) {
		return json.fromJson(ConditionCommand.class, conditionCommandJson);
	}
	
	public interface Condition {
		
		/**
		 * Determines if a player meets a certain condition
		 * @param player
		 * @param args
		 * @return
		 */
		public boolean isMet (Player player, HashMap<String, String> args);
		
	}
	
	public class ConditionCommand {
		
		public String id;
		public HashMap<String, String> args;
		
	}
	
	private void load () {
		
		conditions.put("HASFLAG", new Condition() {

			@Override
			public boolean isMet(Player player, HashMap<String, String> args) {
				if (args.containsKey("flag")) {
					return player.getFlagsManager().hasFlag(args.get("flag"));
				} else
					return false;
			}
			
		});
		
	}
	
}
