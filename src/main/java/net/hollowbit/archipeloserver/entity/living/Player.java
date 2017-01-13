package net.hollowbit.archipeloserver.entity.living;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.java_websocket.WebSocket;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.Tick;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityInteraction;
import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.entity.living.player.PlayerFlagsManager;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.entity.living.player.PlayerNpcDialogManager;
import net.hollowbit.archipeloserver.entity.living.player.PlayerStatsManager;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.ControlsPacket;
import net.hollowbit.archipeloserver.network.packets.LogoutPacket;
import net.hollowbit.archipeloserver.network.packets.PopupTextPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.database.DatabaseManager;
import net.hollowbit.archipeloserver.tools.entity.HitCalculator;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.events.PlayerJoinEvent;
import net.hollowbit.archipeloserver.tools.event.events.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.Controls;
import net.hollowbit.archipeloshared.Direction;

public class Player extends LivingEntity implements PacketHandler {
	
	public static final float CORRECTION_ERROR_PERCENTAGE = 0.9f;
	public static final float ROLLING_DURATION = 0.4f;
	public static final float ROLL_DOUBLE_CLICK_DURATION = 0.3f;
	public static final float HIT_RANGE = 8;
	
	//Equipped Inventory Index
	public static final int EQUIP_SIZE = 6;
	public static final int EQUIP_INDEX_BOOTS = 0;
	public static final int EQUIP_INDEX_PANTS = 1;
	public static final int EQUIP_INDEX_SHIRT = 2;
	public static final int EQUIP_INDEX_GLOVES = 3;
	public static final int EQUIP_INDEX_SHOULDERPADS = 4;
	public static final int EQUIP_INDEX_HAT = 5;
	
	public static final int UNEDITABLE_EQUIP_SIZE = 3;
	public static final int UNEDITABLE_EQUIP_INDEX_BODY = 0;
	public static final int UNEDITABLE_EQUIP_INDEX_FACE = 1;
	public static final int UNEDITABLE_EQUIP_INDEX_HAIR = 2;
	
	public static final int WEAPON_EQUIPPED_SIZE = 1;
	public static final int CONSUMABLES_EQUIPPED_SIZE = 3;
	public static final int BUFFS_EQUIPPED_SIZE = 3;
	public static final int AMMO_EQUIPPED_SIZE = 2;
	
	public static final float PERMITTED_ERROR_MULTIPLIER = 20;
	
	String id;
	String address;
	boolean firstTimeLogin;
	boolean[] controls;
	Direction rollingDirection;
	boolean newOnMap = false;//This is to know if the player needs to be sent a new map.
	float rollTimer;
	float rollDoubleClickTimer = 0;
	boolean wasMoving;
	boolean isSprinting;
	Date lastPlayed, creationDate;
	HollowBitUser hbUser;
	PlayerNpcDialogManager npcDialogManager;
	PlayerFlagsManager flagsManager;
	PlayerInventory inventory;
	PlayerStatsManager statsManager;
	
	public Player (String name, String address, boolean firstTimeLogin) {
		this.create(name, 0, location, address, firstTimeLogin);
	}
	
	public void create (String name, int style, Location location, String address, boolean firstTimeLogin) {
		super.create(name, style, location, EntityType.PLAYER);
		this.npcDialogManager = new PlayerNpcDialogManager(this);
		this.address = address;
		this.firstTimeLogin = firstTimeLogin;
		controls = new boolean[Controls.TOTAL];
		ArchipeloServer.getServer().getNetworkManager().addPacketHandler(this);
	}
	
