package net.hollowbit.archipeloserver.tools.npcdialogs;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.tools.conditions.ConditionCommand;
import net.hollowbit.archipeloserver.tools.executables.ExecutionCommand;

public class NpcDialog {
	public String id;
	public ConditionCommand cond;
	public String change = "";
	public ArrayList<ExecutionCommand> exec = new ArrayList<ExecutionCommand>();
	public ArrayList<String> choices = new ArrayList<String>();
}