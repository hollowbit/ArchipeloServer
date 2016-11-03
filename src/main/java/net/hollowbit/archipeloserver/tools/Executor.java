package net.hollowbit.archipeloserver.tools;

import java.util.HashMap;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.living.Player;

public class Executor {
	
	private HashMap<String, Executable> executables;
	private Json json;
	
	public Executor () {
		executables = new HashMap<String, Executable>();
		json = new Json();
		load();
	}
	
	/**
	 * Execute a command with a specified id. Returns whether successful or not.
	 * @param id
	 * @param sender
	 * @param target
	 * @param arguments
	 * @return
	 */
	public boolean execute (String id, Entity sender, Entity target, HashMap<String, String> arguments) {
		if (executables.containsKey(id))
			return executables.get(id).execute(sender, target, arguments);
		else
			return false;
	}
	
	/**
	 * Execute and unparsed command. Returns whether successful or not.
	 * @param executionCommandJson
	 * @param sender
	 * @param target
	 * @return
	 */
	public boolean execute (String executionCommandJson, Entity sender, Entity target) {
		ExecutionCommand command = parseCommandString(executionCommandJson);
		if (executables.containsKey(command.id))
			return executables.get(command.id).execute(sender, target, command.args);
		else
			return false;
	}
	
	/**
	 * Execute a command with an already parse execution command
	 * @param executionCommand
	 * @param sender
	 * @param target
	 * @return
	 */
	public boolean execute (ExecutionCommand executionCommand, Entity sender, Entity target) {
		if (executables.containsKey(executionCommand.id))
			return executables.get(executionCommand.id).execute(sender, target, executionCommand.args);
		else
			return false;
	}
	
	/**
	 * Parses a command string json
	 * @param executionCommandJson
	 * @return
	 */
	public ExecutionCommand parseCommandString (String executionCommandJson) {
		return json.fromJson(ExecutionCommand.class, executionCommandJson);
	}
	
	public interface Executable {
		
		public abstract boolean execute (Entity sender, Entity target, HashMap<String, String> arguments);
		
	}
	
	public class ExecutionCommand  {
		
		public String id;
		public HashMap<String, String> args;
		
		
		public void execute (Entity sender, Entity target) {
			ArchipeloServer.getServer().getExecutor().execute(this, sender, target);
		}
	}
	
	/**
	 * Loads all executables
	 */
	private void load() {
		executables.put("addCondition", new Executable() {
			
			@Override
			public boolean execute(Entity sender, Entity target, HashMap<String, String> arguments) {
				if (target != null && target instanceof Player) {
					Player player = (Player) target;
					
					if (arguments.containsKey("cond")) {
						player.getConditionsManager().setCondition(arguments.get("cond"));//Set condition to player if available
						return true;
					}else
						return false;
				} else
					return false;
			}
		});
		
		//TODO add more executables here
	}
	
}