	public void load (Map map, PlayerData playerData, HollowBitUser hbUser) {
		this.id = playerData.id;
		this.location = new Location(map, new Vector2(playerData.x, playerData.y));
		this.inventory = new PlayerInventory(this, playerData.inventory, playerData.uneditableEquippedInventory, playerData.equippedInventory, playerData.cosmeticInventory, new ArrayList<Item>(Arrays.asList(playerData.bankInventory)), playerData.weaponInventory, playerData.consumablesInventory, playerData.buffsInventory, playerData.ammoInventory);
		this.lastPlayed = playerData.lastPlayed;
		this.creationDate = playerData.creationDate;
		this.hbUser = hbUser;
		this.flagsManager = new PlayerFlagsManager(playerData.flags, this);
		this.statsManager = new PlayerStatsManager(this);
		
		PlayerJoinEvent event = new PlayerJoinEvent(this);//Triggers player join event
		event.trigger();
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
					newPos.add((float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
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
					newPos.add((float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
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
				
				if (isMoving())  {
					newPos.add((float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
				}
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
					newPos.add((float) (Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR), (float) (-Tick.T_60 * getSpeed() / LivingEntity.DIAGONAL_FACTOR));
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
			if (location.getMap().collidesWithMap(rect, this)) {
				collidesWithMap = true;
				break;
			}
		}
		
		if (!collidesWithMap || doesCurrentPositionCollideWithMap()) {
			if (isMoving())
				move(newPos);
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
	
	public void stopMovement () {
		rollTimer = 0;
		rollDoubleClickTimer = 0;
		controls[Controls.UP] = false;
		controls[Controls.LEFT] = false;
		controls[Controls.DOWN] = false;
		controls[Controls.RIGHT] = false;
	}
	
	public boolean isMoving () {
		return controls[Controls.UP] || controls[Controls.LEFT] || controls[Controls.DOWN] || controls[Controls.RIGHT];
	}
	
	@Override
	public float getSpeed () {
		return statsManager.getSpeed(controls[Controls.ROLL], isRolling());
	}
	
	@Override
	public void interactFrom (Entity entity, String collisionRectName, EntityInteraction interactionType) {
		super.interactFrom(entity, collisionRectName, interactionType);
		
		//Handle interaction with other player
		if (entity.isPlayer()) {
			Player player = (Player) entity;
			switch (interactionType) {
			case STEP_ON:
				player.sendPacket(new PopupTextPacket("{youSteppedOn} " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("{serverTag}", "{youSteppedOn} " + this.getName(), "server"));
				break;
			case STEP_OFF:
				player.sendPacket(new PopupTextPacket("{youSteppedOff} " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("{serverTag}", "{youSteppedOff} " + this.getName(), "server"));
				break;
			case HIT:
				player.sendPacket(new PopupTextPacket("{youHit} " + this.getName(), PopupTextPacket.Type.NORMAL));
				player.sendPacket(new ChatMessagePacket("{serverTag}", "{youHit} " + this.getName(), "server"));
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
	
	/**
	 * Updates player to database to be ready to be removed
	 */
	private void unload() {
		statsManager.dispose();
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
		ArchipeloServer.getServer().getDatabaseManager().updatePlayer(this);
	}
	
	/**
	 * Proper way to simply remove a player from the game
	 */
	@Override
	public void remove () {
		remove(LogoutReason.NONE, "");
	}
	
	/**
	 * Proper way to remove a player from the game with a reason
	 * @param reason
	 * @param alt
	 */
	public void remove (LogoutReason reason, String alt) {
		super.remove();
		
		PlayerLeaveEvent event = new PlayerLeaveEvent(this, reason, alt);
		event.trigger();
		reason = event.getReason();
		alt = event.getReasonAlt();
		
		unload();
		sendPacket(new LogoutPacket(reason.reason, alt));
		
		//Build leave message
		StringBuilder leaveMessage = new StringBuilder();
		leaveMessage.append("<{leave}> " + name);
		leaveMessage.append(" " + reason.message);
		leaveMessage.append(" " + alt);
		
		ArchipeloServer.getServer().getLogger().info(leaveMessage.toString());
	}
	
	
	private void controlUp (int control) {
		switch (control) {
		case Controls.ROLL:
			changes.putBoolean("is-sprinting", false);
			break;
		}
	}
	
	private void controlDown (int control) {
		switch (control) {
		case Controls.ROLL:
			if (rollDoubleClickTimer <= 0) {
				rollDoubleClickTimer = ROLL_DOUBLE_CLICK_DURATION;
			} else {
				rollDoubleClickTimer = 0;
				if (!isRolling()) {
					rollTimer = ROLLING_DURATION;
					changes.putBoolean("is-rolling", true);
					sendPacket(new PopupTextPacket("{youJustRolled}", PopupTextPacket.Type.NORMAL));
					sendPacket(new ChatMessagePacket("{serverTag}", "{youJustRolled}", "server"));
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
	public void move (Vector2 newPos) {
		super.move(newPos);
		npcDialogManager.playerMoved();
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putString("displayInventory", inventory.getDisplayInventoryJson());
		snapshot.putFloat("speed", statsManager.getSpeed());
		return snapshot;
	}
	
	public void updateDisplayInventory () {
		changes.putString("displayInventory", inventory.getDisplayInventoryJson());
	}
	
	@Override
	public boolean handlePacket (Packet packet, String address) {
		if (this.address.equals(address)) {
			switch (packet.packetType) {
			case PacketType.CONTROLS:
				boolean[] newControls = ((ControlsPacket) packet).controls;
				
				if (newControls == null || newControls.length != Controls.TOTAL)
					return true;
				
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
				if (messagePacket.message == null || messagePacket.message.equals(""))
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
					ArchipeloServer.getServer().getLogger().broadcast("&d<" + this.getName() + ">&1 ", messagePacket.message, "@" + this.getName());//@ symbol added to differentiate from server, tells client a user sent this message
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
			ArchipeloServer.getServer().getLogger().broadcastAsServer("", this.getName() + text);
		} else if (label.equalsIgnoreCase("ping")) {
			this.sendPacket(new ChatMessagePacket("", "{pong}", "server"));
		} else if (label.equalsIgnoreCase("logoff") || label.equalsIgnoreCase("exit") || label.equalsIgnoreCase("logout")) {
			this.remove(LogoutReason.LEAVE, "");
		}
	}
	
	public void sendPacket (Packet packet) {
		ArchipeloServer.getServer().getNetworkManager().sendPacket(packet, ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address));
	}
	
	public String getId () {
		return id;
	}
	
	public PlayerInventory getInventory () {
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
	
	public PlayerNpcDialogManager getNpcDialogManager () {
		return npcDialogManager;
	}
	
	public PlayerFlagsManager getFlagsManager () {
		return flagsManager;
	}
	
	public PlayerStatsManager getStatsManager () {
		return statsManager;
	}
	
	private boolean doesCurrentPositionCollideWithMap () {
		for (CollisionRect rect : getCollisionRects(location.pos)) {//Checks to make sure no collision rect is intersecting with map
			if (location.getMap().collidesWithMap(rect, this)) {
				return true;
			}
		}
		return false;
	}
	
	public static PlayerData getNewPlayerData (String name, String hbUuid, Item hair, Item face, Item body) {
		Configuration config = ArchipeloServer.getServer().getConfig();
		PlayerData playerData = new PlayerData();
		playerData.id = UUID.randomUUID().toString();
		playerData.bhUuid = hbUuid;
		playerData.name = name;
		playerData.x = config.spawnX;
		playerData.y = config.spawnY;
		playerData.island = config.spawnIsland;
		playerData.map = config.spawnMap;
		playerData.lastPlayed = DatabaseManager.getCurrentDate();
		playerData.creationDate = DatabaseManager.getCurrentDate();
		playerData.flags = new ArrayList<String>();
		
		//Default inventory
		playerData.inventory = new Item[PlayerInventory.INVENTORY_SIZE];
		playerData.cosmeticInventory = new Item[EQUIP_SIZE];
		playerData.bankInventory = new Item[1];
		playerData.weaponInventory = new Item[WEAPON_EQUIPPED_SIZE];
		playerData.consumablesInventory = new Item[CONSUMABLES_EQUIPPED_SIZE];
		playerData.buffsInventory = new Item[BUFFS_EQUIPPED_SIZE];
		playerData.ammoInventory = new Item[AMMO_EQUIPPED_SIZE];
		
		playerData.uneditableEquippedInventory = new Item[UNEDITABLE_EQUIP_SIZE];
		playerData.uneditableEquippedInventory[UNEDITABLE_EQUIP_INDEX_BODY] = body;
		playerData.uneditableEquippedInventory[UNEDITABLE_EQUIP_INDEX_FACE] = face;
		playerData.uneditableEquippedInventory[UNEDITABLE_EQUIP_INDEX_HAIR] = hair;
		
		playerData.equippedInventory = new Item[EQUIP_SIZE];
		playerData.equippedInventory[EQUIP_INDEX_BOOTS] = new Item(ItemType.BOOTS_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_PANTS] = new Item(ItemType.PANTS_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_SHIRT] = new Item(ItemType.SHIRT_BASIC);
		playerData.equippedInventory[EQUIP_INDEX_GLOVES] = null;
		playerData.equippedInventory[EQUIP_INDEX_SHOULDERPADS] = null;
		playerData.equippedInventory[EQUIP_INDEX_HAT] = null;
		return playerData;
	}
	
}
