package net.hollowbit.archipeloserver.world;

import java.util.ArrayList;
import java.util.Collection;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityManager;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.EntityAddPacket;
import net.hollowbit.archipeloserver.network.packets.EntityRemovePacket;
import net.hollowbit.archipeloserver.tools.NpcDialogManager;
import net.hollowbit.archipeloserver.world.map.MapData;
import net.hollowbit.archipeloserver.world.map.MapLoader;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.TileData;

public class Map {
	
	private static final int FADE_COLOR_WHITE = 0;
	private static final int FADE_COLOR_BLACK = 1;
	
	public static final int CLIMAT_GRASSY = 0;
	public static final int CLIMAT_SANDY = 1;
	public static final int CLIMAT_SNOWY = 2;
	
	public static final int TYPE_ISLAND = 0;
	public static final int TYPE_DUNGEON = 1;
	public static final int TYPE_HOUSE = 2;
	public static final int TYPE_SHOP = 3;
	public static final int TYPE_CAVE = 4;
	
	private String name;
	private MapSnapshot changes;
	private String[][] tileData;
	private String[][] elementData;
	private boolean[][] collisionMap;
	private EntityManager entityManager;
	private NpcDialogManager npcDialogManager;
	private Island island;
	private String displayName;
	private int climat;
	private int type;
	private boolean naturalLighting;
	private String music;
	private boolean loaded = false;
	
	public Map (String name, Island island) {
		this.name = name;
		this.island = island;
		entityManager = new EntityManager();
		changes = new MapSnapshot(name, displayName);
	}
	
	public void tick20 () {
		entityManager.tick20();
	}
	
	public void tick60 () {
		entityManager.tick60();
	}

	//If you wish to add this map, load it from its island, not here.
	public boolean load () {
		npcDialogManager = new NpcDialogManager(this);
		ArchipeloServer.getServer().getLogger().info("Loading map " + getIsland().getName() + ":" + getName() + ".");
		displayName = makeDisplayName();
		MapData mapData = MapLoader.loadMap(this);
		if (mapData == null) {
			loaded = false;
			return false;
		}
		applyMapData(mapData);
		generateCollisionMap();
		loaded = true;
		return true;
	}
	
	//If you wish to remove this map, unload it from its island, not here.
	public void unload () {
		ArchipeloServer.getServer().getLogger().info("Unloading map " + getIsland().getName() + ":" + getName() + ".");
		MapLoader.saveMap(this);
		loaded = false;
	}
	
	private void generateCollisionMap () {
		collisionMap = new boolean[tileData.length * TileData.COLLISION_MAP_SCALE][tileData[0].length * TileData.COLLISION_MAP_SCALE];
		for (int row = 0; row < tileData.length; row++) {
			for (int col = 0; col < tileData[0].length; col++) {
				//Apply tile collision map
				Tile tile = ArchipeloServer.getServer().getMapElementManager().getTile(tileData[row][col]);
				for (int tileRow = 0; tileRow < tile.getCollisionTable().length; tileRow++) {
					for (int tileCol = 0; tileCol < tile.getCollisionTable()[0].length; tileCol++) {
						int x = col * TileData.COLLISION_MAP_SCALE + tileCol;
						int y = collisionMap.length - row * TileData.COLLISION_MAP_SCALE + tileRow;
						
						//if it is out of bounds, don't apply it.
						if (y < 0 || y >= collisionMap.length || x < 0 || x >= collisionMap[0].length)
							continue;
						
						collisionMap[y][x] = (tile.getCollisionTable()[tile.getCollisionTable().length - tileRow - 1][tileCol] ? true: collisionMap[y][x]);
					}
				}
				
				MapElement element = ArchipeloServer.getServer().getMapElementManager().getElement(elementData[row][col]);
				
				if (element != null) {
					for (int elementRow = 0; elementRow < element.getCollisionTable().length; elementRow++) {
						for (int elementCol = 0; elementCol < element.getCollisionTable()[0].length; elementCol++) {
							int x = col * TileData.COLLISION_MAP_SCALE + elementCol + element.offsetX;
							int y = collisionMap.length - row * TileData.COLLISION_MAP_SCALE + elementRow + element.offsetY;
							
							//If it is out of bounds, don't apply it.
							if (y < 0 || y >= collisionMap.length || x < 0 || x >= collisionMap[0].length)
								continue;
							
							collisionMap[y][x] = (element.getCollisionTable()[element.getCollisionTable().length - elementRow - 1][elementCol] ? true: collisionMap[y][x]);
						}
					}
				}
			}
		}
	}
	
