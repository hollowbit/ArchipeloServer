package net.hollowbit.archipeloserver.tools.npcdialogs;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.tools.conditions.ConditionCommand;
import net.hollowbit.archipeloserver.tools.executables.ExecutionCommand;
import net.hollowbit.archipeloserver.world.Map;

public class NpcDialog {
	public String id;
	public ConditionCommand cond;
	public String change = "";
	public ArrayList<ExecutionCommand> exec = new ArrayList<ExecutionCommand>();
	public ArrayList<String> choices = new ArrayList<String>();
	public boolean interruptable = false;
	
	public static String getPrefix (Map map) {
		return map.getIsland().getName() + "-" + map.getName() + "-";
	}
	
}