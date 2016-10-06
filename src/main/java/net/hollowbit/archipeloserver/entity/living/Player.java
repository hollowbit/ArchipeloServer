package net.hollowbit.archipeloserver.entity.living;

import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;

import org.java_websocket.WebSocket;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.Tick;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.ControlsPacket;
import net.hollowbit.archipeloserver.network.packets.PopupTextPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.DatabaseManager;
import net.hollowbit.archipeloserver.tools.HitCalculator;
import net.hollowbit.archipeloserver.tools.Location;
import net.hollowbit.archipeloserver.tools.PlayerData;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Controls;
import net.hollowbit.archipeloshared.Direction;

public class Player extends LivingEntity implements PacketHandler {
	
	public static final float CORRECTION_ERROR_PERCENTAGE = 0.9f;
	public static final float ROLLING_SPEED_SCALE = 3.0f;//Scale of speed when rolling
	public static final float SPRINTING_SPEED_SCALE = 1.4f;//^ when sprinting
	public static final float ROLLING_DURATION = 0.4f;
	public static final float ROLL_DOUBLE_CLICK_DURATION = 0.3f;
	public static final float HIT_RANGE = 8;
	
	//Equipped Inventory Index
	public static final int EQUIP_SIZE = 10;
	public static final int EQUIP_INDEX_BODY = 0;
	public static final int EQUIP_INDEX_BOOTS = 1;
	public static final int EQUIP_INDEX_PANTS = 2;
	public static final int EQUIP_INDEX_SHIRT = 3;
	public static final int EQUIP_INDEX_GLOVES = 4;
	public static final int EQUIP_INDEX_SHOULDERPADS = 5;
	public static final int EQUIP_INDEX_FACE = 6;
	public static final int EQUIP_INDEX_HAIR = 7;
	public static final int EQUIP_INDEX_HAT = 8;
	public static final int EQUIP_INDEX_USABLE = 9;
	
	public static final int INVENTORY_SIZE = 16;
	
	public static final float PERMITTED_ERROR_MULTIPLIER = 20;
	
	String uuid;
	String address;
	boolean firstTimeLogin;
	boolean[] controls;
	Direction rollingDirection;
	boolean newOnMap = false;//This is to know if the player needs to be sent a new map.
	float rollTimer;
	float rollDoubleClickTimer = 0;
	boolean wasMoving;
	boolean isSprinting;
	Item[] equippedInventory;
	Item[] inventory;
	Date lastPlayed, creationDate;
	HollowBitUser hbUser;
	
	public Player (String name, String address, boolean firstTimeLogin) {
		create(name, 0, location, address, firstTimeLogin);
	}
	
	public void create (String name, int style, Location location, String address, boolean firstTimeLogin) {
		super.create(name, style, location, EntityType.PLAYER);
		this.address = address;
		this.firstTimeLogin = firstTimeLogin;
		controls = new boolean[Controls.TOTAL];
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}
	
	public void load (Map map, PlayerData playerData, HollowBitUser hbUser) {
		this.uuid = playerData.uuid;
		this.location = new Location(map, new Vector2(playerData.x, playerData.y));
		this.equippedInventory = playerData.equippedInventory;
		this.inventory = playerData.inventory;
		this.lastPlayed = playerData.lastPlayed;
		this.creationDate = playerData.creationDate;
		this.hbUser = hbUser;
	}
	
