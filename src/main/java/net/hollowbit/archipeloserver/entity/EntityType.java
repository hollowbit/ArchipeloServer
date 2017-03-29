package net.hollowbit.archipeloserver.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.lifeless.BlobbyGrave;
import net.hollowbit.archipeloserver.entity.lifeless.Computer;
import net.hollowbit.archipeloserver.entity.lifeless.Door;
import net.hollowbit.archipeloserver.entity.lifeless.DoorLocked;
import net.hollowbit.archipeloserver.entity.lifeless.Sign;
import net.hollowbit.archipeloserver.entity.lifeless.Teleporter;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.EntityAnimationData;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.EntitySoundData;
import net.hollowbit.archipeloshared.EntityTypeData;

@SuppressWarnings("rawtypes")
public enum EntityType {
	
	PLAYER ("player", Player.class),
	TELEPORTER ("teleporter", Teleporter.class),
	DOOR ("door", Door.class),
	DOOR_LOCKED ("door-locked", DoorLocked.class),
	SIGN ("sign", Sign.class),
	BLOBBY_GRAVE ("blobby_grave", BlobbyGrave.class),
	COMPUTER ("computer", Computer.class);
	
	private String id;
	private Class entityClass;
	private HashMap<String, EntityAnimationData> animations;
	private int numberOfStyles;
	private boolean hittable;
	private float speed;
	private String defaultAnimation = "";
	
	private String footstepSound = "";
	private int footstepOffsetX;
	private int footstepOffsetY;
	
	//Rects
	private CollisionRect viewRect;
	private CollisionRect collRects[];
	
	//Sounds
	private HashSet<String> sounds;
	
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
		
		this.footstepSound = data.footstepSound;
		this.footstepOffsetX = data.footstepOffsetX;
		this.footstepOffsetY = data.footstepOffsetY;
		
		this.viewRect = new CollisionRect(0, 0, data.viewRectOffsetX, data.viewRectOffsetY, data.viewRectWidth, data.viewRectHeight);
		
		this.collRects = new CollisionRect[data.collisionRects.length];
		for (int i = 0; i < collRects.length; i++) {
			this.collRects[i] = new CollisionRect(data.collisionRects[i]);
		}
		
		animations = new HashMap<String, EntityAnimationData>();
		boolean first = true;
		for (EntityAnimationData animationData : data.animations) {
			animations.put(animationData.id, animationData);
			
			if (first) {//Set the first animation as the default
				defaultAnimation = animationData.id;
				first = false;
			}
		}
		
		sounds = new HashSet<String>();
		if (data.sounds != null) {
			for (EntitySoundData soundData : data.sounds)
				sounds.add(soundData.id);
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
	
	/**
	 * Returns the view rect for this entity at the specified location.
	 * The view rect is a rect to specify what part of the entity is visible for rendering optimizations.
	 * @param x
	 * @param y
	 * @return
	 */
	public CollisionRect getViewRect (float x, float y) {
		return viewRect.move(x, y);
	}
	
	/**
	 * Returns a list of all collision rects for this entity at the specified location.
	 * @param x
	 * @param y
	 * @return
	 */
	public CollisionRect[] getCollisionRects (float x, float y) {
		CollisionRect[] rects = new CollisionRect[collRects.length];
		for (int i = 0; i < rects.length; i++) {
			CollisionRect rect = new CollisionRect(collRects[i]);
			rect.move(x, y);
			rects[i] = rect;
		}
		return rects;
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
	
	/**
	 * Default animation to play for this entity
	 * @return
	 */
	public String getDefaultAnimationId () {
		return defaultAnimation;
	}
	
	public boolean hasAnimation (String animationId) {
		return animations.containsKey(animationId);
	}
	
	public EntityAnimationData getAnimationDataById (String animationId) {
		return animations.get(animationId);
	}
	
	public boolean hasSound (String sound) {
		return sounds.contains(sound);
	}
	
	public boolean hasFootstepSound() {
		return !footstepSound.equals("");
	}
	
	public String getFootstepSound () {
		return footstepSound;
	}
	
	public int getFootstepOffsetX() {
		return footstepOffsetX;
	}

	public int getFootstepOffsetY() {
		return footstepOffsetY;
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
		EntityType entityType = entityTypeMap.get(fullSnapshot.type);
		Entity entity = entityType.createEntityOfType();
		entity.create(fullSnapshot, map, entityType);
		return entity;
	}
	
	public static EntityType getEntityTypeById (String id) {
		return entityTypeMap.get(id);
	}
	
}
