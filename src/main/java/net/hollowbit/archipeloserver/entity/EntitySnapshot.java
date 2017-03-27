package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class EntitySnapshot {
	
	public String name;
	public String type;
	public String anim;
	public float animTime;
	public String animMeta = "";
	public String footSound = "";
	public float footPitch = 1;
	public ArrayList<String> sounds = new ArrayList<String>();
	public HashMap<String, String> properties;
	
	public EntitySnapshot () {
		properties = new HashMap<String, String>();
	}
	
	public EntitySnapshot (Entity entity, boolean ignoreType) {
		this.name = entity.getName();
		if (ignoreType)
			this.type = null;
		else
			this.type = entity.getEntityType().getId();
		properties = new HashMap<String, String>();
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
	
	public float getFloat (String key, float currentValue) {
		if (!properties.containsKey(key))
			return currentValue;
		try {
			return Float.parseFloat(properties.get(key));
		} catch (Exception e) {
			return currentValue;
		}
	}
	
	public String getString (String key, String currentValue) {
		if (!properties.containsKey(key))
			return currentValue;
		
		return properties.get(key);
	}

	public int getInt (String key, int currentValue) {
		if (!properties.containsKey(key))
			return currentValue;
		try {
			return Integer.parseInt(properties.get(key));
		} catch (Exception e) {
			return currentValue;
		}
	}
	
	public boolean getBoolean (String key, boolean currentValue) {
		if (!properties.containsKey(key))
			return currentValue;
		try {
			return Boolean.parseBoolean(properties.get(key));
		} catch (Exception e) {
			return currentValue;
		}
	}
	
	public void setSound(String sound) {
		this.footSound = sound;
	}
	
	public void addSound(String sound) {
		sounds.add(sound);
	}
	
	public void clear () {
		properties.clear();
		sounds.clear();
	}
	
	public boolean isEmpty () {
		return properties.isEmpty() && sounds.isEmpty();
	}
	
}
