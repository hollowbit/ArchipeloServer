package net.hollowbit.archipeloserver.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.lifeless.*;
import net.hollowbit.archipeloserver.entity.living.*;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.EntityTypeData;

@SuppressWarnings("rawtypes")
public enum EntityType {
	
	PLAYER ("player", Player.class),
	TELEPORTER ("teleporter", Teleporter.class),
	DOOR ("door", Door.class),
	DOOR_LOCKED ("door-locked", DoorLocked.class),
	SIGN ("sign", Sign.class);
	
	private String id;
	private Class entityClass;
	private int numberOfStyles;
	private boolean hittable;
	private float speed;

	//Rects
	private CollisionRect viewRect;
	private CollisionRect collRects[];
	
	private EntityType (String id, Class entityClass) {
		this.id = id;
		this.entityClass = entityClass;
		
		//Load rest of data from file
		Json json = new Json();
		InputStream in = getClass().getResourceAsStream("/entities/" + id + ".json");
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
		EntityTypeData data = json.fromJson(EntityTypeData.class, fileString);
		
		this.numberOfStyles = data.numberOfStyles;
		this.hittable = data.hittable;
		this.speed = data.speed;
		
		this.viewRect = new CollisionRect(0, 0, data.viewRectOffsetX, data.viewRectOffsetY, data.viewRectWidth, data.viewRectHeight);
		
		this.collRects = new CollisionRect[data.collisionRects.length];
		for (int i = 0; i < collRects.length; i++) {
			this.collRects[i] = new CollisionRect(data.collisionRects[i]);
		}
	}

	@SuppressWarnings("unchecked")
	public Entity createEntityOfType () {
		Entity entity = null;
		try {
			entity = (Entity) ClassReflection.newInstance(entityClass);
		} catch (ReflectionException e) {
			ArchipeloServer.getServer().getLogger().error("Could not load new instance of entity type for: " + id + ".");
		}
		return entity;
	}
	
	public String getId () {
		return id;
	}
	
	public CollisionRect getViewRect (float x, float y) {
		return viewRect.move(x, y);
	}
	
	public CollisionRect[] getCollisionRects (float x, float y) {
		for (CollisionRect rect : collRects) {
			rect.move(x, y);
		}
		return collRects;
	}
	
	public int getNumberOfStyles () {
		return numberOfStyles;
	}
	
	public boolean isHittable () {
		return hittable;
	}
	
	public float getSpeed () {
		return speed;
	}
	
	public float getViewWidth () {
		return viewRect.width;
	}
	
	public float getViewHeight () {
		return viewRect.height;
	}
	
	//Static
	private static HashMap<String, EntityType> entityTypeMap;
	
	static {
		entityTypeMap = new HashMap<String, EntityType>();
		for (EntityType entityType : EntityType.values()) {
			entityTypeMap.put(entityType.getId(), entityType);
		}
	}
	
	public static Entity createEntityBySnapshot (EntitySnapshot fullSnapshot, Map map) {
		EntityType entityType = entityTypeMap.get(fullSnapshot.entityType);
		Entity entity = entityType.createEntityOfType();
		entity.create(fullSnapshot, map, entityType);
		return entity;
	}
	
	public static EntityType getEntityTypeById (String id) {
		return entityTypeMap.get(id);
	}
	
}
