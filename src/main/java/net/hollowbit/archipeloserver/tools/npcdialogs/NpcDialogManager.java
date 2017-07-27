package net.hollowbit.archipeloserver.tools.npcdialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.world.Map;

public class NpcDialogManager {

	private HashMap<String, NpcDialog> npcDialogs;
	
	public NpcDialogManager (Map map) {
		npcDialogs = new HashMap<String, NpcDialog>();
		
		NpcDialogs npcDialogsFile = getNpcDialogsFileByMap(map);
		
		if (npcDialogsFile == null)
			return;
		
		ArrayList<NpcDialog> dialogs = npcDialogsFile.dialogs;
		
		if (dialogs != null) {
			for (NpcDialog dialog : dialogs)
				npcDialogs.put(dialog.id, dialog);
		}
	}
	
	/**
	 * Gets an NPC dialog by id. Can return a null value.
	 * @param id
	 * @return
	 */
	public NpcDialog getNpcDialogById (String id) {
		return npcDialogs.get(id);
	}
	
	private static NpcDialogs getNpcDialogsFileByMap (Map map) {
		File dialogFile = new File("maps/" + map.getName() + "/npc_dialogs.json");
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
				npcDialogs = (NpcDialogs) json.fromJson(NpcDialogs.class, file);
				
				scanner.close();
				return npcDialogs;
			} catch (Exception e) {
				unableToLoad = true;
			}
			
			if (unableToLoad)
				System.out.println("Could not load NPC dialogs for " + map.getName());
			return null;
		} else {
			return null;
		}
	}
	
}
