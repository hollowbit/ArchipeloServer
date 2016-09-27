package net.hollowbit.archipeloserver.world.map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.world.Map;

public class MapLoader {
	
	public static MapData loadMap (Map map) {
		File mapFile = new File("islands/" + map.getIsland().getName() + "/" + map.getName() + ".json");
		if (mapFile.exists()) {
			boolean unableToLoad = false;
			try {
				Scanner scanner = new Scanner(mapFile);
				MapData mapData = null;
				Json json = new Json();
				String file = "";
				
				if (!scanner.hasNext())
					unableToLoad = true;
				
				while (scanner.hasNext()) {
					file += scanner.next();
				}
				mapData = (MapData) json.fromJson(MapData.class, file.trim());
					
				scanner.close();
				return mapData;
			} catch (Exception e) {
				unableToLoad = true;
			}
			
			if (unableToLoad)
				System.out.println("Could not load map: " + map.getName());
			return null;
		} else {
			return null;
		}
	}
	
	public static void saveMap (Map map) {
		File mapFile = new File("islands/" + map.getIsland().getName() + "/" + map.getName() + ".json");
		
		if(!mapFile.exists()) {
			mapFile.getParentFile().mkdirs();
			try {
				mapFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Could not save map: " + map.getName());
				return;
			}
		}
		
		Formatter formatter = null;
		try {
			formatter = new Formatter(mapFile);
		} catch (FileNotFoundException e) {
			System.out.println("Could not save map: " + map.getName());
			return;
		}
		Json json = new Json();
		formatter.format("%s", json.toJson(new MapData(map)).replaceAll("],", "],\n"));
		formatter.flush();
		formatter.close();
	}
	
}
