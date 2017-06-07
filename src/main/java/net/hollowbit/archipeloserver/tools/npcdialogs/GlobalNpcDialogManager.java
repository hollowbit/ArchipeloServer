package net.hollowbit.archipeloserver.tools.npcdialogs;

import java.util.HashMap;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.FileReader;
import net.hollowbit.archipeloserver.tools.StaticTools;

public class GlobalNpcDialogManager {
	
	private static final String INDEX_FILE_PATH = "npcdialogs/npcdialogs.json";
	private static final String NPC_DIALOGS_FOLDER = "npcdialogs/dialogs";
	
	private HashMap<String, HashMap<String, NpcDialog>> npcDialogs;
	
	public GlobalNpcDialogManager () {
		npcDialogs = new HashMap<String, HashMap<String, NpcDialog>>();
		
		String[] dialogFiles = null;
		try {
			dialogFiles = StaticTools.getJson().fromJson(String[].class, FileReader.readFileIntoString(INDEX_FILE_PATH));
		} catch (Exception e) {
			ArchipeloServer.getServer().getLogger().error("Global NPC Dialog Indexer file not found. Please place one at " + INDEX_FILE_PATH);
		}
			
		if (dialogFiles == null)
			return;
		
		for (String dialogFile : dialogFiles) {
			NpcDialogs dialogs = null;
			try {
				dialogs = StaticTools.getJson().fromJson(NpcDialogs.class, FileReader.readFileIntoString(NPC_DIALOGS_FOLDER + "/" + dialogFile + ".json"));
			} catch (Exception e) {
				ArchipeloServer.getServer().getLogger().caution("Global NPC Dialog file called " + dialogFile + " not found.");
			}
			
			if (dialogs != null) {
				HashMap<String, NpcDialog> map = new HashMap<String, NpcDialog>();
				npcDialogs.put(dialogFile, map);
				for (NpcDialog dialog : dialogs.dialogs)
					map.put(dialog.id, dialog);
			}
		}
	}
	
	/**
	 * Gets an NPC dialog by id. Can return a null value.
	 * @param id
	 * @return
	 */
	public NpcDialog getNpcDialogById (String prefix, String id) {
		try {
			return npcDialogs.get(prefix).get(id);
		} catch (Exception e) {
			ArchipeloServer.getServer().getLogger().caution("An NPC Dialog with prefix " + prefix + " and id " + id + " was not found.");
			return null;
		}
	}
	
}