	@Override
	public void tick60 () {
		//Tick timer for roll double-click
		if (rollDoubleClickTimer >= 0) {
			rollDoubleClickTimer -= Tick.T_60;
			if (rollDoubleClickTimer < 0)
				rollDoubleClickTimer = 0;
		}
		
		//Check whether the player started/stopped moving
		if (isMoving() != wasMoving)
			changes.putBoolean("is-moving", isMoving());
		wasMoving = isMoving();
		
		Vector2 newPos = new Vector2(location.getX(), location.getY());
		
		//Direction
		if (controls[Controls.UP]) {
			if (controls[Controls.LEFT]) {//Up left
				if (location.direction != Direction.UP_LEFT && !controls[Controls.LOCK]) {
					location.direction = Direction.UP_LEFT;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.UP_LEFT) {
					rollingDirection = Direction.UP_LEFT;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving())
					newPos.add((float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (Tick.T_60 * getSpeed() / Math.sqrt(2)));
			} else if (controls[Controls.RIGHT]) {//Up right
				if (location.direction != Direction.UP_RIGHT && !controls[Controls.LOCK]) {
					location.direction = Direction.UP_RIGHT;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.UP_RIGHT) {
					rollingDirection = Direction.UP_RIGHT;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving()) 
					newPos.add((float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (Tick.T_60 * getSpeed() / Math.sqrt(2)));
			} else {//Up
				if (location.direction != Direction.UP && !controls[Controls.LOCK]) {
					location.direction = Direction.UP;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.UP) {
					rollingDirection = Direction.UP;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving())
					newPos.add(0, Tick.T_60 * getSpeed());
			}
		} else if (controls[Controls.DOWN]) {
			if (controls[Controls.LEFT]) {//Down left
				if (location.direction != Direction.DOWN_LEFT && !controls[Controls.LOCK]) {
					location.direction = Direction.DOWN_LEFT;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.DOWN_LEFT) {
					rollingDirection = Direction.DOWN_LEFT;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving()) 
					newPos.add((float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (-Tick.T_60 * getSpeed() / Math.sqrt(2)));
			} else if (controls[Controls.RIGHT]) {//Down right
				if (location.direction != Direction.DOWN_RIGHT && !controls[Controls.LOCK]) {
					location.direction = Direction.DOWN_RIGHT;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.DOWN_RIGHT) {
					rollingDirection = Direction.DOWN_RIGHT;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving())
					newPos.add((float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (-Tick.T_60 * getSpeed() / Math.sqrt(2)));
			} else {//Down
				if (location.direction != Direction.DOWN && !controls[Controls.LOCK]) {
					location.direction = Direction.DOWN;
					changes.putInt("direction", location.direction.ordinal());
				}
				
				if (rollingDirection != Direction.DOWN) {
					rollingDirection = Direction.DOWN;
					changes.putInt("rolling-direction", rollingDirection.ordinal());
				}
				
				if (isMoving())
					newPos.add(0, -Tick.T_60 * getSpeed());
			}
		} else if (controls[Controls.LEFT]) {//Left
			if (location.direction != Direction.LEFT && !controls[Controls.LOCK]) {
				location.direction = Direction.LEFT;
				changes.putInt("direction", location.direction.ordinal());
			}
			
			if (rollingDirection != Direction.LEFT) {
				rollingDirection = Direction.LEFT;
				changes.putInt("rolling-direction", rollingDirection.ordinal());
			}
			
			if (isMoving())
				newPos.add(-Tick.T_60 * getSpeed(), 0);
		} else if (controls[Controls.RIGHT]) {//Right
			if (location.direction != Direction.RIGHT && !controls[Controls.LOCK]) {
				location.direction = Direction.RIGHT;
				changes.putInt("direction", location.direction.ordinal());
			}
			
			if (rollingDirection != Direction.RIGHT) {
				rollingDirection = Direction.RIGHT;
				changes.putInt("rolling-direction", rollingDirection.ordinal());
			}
			
			if (isMoving())
				newPos.add(Tick.T_60 * getSpeed(), 0);
		}
		
		boolean collidesWithMap = false;
		for (CollisionRect rect : getCollisionRects(newPos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect)) {
				collidesWithMap = true;
				break;
			}
		}
		
		if (!collidesWithMap || doesCurrentPositionCollideWithMap()) {
			location.set(newPos);
			moved();
		}
		
		//Rolling
		if (rollTimer > 0) {
			rollTimer -= Tick.T_60;
			if (rollTimer < 0) {
				rollTimer = 0;
			}
		}
		
		super.tick60();
	}
	
	public boolean isMoving () {
		return controls[Controls.UP] || controls[Controls.LEFT] || controls[Controls.DOWN] || controls[Controls.RIGHT];
	}
	
	@Override
	public float getSpeed () {
		return entityType.getSpeed() * (isRolling() ? ROLLING_SPEED_SCALE : (controls[Controls.ROLL] ? SPRINTING_SPEED_SCALE : 1));
	}
	
	@Override
	public void interactFrom (Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		
		//Handle interaction with other player
		if (entity.isPlayer()) {
			Player player = (Player) entity;
			switch (interactionType) {
			case STEP_ON:
				player.sendPacket(new PopupTextPacket("You stepped on " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("<Server> You have just stepped on " + this.getName(), "server"));
				break;
			case STEP_OFF:
				player.sendPacket(new PopupTextPacket("You stepped off of " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("<Server> You have just stepped off of " + this.getName(), "server"));
				break;
			case HIT:
				player.sendPacket(new PopupTextPacket("You hit " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("<Server> You hit " + this.getName(), "server"));
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public boolean isPlayer () {
		return true;
	}
	
	public String getAddress () {
		return address;
	}
	
	@Override
	public void unload() {
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
		ArchipeloServer.getServer().getDatabaseManager().updatePlayer(this);
		super.unload();
	}
	
	@Override
	public void remove () {
		remove(LogoutReason.NONE, "");
	}
	
	public void remove (LogoutReason reason, String alt) {
		unload();
		getLocation().getWorld().logoutPlayer(this, reason, alt);
	}
	
	public void controlUp (int control) {
		switch (control) {
		case Controls.ROLL:
			changes.putBoolean("is-sprinting", false);
			break;
		}
	}
	
	public void controlDown (int control) {
		switch (control) {
		case Controls.ROLL:
			if (rollDoubleClickTimer <= 0) {
				rollDoubleClickTimer = ROLL_DOUBLE_CLICK_DURATION;
			} else {
				rollDoubleClickTimer = 0;
				if (!isRolling()) {
					rollTimer = ROLLING_DURATION;
					changes.putBoolean("is-rolling", true);
					sendPacket(new PopupTextPacket("You have just rolled.", PopupTextPacket.Type.NORMAL));
					sendPacket(new ChatMessagePacket("<Server> You have just rolled.", "server"));
				}
			}
			
			changes.putBoolean("is-sprinting", true);
			break;
		case Controls.ATTACK:
			ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) location.getMap().getEntities();
			for (Entity entity : entitiesOnMap) {
				if (entity == this)
					continue;
				
				//Run hit event for every collision rect hit on entity
				for (String rectHit : HitCalculator.getCollRectsHit(this, entity, HIT_RANGE, location.getDirection())) {
					this.interactWith(entity, rectHit, EntityInteraction.HIT);
				}
			}
			break;
		}
	}
	
	public void setNewOnMap (boolean newOnMap) {
		this.newOnMap = newOnMap;
	}
	
	public boolean isNewOnMap () {
		return newOnMap;
	}
	
	public boolean isRolling () {
		return rollTimer > 0;
	}
	
	public WebSocket getConnection () {
		return ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address);
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		Json json = new Json();
		snapshot.putString("equipped-inventory", json.toJson(equippedInventory));
		return snapshot;
	}
	
	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (this.address.equals(address)) {
			switch (packet.packetType) {
			case PacketType.CONTROLS:
				boolean[] newControls = ((ControlsPacket) packet).controls;
				
				//Loops through all controls to handle them one by one.
				for (int i = 0; i < Controls.TOTAL; i++) {
					//Checks for control change and executes controlUp/Down if there is a one.
					if (controls[i]) {
						if (!newControls[i]) {
							controls[i] = false;
							controlUp(i);
						}
					} else {
						if (newControls[i]) {
							controls[i] = true;
							controlDown(i);
						}
					}
				}
				return true;
			case PacketType.CHAT_MESSAGE:
				ChatMessagePacket messagePacket = (ChatMessagePacket) packet;
				if (messagePacket.message.equals(""))
					return true;
				
				if (messagePacket.message.startsWith("/") || messagePacket.message.startsWith(".") || messagePacket.message.startsWith("!")) {//Is a command
					StringBuilder builder = new StringBuilder(messagePacket.message);
					builder.deleteCharAt(0);
					String cleanedMessage = builder.toString();
					if (!cleanedMessage.equals("")) {
						String[] messageArray = cleanedMessage.split(" ");
						if (messageArray.length == 1) {
							handleCommand(messageArray[0], new String[0]);
						} if (messageArray.length > 1) {
							String[] args = new String[messageArray.length - 1];
							for (int i = 0; i < args.length; i++) {
								args[i] = messageArray[i + 1];
							}
							handleCommand(messageArray[0], args);
						}
					}
				} else {
					ArchipeloServer.getServer().getLogger().broadcast("<" + this.getName() + "> " + messagePacket.message, this.getName());
				}
				return true;
			}
		}
		return false;
	}
	
	public void handleCommand (String label, String[] args) {
		if (label.equalsIgnoreCase("me")) {
			String text = "";
			for (String arg : args) {
				text += " " + arg;
			}
			ArchipeloServer.getServer().getLogger().broadcast(this.getName() + text, "server");
		} else if (label.equalsIgnoreCase("ping")) {
			this.sendPacket(new ChatMessagePacket("Pong!", "server"));
		} else if (label.equalsIgnoreCase("logoff") || label.equalsIgnoreCase("exit") || label.equalsIgnoreCase("logout")) {
			location.getWorld().logoutPlayer(this, LogoutReason.NONE, "");
		}
	}
	
	public void sendPacket (Packet packet) {
		ArchipeloServer.getServer().getNetworkManager().sendPacket(packet, ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
	}
	
	public String getUUID () {
		return uuid;
	}
	
	public Item[] getEquippedInventory () {
		return equippedInventory;
	}
	
	public Item[] getInventory () {
		return inventory;
	}
	
	public boolean isFirstTimeLogin () {
		return firstTimeLogin;
	}
	
	public Date getLastPlayedDate () {
		return lastPlayed;
	}
	
	public Date getCreationDate () {
		return creationDate;
	}
	
	public HollowBitUser getHollowBitUser () {
		return hbUser;
	}
	
	private boolean doesCurrentPositionCollideWithMap () {
		for (CollisionRect rect : getCollisionRects(location.pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect)) {
				return true;
			}
		}
		return false;
	}
	
	public static PlayerData getNewPlayerData (String name, String hbUuid, Item hair, Item face, Item body) {
		Configuration config = ArchipeloServer.getServer().getConfig();
		PlayerData playerData = new PlayerData();
		playerData.uuid = UUID.randomUUID().toString();
		playerData.bhUuid = hbUuid;
		playerData.name = name;
		playerData.x = config.spawnX;
		playerData.y = config.spawnY;
		playerData.island = config.spawnIsland;
		playerData.map = config.spawnMap;
		playerData.lastPlayed = DatabaseManager.getCurrentDate();
		playerData.creationDate = DatabaseManager.getCurrentDate();
		
		//Default inventory
		playerData.inventory = new Item[INVENTORY_SIZE];
		playerData.equippedInventory = new Item[EQUIP_SIZE];
		playerData.equippedInventory[EQUIP_INDEX_BODY] = body;
		playerData.equippedInventory[EQUIP_INDEX_BOOTS] = new Item(ItemType.BOOTS_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_PANTS] = new Item(ItemType.PANTS_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_SHIRT] = new Item(ItemType.SHIRT_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_GLOVES] = null;
		playerData.equippedInventory[EQUIP_INDEX_SHOULDERPADS] = null;
		playerData.equippedInventory[EQUIP_INDEX_HAIR] = hair;
		playerData.equippedInventory[EQUIP_INDEX_FACE] = face;
		playerData.equippedInventory[EQUIP_INDEX_HAT] = null;
		playerData.equippedInventory[EQUIP_INDEX_USABLE] = null;
		return playerData;
	}
	
}
