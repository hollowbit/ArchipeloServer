package net.hollowbit.archipeloserver.world;

import java.util.ArrayList;
import java.util.Collection;

import net.hollowbit.archipeloserver.entity.living.Player;

public class Island {
	
	public static final float TICK = 1 / 30f;
	
	private String name;
	private World world;
	private ArrayList<Map> loadedMaps;
	private boolean loaded = false;
	
	public Island (String name, World world) {
		this.name = name;
		this.world = world;
		loadedMaps = new ArrayList<Map>();
	}
	
	public Collection<Player> getPlayers () {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Map map : loadedMaps) {
			players.addAll(map.getPlayers());
		}
		return players;
	}
	
	public void tick20 () {
		for (Map map : duplicateMapList()) {
			map.tick20();
		}
	}
	
	public void tick60 () {
		for (Map map : duplicateMapList()) {
			map.tick60();
		}
	}
	
	public synchronized void addMap (Map map) {
		loadedMaps.add(map);
	}
	
	public synchronized void removeMap (Map map) {
		loadedMaps.remove(map);
	}
	
	public boolean load () {
		loaded = true;
		return true;
	}
	
	public void unload () {}
	
	public boolean loadMap (String name) {
		Map map = new Map(name, this);
		if (map.load()) {
			addMap(map);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean unloadMap (Map map) {
		if (isMapLoaded(map.getName())) {
			map.unload();
			removeMap(map);
			
			//Check if there are any loaded maps left, if not unload this island.
			if (loadedMaps.isEmpty()) {
				world.unloadIsland(this);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized Map getMap (String name) {
		for (Map map : loadedMaps) {
			if (map.getName().equalsIgnoreCase(name))
				return map;
		}
		return null;
	}
	
	public synchronized ArrayList<Map> duplicateMapList () {
		ArrayList<Map> mapList = new ArrayList<Map>();
		mapList.addAll(loadedMaps);
		return mapList;
	}
	
	public String getName () {
		return name;
	}
	
	public World getWorld () {
		return world;
	}
	
	public Collection<Map> getMaps () {
		return loadedMaps;
	}
	
	public boolean isLoaded () {
		return loaded;
	}
	
	public boolean isMapLoaded (String mapName) {
		for (Map map : loadedMaps) {
			if (map.getName().equalsIgnoreCase(mapName))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Island))
			return false;
		
		Island island = (Island) obj;
		
		return island.getName().equals(name);
	}
	
}
