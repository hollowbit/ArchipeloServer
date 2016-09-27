package net.hollowbit.archipeloserver.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.hollowbit.archipeloshared.ElementData;
import net.hollowbit.archipeloshared.ElementList;
import net.hollowbit.archipeloshared.TileData;
import net.hollowbit.archipeloshared.TileList;

public class MapElementLoader {
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Tile> loadTiles () {
		InputStream in = getClass().getResourceAsStream("/map-elements/tiles.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String fileString = "";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				fileString += line;
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String, Tile> tiles = new HashMap<String, Tile>();
		Json json = new Json();
		TileData[] tileList = null;
		try {
			tileList = ((TileList) json.fromJson(ClassReflection.forName("net.hollowbit.archipeloshared.TileList"), fileString)).tileList;
		} catch (ReflectionException e) {
			System.out.println("Was unable to load tile data.");
			e.printStackTrace();
		}
		
		for (TileData data : tileList) {
			tiles.put(data.id, new Tile(data));
		}
		
		return tiles;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, MapElement> loadElements () {
		InputStream in = getClass().getResourceAsStream("/map-elements/elements.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String fileString = "";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				fileString += line;
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		HashMap<String, MapElement> elements = new HashMap<String, MapElement>();
		Json json = new Json();
		ElementData[] elementList = null;
		try {
			elementList = ((ElementList) json.fromJson(ClassReflection.forName("net.hollowbit.archipeloshared.ElementList"), fileString)).elementList;
		} catch (ReflectionException e) {
			System.out.println("Was unable to load element data.");
			e.printStackTrace();
		}
		
		for (ElementData data : elementList) {
			elements.put(data.id, new MapElement(data));
		}
		
		return elements;
	}
	
}
