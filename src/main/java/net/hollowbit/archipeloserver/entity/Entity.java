package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.PopupTextPacket;
import net.hollowbit.archipeloserver.network.packets.TeleportPacket;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.events.EntityInteractionEvent;
import net.hollowbit.archipeloserver.tools.event.events.EntityTeleportEvent;
import net.hollowbit.archipeloserver.world.Island;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloserver.world.World;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Direction;

public abstract class Entity {
	
	protected String name;
	protected EntityType entityType;
	protected int style;
	protected Location location;
	protected EntitySnapshot changes;
	protected EntityLog log;
	protected EntityAnimationManager animationManager;
	protected ArrayList<EntityComponent> components;
	
	public Entity () {
		components = new ArrayList<EntityComponent>();
	}
	
	public void create (String name, int style, Location location, EntityType entityType) {
		this.name = name;
		this.style = style;
		this.entityType = entityType;
		this.location = location;
		changes = new EntitySnapshot(this, true);
		log = new EntityLog();
		animationManager = new EntityAnimationManager(this, entityType.getDefaultAnimationId(), "", 0);
	}
	
	public void create (EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		this.name = fullSnapshot.name;
		this.entityType = entityType;
		this.location =  new Location(map, new Vector2(fullSnapshot.getFloat("x", 0), fullSnapshot.getFloat("y", 0)), fullSnapshot.getInt("direction", 0));
		
		//Make sure style is valid
		this.style = fullSnapshot.getInt("style", 0);
		if (this.style >= entityType.getNumberOfStyles()) {
			this.style = 0;
			ArchipeloServer.getServer().getLogger().caution("Entity " + this.name + " on map " + this.getMap().getIsland().getName() + ":" + this.getMap().getName() + " has a bad style attribute.");
		}
		
		changes = new EntitySnapshot(this, true);
		log = new EntityLog();
		animationManager = new EntityAnimationManager(this, fullSnapshot.anim, fullSnapshot.animMeta, fullSnapshot.animTime);
	}
	
	/**
	 * Should only be called in the EntityAnimationManager.
	 * This is a listener to handle when the current non-looping animation has finished.
	 * Usually, this should return a looping animation just to be safe.
	 * Safe to return null in many cases.
	 */
	public abstract EntityAnimationObject animationCompleted (String animationId);
	
	public void tick20 (float deltaTime) {
		animationManager.update(deltaTime);
		log.removeOldEntitySnapshotsFromLog();
		
		for (EntityComponent component : components)
			component.tick20(deltaTime);
	}
	
	public void tick60 (float deltaTime) {
		log.addEntry(new EntityLogEntry(location.getX(), location.getY(), getSpeed()));
		
		for (EntityComponent component : components)
			component.tick60(deltaTime);
	}
	
	protected void interactWith (Entity target, String collisionRectName, EntityInteractionType interactionType) {
		EntityInteractionEvent event = new EntityInteractionEvent(this, target, collisionRectName, interactionType);
		event.trigger();
		
		if (event.wasCanceled())
			return;
		
		target.interactFrom(this, collisionRectName, interactionType);
	}
	
	protected void interactFrom (Entity entity, String collisionRectName, EntityInteractionType interactionType) {}
	
	public String getName () {
		return name;
	}
	
	/**
	 * Change the entities style on the fly. It also makes sure the style is available first.
	 * @param style
	 */
	public void setStyle (int style) {
		if (style < entityType.getNumberOfStyles()) {
			this.style = style;
			this.changes.putInt("style", style);
		}
	}
	
	public int getStyle () {
		return style;
	}
	
	public void remove () {
		location.getMap().removeEntity(this);
		
		for (EntityComponent component : components)
			component.remove();
	}
	
	/**
	 * This is used by certain entities which don't always want a collision rect to be hard.
	 * Ex: Like a locked door that becomes unlocked for some players.
	 * @param player
	 * @param rectName
	 * @return
	 */
	public boolean ignoreHardnessOfCollisionRects (Player player, String rectName) {
		boolean ignore = false;
		for (EntityComponent component : components) {
			if (component.ignoreHardnessOfCollisionRects(player, rectName))
				ignore = true;
		}
		return ignore;
	}
	
	/**
	 * InterpSnapshots are for things like position that can be interpolated between. Packet dropping should not be an issue for these data values.
	 * @return
	 */
	public EntitySnapshot getInterpSnapshot () {
		EntitySnapshot snapshot = new EntitySnapshot(this, true);
		animationManager.applyToEntitySnapshot(snapshot);
		
		for (EntityComponent component : components)
			component.editInterpSnapshot(snapshot);
		return snapshot;
	}
	
	/**
	 * Changes since last tick. Unlike InterpSnapshots, these are changes that MUST be applied.
	 * @return
	 */
	public EntitySnapshot getChangesSnapshot () {
		return changes;
	}
	
	/**
	 * Full data of an entity. This is used for EntityAddPackets.
	 * @return
	 */
	public EntitySnapshot getFullSnapshot () {
		EntitySnapshot snapshot = new EntitySnapshot(this, false);
		snapshot.putFloat("x", this.getX());
		snapshot.putFloat("y", this.getY());
		snapshot.putInt("direction", this.getLocation().getDirectionInt());
		snapshot.putInt("style", style);
		animationManager.applyToEntitySnapshot(snapshot);
		
		for (EntityComponent component : components)
			component.editFullSnapshot(snapshot);
		return snapshot;
	}
	