	public boolean collidesWithMap (CollisionRect rect, Entity testEntity) {
		//See if rect collides with map
		if (rect.x < - ArchipeloServer.TILE_SIZE || rect.y < + ArchipeloServer.TILE_SIZE || rect.x + rect.width > getPixelWidth() - ArchipeloServer.TILE_SIZE || rect.y + rect.height > getPixelHeight() + ArchipeloServer.TILE_SIZE - rect.height)
			return true;
		
		//See if it collides with tiles and elements
		int collisionBoxSize = (int) ArchipeloServer.TILE_SIZE / TileData.COLLISION_MAP_SCALE;
		CollisionRect tileRect = new CollisionRect(0, 0, 0, 0, collisionBoxSize, collisionBoxSize);
		for (int row = (int) (rect.y / collisionBoxSize) - 1; row < (int) (rect.height / collisionBoxSize) + (rect.y / collisionBoxSize) + 2; row++) {
			for (int col = (int) (rect.x / collisionBoxSize) - 1; col < (int) (rect.width / collisionBoxSize) + (rect.x / collisionBoxSize) + 2; col++) {
				if (row < 0 || row >= collisionMap.length || col < 0 || col >= collisionMap[0].length)//If out of bounds, continue to next
					continue;
				
				if (collisionMap[row][col]) {
					tileRect.move(col * tileRect.width, row * tileRect.height);
					if (tileRect.collidesWith(rect))
						return true;
				}
			}
		}
		
		//Check for collisions with entities
		for (Entity entity : getEntities()) {
			if (entity == testEntity)
				continue;
			
			for (CollisionRect entityRect : entity.getCollisionRects()) {
				if (!entityRect.hard)
					continue;
				
				if (entityRect.collidesWith(rect))
					return true;
			}
		}
		return false;
	}
	
	public String getName () {
		return name;
	}
	
	public String makeDisplayName () {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.setCharAt(0, (builder.charAt(0) + "").toUpperCase().charAt(0));
		
		String displayName = builder.toString();
		return displayName;
	}
	
	public void setDisplayName (String displayName) {
		this.displayName = displayName;
		changes.putString("display-name", displayName);
	}
	
	public String getDisplayName () {
		return displayName;
	}
	
	public Island getIsland () {
		return island;
	}
	
	public World getWorld () {
		return island.getWorld();
	}
	
	public MapSnapshot getChangesSnapshot () {
		return changes;
	}
	
	public MapSnapshot getFullSnapshot () {
		MapSnapshot snapshot = new MapSnapshot(name, displayName);
		snapshot.setTileData(tileData.clone());
		snapshot.setElementData(elementData.clone());
		snapshot.putString("display-name", displayName);
		snapshot.putInt("fade-color", naturalLighting ? FADE_COLOR_WHITE : FADE_COLOR_BLACK);
		snapshot.putString("music", music);
		return snapshot;
	}
	
	public Collection<Entity> getEntities () {
		return entityManager.getEntities();
	}
	
	public Collection<Player> getPlayers () {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Entity entity : getEntities()) {
			if (entity.isPlayer()) {
				players.add((Player) entity);
			}
		}
		return players;
	}
	
	public EntityManager getEntityManager () {
		return entityManager;
	}
	
	public void addEntity (Entity entity) {
		EntityAddPacket addPacket = new EntityAddPacket(entity);
		for (Player player : getPlayers()) {
			player.sendPacket(addPacket);
		}
		if(entity.isPlayer()) {
			Player player = (Player) entity;
			player.setNewOnMap(true);
		}
		entityManager.addEntity(entity);
	}
	
	public void removeEntity (Entity entity) {
		entityManager.removeEntity(entity);
		
		EntityRemovePacket removePacket = new EntityRemovePacket(entity);
		for (Player player : getPlayers()) {
			player.sendPacket(removePacket);
		}
		
		//Check if there are any players left. If not, unload the map.
		if (entity.isPlayer()) {
			if (entityManager.getPlayers().isEmpty()) {
				island.unloadMap(this);
			}
		}
	}
	
	public boolean isLoaded () {
		return loaded;
	}
	
	public boolean isThereNewPlayerOnMap () {
		for (Player player : getPlayers()) {
			if (player.isNewOnMap())
				return true;
		}
		return false;
	}
	
	public String[][] getTileData() {
		return tileData;
	}
	
	public String[][] getElementData() {
		return elementData;
	}
	
	public NpcDialogManager getNpcDialogManager () {
		return npcDialogManager;
	}
	
	public void applyMapData (MapData mapData) {
		displayName = mapData.displayName;
		tileData = mapData.tileData;
		elementData = mapData.elementData;
		climat = mapData.climat;
		type = mapData.type;
		naturalLighting = mapData.naturalLighting;
		music = mapData.music;
		for (EntitySnapshot snapshot : mapData.entitySnapshots) {
			entityManager.addEntity(EntityType.createEntityBySnapshot(snapshot, this));
		}
	}
	
	public Collection<Player> duplicatePlayerList () {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(getPlayers());
		return players;
	}

	public int getClimat() {
		return climat;
	}

	public void setClimat(int climat) {
		this.climat = climat;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean hasNaturalLighting() {
		return naturalLighting;
	}

	public void setNaturalLighting(boolean naturalLighting) {
		this.naturalLighting = naturalLighting;
	}

	public String getMusic() {
		return music;
	}

	public void setMusic(String music) {
		this.music = music;
	}
	
	public int getWidth () {
		return tileData[0].length;
	}
	
	public int getHeight () {
		return tileData.length;
	}
	
	public int getPixelWidth () {
		return getWidth() * ArchipeloServer.TILE_SIZE;
	}
	
	public int getPixelHeight () {
		return getHeight() * ArchipeloServer.TILE_SIZE;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Map))
			return false;
		
		Map map = (Map) obj;
		return island == map.getIsland() && map.getName().equals(name);
	}
	
}
