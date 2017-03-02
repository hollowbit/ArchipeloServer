package net.hollowbit.archipeloserver.tools.executables;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;

public class ExecutionCommand  {
	
	public String id;
	public HashMap<String, String> args;
	
	public void execute (Entity sender, Entity target) {
		ArchipeloServer.getServer().getExecutableManager().execute(this, sender, target);
	}
}