	/**
	 * Get a snapshot of an entity to save them when map unloads
	 * @return
	 */
	public EntitySnapshot getSaveSnapshot () {
		EntitySnapshot snapshot = new EntitySnapshot(this, false);
		snapshot.putFloat("x", this.getX());
		snapshot.putFloat("y", this.getY());
		snapshot.putInt("direction", this.getLocation().getDirectionInt());
		snapshot.putInt("style", style);
		
		for (EntityComponent component : components)
			component.editSaveSnapshot(snapshot);
		return snapshot;
	}
	
	public Location getLocation () {
		return location;
	}
	
	public void teleport (float x, float y) {
		teleport(x, y, location.getDirection());
	}
	
	public void teleport (float x, float y, Direction direction) {
		teleport(x, y, direction, location.getMap().getName());
	}
	
	public void teleport (float x, float y, Direction direction, String mapName) {
		teleport(x, y, direction, mapName, location.getIsland().getName());
	}
	
	public void teleport (Location location) {
		teleport(location.getX(), location.getY(), location.getDirection(), location.getMap().getName(), location.getIsland().getName());
	}
	
	/**
	 * Teleport this entity to another one
	 * @param entity
	 */
	public void teleportTo (Entity entity) {
		teleport(entity.getLocation());
	}
	
	/**
	 * Full teleport. Loads new islands and maps if necessary.
	 * @param x
	 * @param y
	 * @param direction
	 * @param mapName
	 * @param islandName
	 */
	public void teleport (float x, float y, Direction direction, String mapName, String islandName) {
		Vector2 newPos = new Vector2(x, y);
		
		EntityTeleportEvent event = new EntityTeleportEvent(this, newPos, location.pos, location.map, mapName, islandName, location.getDirection(), direction);
		event.trigger();
		if (event.wasCanceled())
			return;
		
		newPos.x = event.getNewPos().x;
		newPos.y = event.getNewPos().y;
		direction = event.getNewDirection();
		mapName = event.getNewMap();
		islandName = event.getNewIsland();
		
		boolean islandChanged = !location.getIsland().getName().equals(islandName);
		boolean mapChanged = islandChanged || !location.getMap().getName().equals(mapName);
		
		World world = ArchipeloServer.getServer().getWorld();
		Island island = null;
		Map map = null;
		
		if (islandChanged) {
			//Map sure island and map are loaded
			if (!world.isIslandLoaded(islandName)) {
				if (!world.loadIsland(islandName)) {
					if (isPlayer()) {
						Player p = (Player) this;
						p.sendPacket(new PopupTextPacket("Unable to teleport.", PopupTextPacket.Type.NORMAL));
						return;
					}
				}
			}
			island = world.getIsland(islandName);
		} else
			island = location.getIsland();
		
		if (mapChanged) {
			if (!island.isMapLoaded(mapName)) {
				if (!island.loadMap(mapName)) {
					if (isPlayer()) {
						Player p = (Player) this;
						p.sendPacket(new PopupTextPacket("Unable to teleport.", PopupTextPacket.Type.NORMAL));
						return;
					}
				}
			}
			map = island.getMap(mapName);
			
			//Add player to other map then remove it from current
			map.addEntity(this);
			location.getMap().removeEntity(this);
			location.setMap(map);
		} else
			map = location.getMap();
		
		location.set(newPos);
		location.setDirection(direction);
		
		for (Player player : location.getMap().duplicatePlayerList()) {
			player.sendPacket(new TeleportPacket(this.name, this.location.getX(), this.location.getY(), this.location.getDirectionInt(), mapChanged));
		}
		
		log.clearAll();
	}
	
	public boolean isAlive () {
		return false;
	}
	
	/**
	 * Returns whether this entity is a player type.
	 * @return
	 */
	public boolean isPlayer () {
		return false;
	}
	
	public EntityType getEntityType () {
		return entityType;
	}
	
	public float getX () {
		return location.getX();
	}
	
	public float getY () {
		return location.getY();
	}
	
	public Map getMap () {
		return location.getMap();
	}
	
	/**
	 * Returns array of all collisions rects for this entity
	 * @return
	 */
	public CollisionRect[] getCollisionRects () {
		return entityType.getCollisionRects(location.getX(), location.getY());
	}
	
	/**
	 * Get collision rects of this entity considering it were at a specified position.
	 * @param potentialPosition
	 * @return
	 */
	public CollisionRect[] getCollisionRects (Vector2 potentialPosition) {
		return entityType.getCollisionRects(potentialPosition.x, potentialPosition.y);
	}
	
	/**
	 * Get entity collision rects at a certain point in time. Maximum 2 seconds ago.
	 * @param time
	 * @return
	 */
	public CollisionRect[] getCollisionRects (long time) {
		Vector2 pos = log.getPositionAtTimestamp(time);
		if (pos == null)
			return getCollisionRects();
		else
			return getCollisionRects(pos);
	}
	
	/**
	 * Exact center point of the entities view rect.
	 * @return
	 */
	public Vector2 getCenterPoint () {
		CollisionRect viewRect = entityType.getViewRect(location.getX(), location.getY());
		return new Vector2(location.getX() + viewRect.width / 2, location.getY() + viewRect.height / 2);
	}
	
	public float getSpeed () {
		return entityType.getSpeed();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Entity))
			return false;
		
		Entity entity = (Entity) obj;
		return entity.name.equals(this.name);
	}
	
}
