package net.hollowbit.archipeloserver.world;

import java.util.HashMap;

public class MapSnapshot {
	
	public String name;
	public String displayName;
	public String[][] tileData;
	public String[][] elementData;
	public HashMap<String, String> properties;
	
	public MapSnapshot (String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
		properties = new HashMap<String, String>();
	}
	
	public void setTileData (String[][] tileData) {
		this.tileData = tileData;
	}
	
	public void setElementData (String[][] elementData) {
		this.elementData = elementData;
	}
	
	public void putFloat (String key, float value) {
		properties.put(key, "" + value);
	}
	
	public void putString (String key, String value) {
		properties.put(key, value);
	}
	
	public void putInt (String key, int value) {
		properties.put(key, "" + value);
	}
	
	public void putBoolean (String key, boolean value) {
		properties.put(key, "" + value);
	}
	
	public void clear () {
		properties.clear();
		tileData = null;
		elementData = null;
	}
	
}
