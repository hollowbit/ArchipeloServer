package net.hollowbit.archipeloserver.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.Executor.ExecutionCommand;

public class NpcDialogManager {
	

	private HashMap<String, NpcDialog> npcDialogs;
	
	public NpcDialogManager () {
		npcDialogs = new HashMap<String, NpcDialog>();
		
		for (String dialogFileName : getNpcDialogsListFromFile().npcDialogs) {
			ArrayList<NpcDialog> dialogs = getNpcDialogsFileByName(dialogFileName).dialogs;
			for (NpcDialog dialog : dialogs)
				npcDialogs.put(dialog.id, dialog);
		}
	}
	
	private NpcDialogs getNpcDialogsFileByName (String name) {
		File dialogFile = new File("npc_dialogs/" + name + ".json");
		if (dialogFile.exists()) {
			boolean unableToLoad = false;
			try {
				Scanner scanner = new Scanner(dialogFile);
				NpcDialogs npcDialogs = null;
				Json json = new Json();
				String file = "";
				
				if (!scanner.hasNext())
					unableToLoad = true;
				
				while (scanner.hasNext()) {
					file += scanner.next();
				}
				npcDialogs = (NpcDialogs) json.fromJson(NpcDialogs.class, file.trim());
					
				scanner.close();
				return npcDialogs;
			} catch (Exception e) {
				unableToLoad = true;
			}
			
			if (unableToLoad) {
				System.out.println("Could not load NPC dialogs");
				ArchipeloServer.getServer().stop();
			}
			return null;
		} else {
			return null;
		}
	}
	
	private NpcDialogsList getNpcDialogsListFromFile () {
		File dialogListFile = new File("npc_dialogs.json");
		if (dialogListFile.exists()) {
			boolean unableToLoad = false;
			try {
				Scanner scanner = new Scanner(dialogListFile);
				NpcDialogsList npcDialogsList = null;
				Json json = new Json();
				String file = "";
				
				if (!scanner.hasNext())
					unableToLoad = true;
				
				while (scanner.hasNext()) {
					file += scanner.next();
				}
				npcDialogsList = (NpcDialogsList) json.fromJson(NpcDialogsList.class, file.trim());
					
				scanner.close();
				return npcDialogsList;
			} catch (Exception e) {
				unableToLoad = true;
			}
			
			if (unableToLoad) {
				System.out.println("Could not load NPC dialogs");
				ArchipeloServer.getServer().stop();
			}
			return null;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets an NPC dialog by id
	 * @param id
	 * @return
	 */
	public NpcDialog getNpcDialogById (String id) {
		if (npcDialogs.containsKey(id))
			return npcDialogs.get(id);
		else
			return new NpcDialog();
	}
	
	public class NpcDialog {
		public String id;
		public String cond = "";
		public String change = "";
		public ArrayList<ExecutionCommand> execCommands = new ArrayList<ExecutionCommand>();
		public ArrayList<String> choices = new ArrayList<String>();
	}
	
	public class NpcDialogs {
		public ArrayList<NpcDialog> dialogs;
	}
	
	public class NpcDialogsList {
		public ArrayList<String> npcDialogs;
	}
	
}
