package net.hollowbit.archipeloserver.entity;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.PopupTextPacket;
import net.hollowbit.archipeloserver.network.packets.TeleportPacket;
import net.hollowbit.archipeloserver.tools.entity.Location;
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
	
	public Entity () {}
	
	public void create (String name, int style, Location location, EntityType entityType) {
		this.name = name;
		this.style = style;
		this.entityType = entityType;
		this.location = location;
		changes = new EntitySnapshot(this);
		log = new EntityLog();
	}
	
	public void create (EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		this.name = fullSnapshot.name;
		this.style = fullSnapshot.style;
		this.entityType = entityType;
		this.location =  new Location(map, new Vector2(fullSnapshot.getFloat("x", 0), fullSnapshot.getFloat("y", 0)), fullSnapshot.getInt("direction", 0));
		changes = new EntitySnapshot(this);
		log = new EntityLog();
	}
	
	public void tick20 () {
		log.removeOldEntitySnapshotsFromLog();
	}
	
	public void tick60 () {
		log.addEntry(new EntityLogEntry(location.getX(), location.getY(), getSpeed()));
	}
	
	public void interactWith (Entity entity, String collisionRectName, EntityInteraction interactionType) {
		entity.interactFrom(this, collisionRectName, interactionType);
	}
	
	public void interactFrom (Entity entity, String collisionRectName, EntityInteraction interactionType) {}
	
	public String getName () {
		return name;
	}
	
	public int getStyle () {
		return style;
	}
	
	public void remove () {
		location.getMap().removeEntity(this);
	}
	
	/**
	 * This is used by certain entities which don't always want a collision rect to be hard.
	 * Ex: Like a locked door that becomes unlocked for some players.
	 * @param player
	 * @param rectName
	 * @return
	 */
	public boolean ignoreHardnessOfCollisionRects (Player player, String rectName) {
		return false;
	}
	
	//InterpSnapshots are for thing like position that can be interpolated between and can be skipped.
	public EntitySnapshot getInterpSnapshot () {
		EntitySnapshot snapshot = new EntitySnapshot(this);
		return snapshot;
	}
	
	//Changes since last tick. Unlike InterpSnapshots, these are changes that MUST be applied.
	public EntitySnapshot getChangesSnapshot () {
		return changes;
	}
	
	//Full data of an entity. This is used for EntityAddPackets.
	public EntitySnapshot getFullSnapshot () {
		EntitySnapshot snapshot = new EntitySnapshot(this);
		snapshot.putFloat("x", this.getX());
		snapshot.putFloat("y", this.getY());
		return snapshot;
	}
	
	//Get a snapshot of an entity to save them when map unloads
	public EntitySnapshot getSaveSnapshot () {
		return getFullSnapshot();
	}
	
	public Location getLocation () {
		return location;
	}
	
	public void clearAfterTeleport () {
		log.clearAll();
	}
	
	public void teleport (float x, float y, int direction) {
		teleport(x, y, direction, false);
	}
	
	private void teleport (float x, float y, int direction, boolean newMap) {
		location.set(new Vector2(x, y));
		if (direction >= 0)//Only set direction if it is positive
			location.setDirection(Direction.values()[direction]);
		moved();
		
		for (Player player : location.getMap().duplicatePlayerList()) {
			player.sendPacket(new TeleportPacket(this.name, this.location.getX(), this.location.getY(), this.location.getDirectionInt(), newMap));
		}
		clearAfterTeleport();
	}
	
	public void teleport (float x, float y, int direction, String mapName) {
		//Don't bother running teleport logic for different map if on same map
		if (location.getMap().getName().equals(mapName)) {
			teleport(x, y, direction, false);
			return;
		}
		
		Island island = location.getIsland();
		Map map = null;
		
		//Make sure map is loaded
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
		teleport(x, y, direction, true);
	}
	
	public void teleport (float x, float y, int direction, String mapName, String islandName) {
		//Don't bother running teleport logic for different island if on same island
		if (location.getIsland().getName().equals(islandName)) {
			teleport(x, y, direction, mapName);
			return;
		}
		
		World world = ArchipeloServer.getServer().getWorld();
		Island island = null;
		Map map = null;
		
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
		teleport(x, y, direction, true);
	}
	
	public void moved () {
		
	}
	
	public boolean isAlive () {
		return false;
	}
	
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
	
	public boolean equals (Entity entity) {
		return (this.name.equalsIgnoreCase(entity.getName()));
	}
	
	public CollisionRect[] getCollisionRects () {
		return entityType.getCollisionRects(location.getX(), location.getY());
	}
	
	public CollisionRect[] getCollisionRects (Vector2 potentialPosition) {
		return entityType.getCollisionRects(potentialPosition.x, potentialPosition.y);
	}
	
	public Vector2 getCenterPoint () {
		CollisionRect viewRect = entityType.getViewRect(location.getX(), location.getY());
		return new Vector2(location.getX() + viewRect.width / 2, location.getY() + viewRect.height / 2);
	}
	
	public float getSpeed () {
		return entityType.getSpeed();
	}
	
}
