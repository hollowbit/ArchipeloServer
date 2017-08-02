package net.hollowbit.archipeloserver.world;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonWriter;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityManager;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.form.FormManager;
import net.hollowbit.archipeloserver.network.packets.EntityAddPacket;
import net.hollowbit.archipeloserver.network.packets.EntityRemovePacket;
import net.hollowbit.archipeloserver.particles.Particles;
import net.hollowbit.archipeloserver.tools.npcdialogs.NpcDialogManager;
import net.hollowbit.archipeloserver.world.map.Chunk;
import net.hollowbit.archipeloserver.world.map.ChunkRow;
import net.hollowbit.archipeloshared.ChunkData;
import net.hollowbit.archipeloshared.ChunkLocation;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.InvalidMapFolderException;
import net.hollowbit.archipeloshared.MapData;
import net.hollowbit.archipeloshared.MapSnapshot;
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
	private TreeMap<Integer, ChunkRow> chunkRows;
	private NpcDialogManager npcDialogManager;
	private FormManager formManager;
	private EntityManager entityManager;
	private World world;
	private boolean canSave;
	private String displayName;
	private int climat;
	private int type;
	private boolean naturalLighting;
	private String music;
	private boolean loaded = false;
	Json json = new Json();
	
	private int width, height;
	private int minTileX, maxTileX, minTileY, maxTileY;
	private ArrayList<ChunkLocation> chunkLocations;
	
	public Map (String name, World world) {
		this.name = name;
		this.world = world;
		chunkRows = new TreeMap<Integer, ChunkRow>();
		npcDialogManager = new NpcDialogManager(this);
		formManager = new FormManager(this);
		changes = new MapSnapshot(name, displayName);
		entityManager = new EntityManager();
	}
	
	public void tick20 (float deltaTime) {
		for (Entity entity : entityManager.duplicateEntityList()) {
			entity.tick20(deltaTime);
		}
	}
	
	public void tick60 (float deltaTime) {
		for (Entity entity : entityManager.duplicateEntityList()) {
			entity.tick60(deltaTime);
		}
	}

	//If you wish to add this map, load it from its island, not here.
	public boolean load () {
		ArchipeloServer.getServer().getLogger().info("Loading map: " + getName() + ".");
		displayName = makeDisplayName();
		
		try {
			this.loadFromFile();
		} catch (InvalidMapFolderException e) {
			ArchipeloServer.getServer().getLogger().error("Could not load map " + this.name + ". Reason: " + e.getMessage());
		}
		loaded = true;
		return true;
	}
	
	//If you wish to remove this map, unload it from its island, not here.
	public void unload () {
		ArchipeloServer.getServer().getLogger().info("Unloading map: " + getName() + ".");
		try {
			if (canSave)
				this.saveToFile();
		} catch (IOException e) {
			ArchipeloServer.getServer().getLogger().error("Could not save map " + this.name + ". Reason: " + e.getMessage());
		}
		formManager.dispose();
		loaded = false;
	}
	
	private boolean getTileCollisionAtPos(int x, int y) {
		int chunkX = (int) Math.floor((float) x / (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE));
		int chunkY = (int) Math.floor((float) y / (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE));
		
		int xWithinChunk = Math.abs(x) % (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE);
		if (x < 0)
			xWithinChunk = (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE) - xWithinChunk;
		int yWithinChunk = Math.abs(y) % (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE);
		if (y < 0)
			yWithinChunk = (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE) - yWithinChunk;
		
		if (xWithinChunk == (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE) || yWithinChunk == (ChunkData.SIZE * TileData.COLLISION_MAP_SCALE))
			return false;
		
		Chunk chunk = getChunk(chunkX, chunkY);
		if (chunk != null)
			return chunk.getCollisionMap()[yWithinChunk][xWithinChunk];
		return true;
	}
	
	public boolean collidesWithMap (CollisionRect[] rects, Entity testEntity) {
		for (CollisionRect rect : rects) {
			if (collidesWithMap(rect, testEntity))
				return true;
		}
		return false;
	}
	
	public boolean collidesWithMap (CollisionRect rect, Entity testEntity) {
		int collisionBoxSize = (int) ArchipeloServer.TILE_SIZE / TileData.COLLISION_MAP_SCALE;
		
		//See if rect collides with map
		if (rect.xWithOffset() < 0 || rect.yWithOffset() < 0 || rect.xWithOffset() + rect.width > getPixelWidth() || rect.yWithOffset() + rect.height > getPixelHeight())
			return true;
		
		//See if it collides with tiles and elements
		for (int row = (int) (rect.yWithOffset() / collisionBoxSize); row < Math.ceil((rect.height + rect.yWithOffset()) / collisionBoxSize); row++) {
			for (int col = (int) (rect.xWithOffset() / collisionBoxSize); col < Math.ceil((rect.width + rect.xWithOffset()) / collisionBoxSize); col++) {
				if (getTileCollisionAtPos(col, row))
						return true;
			}
		}

		boolean isPlayer = testEntity instanceof Player;
		
		//Check for collisions with entities
		for (Entity entity : getEntities()) {
			if (entity == testEntity)
				continue;
			
			for (CollisionRect entityRect : entity.getCollisionRects()) {
				//Handles the case where this entity runs into a player that is cannot collide with
				if (entity.isPlayer()) {
					if (rect.hard && !testEntity.ignoreHardnessOfCollisionRects((Player) entity, rect.name) && entityRect.collidesWith(rect))
						return true;
				}
				
				if (!entityRect.hard)
					continue;
				
				if (isPlayer && entity.ignoreHardnessOfCollisionRects((Player) testEntity, entityRect.name))
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
	
	public World getWorld () {
		return world;
	}
	
	public MapSnapshot getChangesSnapshot () {
		return changes;
	}
	
	public MapSnapshot getFullSnapshot () {
		MapSnapshot snapshot = new MapSnapshot(name, displayName);
		snapshot.putString("display-name", displayName);
		snapshot.putInt("fade-color", naturalLighting ? FADE_COLOR_WHITE : FADE_COLOR_BLACK);
		snapshot.putString("music", music);
		return snapshot;
	}
	
	/**
	 * Safe way to loop through entities. Prevents ConcurrentModification Exceptions.
	 * Exceptions
	 * @return
	 */
	public Collection<Entity> getEntities () {
		return entityManager.duplicateEntityList();
	}
	
	public Collection<Player> getPlayers () {
		return entityManager.getPlayers();
	}
	
	public EntityManager getEntityManager() {
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
	
	/**
	 * Please call Entity.remove() instead. Can cause bugs otherwise.
	 * @param entity
	 */
	public void removeEntityUnsafe (Entity entity) {
		entityManager.removeEntity(entity);
		
		EntityRemovePacket removePacket = new EntityRemovePacket(entity);
		for (Player player : getPlayers()) {
			player.sendPacket(removePacket);
		}
		
		//Check if there are any players left. If not, unload the map.
		if (entity.isPlayer()) {
			if (entityManager.noPlayersInList()) {
				world.unloadMap(this);
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
	
	public NpcDialogManager getNpcDialogManager () {
		return npcDialogManager;
	}
	
	/**
	 * Play a sound at a position (pixel coordinate).
	 * Does not play if sound path is incorrect.
	 * @param path
	 * @param tileX
	 * @param tileY
	 */
	public void playSound (String path, float x, float y) {
		this.playSound(path, (int) (x / ArchipeloServer.TILE_SIZE), (int) (y / ArchipeloServer.TILE_SIZE));
	}
	
	/**
	 * Play a sound at a tile coordinate.
	 * Does not play if sound path is incorrect.
	 * @param path
	 * @param tileX
	 * @param tileY
	 */
	private void playSound (String path, int tileX, int tileY) {
		if (ArchipeloServer.getServer().getSoundManager().doesSoundExist(path))
			changes.addSound(path, tileX, tileY);
	}
	
	public Collection<Player> duplicatePlayerList () {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(getPlayers());
		return players;
	}
	
	/**
	 * Spawn particles on the map.
	 * @param particles
	 */
	public void spawnParticles(Particles particles) {
		changes.particles.add(particles.getData());
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
		return width;
	}
	
	public int getHeight () {
		return height;
	}
	
	public int getPixelWidth () {
		return getWidth() * ArchipeloServer.TILE_SIZE;
	}
	
	public int getPixelHeight () {
		return getHeight() * ArchipeloServer.TILE_SIZE;
	}
	
	public ArrayList<ChunkLocation> getChunkLocations() {
		return chunkLocations;
	}
	
	/**
	 * Will check if a chunk is loaded, if not it will load it, if possible.
	 * Returns null if the chunk could not be loaded.
	 * @param x
	 * @param y
	 * @return
	 */
	public Chunk loadChunk(int x, int y) {
		ChunkRow row = chunkRows.get(y);
		if (row == null) {
			row = new ChunkRow(y);
			chunkRows.put(y, row);
		}
		
		Chunk chunk = row.getChunks().get(x);
		if (chunk != null) //Loaded so just return it
			return chunk;	
		else {//Not loaded so load it
			File chunkFile = new File("maps/" + this.name + "/chunks/" + y + "/" + x + ".json");
			if (!chunkFile.exists()) {
				return null;
			} else {
				FileReader reader = null;
				try {
					reader = new FileReader(chunkFile);
					ChunkData data = json.fromJson(ChunkData.class, reader);
					
					chunk = new Chunk(data, this);
					row.getChunks().put(x, chunk);
					
					//Load entities
					for (EntitySnapshot snapshot : data.entities.values())
						entityManager.addEntity(EntityType.createEntityBySnapshot(snapshot, this));
					
					return chunk;
				} catch (Exception e) {
					return null;
				} finally {
					try {
						reader.close();
					} catch (Exception e) {}
				}
				
			}
		}
	}
	
	/**
	 * Used to unload chunks that aren't loaded by players
	 * @param validChunks
	 */
	public void unloadChunksNotInSet(HashSet<Chunk> validChunks) {
		Iterator<ChunkRow> i = chunkRows.values().iterator();
		while (i.hasNext()) {
			ChunkRow row = i.next();
			Iterator<Chunk> i2 = row.getChunks().values().iterator();
			while (i2.hasNext()) {
				Chunk chunk = i2.next();
				if (!validChunks.contains(chunk)) {
					i2.remove();
					unloadChunk(chunk);
				}
			}
			
			if (row.getChunks().isEmpty())
				i.remove();
		}
	}
	
	/**
	 * Call this before removing the chunk from its row.
	 */
	protected void unloadChunk(Chunk chunk) {
		ChunkData data = chunk.getSaveData();
		for (Entity entity : getEntitiesInChunk(chunk)) {
			if (canSave)
				data.entities.put(entity.getName(), entity.getSaveSnapshot());
			entity.remove();
		}
		
		if (canSave) {
			File chunkFile = new File("maps/" + name + "/" + chunk.getY() + "/" + chunk.getX() + ".json");
			FileWriter writer = null;
			try {
				writer = new FileWriter(chunkFile);
				json.toJson(data, writer);
			} catch (IOException e) {
				ArchipeloServer.getServer().getLogger().caution("Could not save map chunk of: " + name + ":" + chunk.getX() + ":" + chunk.getY());
			} finally {
				try {
					writer.close();
				} catch (Exception e) {}
			}
		}
	}
	
	protected ArrayList<Entity> getEntitiesInChunk(Chunk chunk) {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		for (Entity entity : entityManager.duplicateEntityList()) {
			if (entity.getLocation().getChunkX() == chunk.getX() && entity.getLocation().getChunkY() == chunk.getY())
				entities.add(entity);
		}
		return entities;
	}
	
	protected void loadFromFile() throws InvalidMapFolderException {
		File folder = new File("maps/" + this.name + "/");
		if (!folder.exists())
			throw new InvalidMapFolderException("No folder selected");
		
		File settingsFile = new File(folder, "settings.json");
		if (!settingsFile.exists())
			throw new InvalidMapFolderException("Settings file not found. There must be a settings.json file in the map's root directory.");
			
		MapData mapData;
		FileReader reader = null;
		try {
			 reader = new FileReader(settingsFile);
			mapData = (MapData) json.fromJson(MapData.class, reader);
		} catch (Exception e) {
			throw new InvalidMapFolderException("Settings file is invalid.");
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.name = mapData.name;
		this.displayName = mapData.displayName;
		this.naturalLighting = mapData.naturalLighting;
		this.music = mapData.music;
		this.canSave = mapData.canSave;
		this.width = mapData.width;
		this.height = mapData.height;
		this.minTileX = mapData.minTileX;
		this.minTileY = mapData.minTileY;
		this.maxTileX = mapData.maxTileX;
		this.maxTileY = mapData.maxTileY;
		this.chunkLocations = mapData.chunks;//Not necessary, chunks are obtained on the fly and not all at once
	}
	
	public void saveToFile() throws IOException {
		if (!canSave)
			return;
		
		File parentFolder = new File("maps/");
		parentFolder.mkdirs();
		File folder = new File(parentFolder, name + "/");
		folder.mkdirs();
		
		File settingsFile = new File(folder, "settings.json");
		settingsFile.createNewFile();

		Json json = new Json();
		PrettyPrintSettings settings = new PrettyPrintSettings();
		settings.singleLineColumns = 30;
		settings.wrapNumericArrays = false;
		settings.outputType = JsonWriter.OutputType.javascript;
		
		FileWriter settingsWriter = new FileWriter(settingsFile);
		MapData data = new MapData();
		data.name = this.name;
		data.displayName = this.displayName;
		data.naturalLighting = this.naturalLighting;
		data.music = this.music;
		
		settingsWriter.write(json.prettyPrint(data, settings));
		settingsWriter.close();
		
		File chunkFolder = new File(folder, "chunks/");
		chunkFolder.mkdirs();
		deleteFolderContents(chunkFolder);//Make sure chunk folder is empty before putting things in it
		
		for (ChunkRow row : chunkRows.values()) {
			File rowFolder = new File(chunkFolder, row.getY() + "/");
			rowFolder.mkdirs();
			deleteFolderContents(rowFolder);//Make sure row folder is empty
			
			for (Chunk chunk : row.getChunks().values()) {
				FileWriter chunkFileWriter = new FileWriter(new File(rowFolder, chunk.getX() + ".json"));
				
				ChunkData chunkData = chunk.getSaveData();
				//Get entity save data
				for (Entity entity : getEntitiesInChunk(chunk))
					chunkData.entities.put(entity.getName(), entity.getSaveSnapshot());
				
				chunkFileWriter.write(json.prettyPrint(chunkData, settings));
				chunkFileWriter.close();
			}
		}
	}
	
	private void deleteFolderContents(File folder) {
		for (File child : folder.listFiles()) {
			if (child.isDirectory())
				deleteFolderContents(child);
			child.delete();
		}
	}
	
	public boolean doesChunkExist(int x, int y) {
		return this.getChunk(x, y) != null;
	}
	
	public Chunk getChunk(int x, int y) {
		ChunkRow row = chunkRows.get(y);
		if (row == null)
			return null;
		
		return row.getChunks().get(x);
	}

	public int getMinTileX() {
		return minTileX;
	}

	public int getMaxTileX() {
		return maxTileX;
	}

	public int getMinTileY() {
		return minTileY;
	}

	public int getMaxTileY() {
		return maxTileY;
	}
	
	public String getTile(int chunkX, int chunkY, int xWithinChunk, int yWithinChunk) {
		Chunk chunk = getChunk(chunkX, chunkY);
		if (chunk == null)
			return null;
		
		return chunk.getTiles()[yWithinChunk][xWithinChunk];
	}
	
	public String getTile(int tileX, int tileY) {
		int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
		int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
		
		int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
		if (tileX < 0)
			xWithinChunk = ChunkData.SIZE - xWithinChunk;
		int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
		if (tileY < 0)
			yWithinChunk = ChunkData.SIZE - yWithinChunk;
		
		return getTile(chunkX, chunkY, xWithinChunk, yWithinChunk);
	}
	
	public String getElement(int chunkX, int chunkY, int xWithinChunk, int yWithinChunk) {
		Chunk chunk = getChunk(chunkX, chunkY);
		if (chunk == null)
			return null;
		
		return chunk.getElements()[yWithinChunk][xWithinChunk];
	}
	
	public String getElement(int tileX, int tileY) {
		int chunkX = (int) Math.floor((float) tileX / ChunkData.SIZE);
		int chunkY = (int) Math.floor((float) tileY / ChunkData.SIZE);
		
		int xWithinChunk = Math.abs(tileX) % ChunkData.SIZE;
		if (tileX < 0)
			xWithinChunk = ChunkData.SIZE - xWithinChunk;
		int yWithinChunk = Math.abs(tileY) % ChunkData.SIZE;
		if (tileY < 0)
			yWithinChunk = ChunkData.SIZE - yWithinChunk;
		
		return getElement(chunkX, chunkY, xWithinChunk, yWithinChunk);
	}
	
}
