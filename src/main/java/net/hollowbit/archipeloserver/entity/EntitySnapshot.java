package net.hollowbit.archipeloserver.entity;

import java.util.HashMap;

public class EntitySnapshot {
	
	public String name;
	public String type;
	public String anim;
	public float animTime;
	public String animMeta = "";
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
	
	public void clear () {
		properties.clear();
	}
	
	public boolean isEmpty () {
		return properties.isEmpty();
	}
	
}
