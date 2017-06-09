package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.hollowbit.archipeloserver.entity.living.Player;

public class EntityManager {
	
	private HashMap<String, Entity> entities;
	private ArrayList<Entity> entitiesList;
	
	public EntityManager () {
		entities = new HashMap<String, Entity>();
		entitiesList = new ArrayList<Entity>();
	}
	
	public synchronized void addEntity (Entity entity) {
		entities.put(entity.getName(), entity);
		entitiesList.add(entity);
	}
	
	public synchronized void removeEntity (Entity entity) {
		entities.remove(entity);
		entitiesList.remove(entity);
	}
	
	public synchronized ArrayList<Entity> duplicateEntityList () {
		ArrayList<Entity> entityList = new ArrayList<Entity>();
		entityList.addAll(entitiesList);
		return entityList;
	}
	
	public void tick20 (float deltaTime) {
		for (Entity entity : duplicateEntityList()) {
			entity.tick20(deltaTime);
		}
	}
	
	public void tick60 (float deltaTime) {
		for (Entity entity : duplicateEntityList()) {
			entity.tick60(deltaTime);
		}
	}
	
	public synchronized Entity getEntity (String name) {
		return entities.get(name);
	}
	
	public boolean exists (String name) {
		if (!entities.containsKey(name))
			return false;
		else {
			return true;
		}
	}
	
	public Player getPlayer (String name) {
		Entity entity = entities.get(name);
		if (entity == null)
			return null;
		
		if (entity.isPlayer()) {
			return (Player) entity;
		} else {
			return null;
		}
	}
	
	public Collection<Entity> getEntities () {
		return entitiesList;
	}
	
	public Collection<Player> getPlayers () {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Entity entity : entitiesList) {
			if(entity.isPlayer())
				players.add((Player) entity);
		}
		return players;
	}
	
}
