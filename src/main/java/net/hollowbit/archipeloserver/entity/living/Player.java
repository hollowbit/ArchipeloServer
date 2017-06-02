package net.hollowbit.archipeloserver.entity.living;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import org.java_websocket.WebSocket;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityInteractionType;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.entity.components.FootstepPlayerComponent;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.entity.living.player.PlayerFlagsManager;
import net.hollowbit.archipeloserver.entity.living.player.PlayerInventory;
import net.hollowbit.archipeloserver.entity.living.player.PlayerNpcDialogManager;
import net.hollowbit.archipeloserver.entity.living.player.PlayerStatsManager;
import net.hollowbit.archipeloserver.hollowbitserver.HollowBitUser;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.items.ItemType;
import net.hollowbit.archipeloserver.items.ItemUseAnimation;
import net.hollowbit.archipeloserver.network.LogoutReason;
import net.hollowbit.archipeloserver.network.Packet;
import net.hollowbit.archipeloserver.network.PacketHandler;
import net.hollowbit.archipeloserver.network.PacketType;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;
import net.hollowbit.archipeloserver.network.packets.ControlsPacket;
import net.hollowbit.archipeloserver.network.packets.LogoutPacket;
import net.hollowbit.archipeloserver.network.packets.PlayerStatsPacket;
import net.hollowbit.archipeloserver.network.packets.PopupTextPacket;
import net.hollowbit.archipeloserver.network.packets.PositionCorrectionPacket;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.UnloadedLocation;
import net.hollowbit.archipeloserver.tools.database.DatabaseManager;
import net.hollowbit.archipeloserver.tools.entity.Location;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventHandlerPriority;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityDeathEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerJoinEvent;
import net.hollowbit.archipeloserver.tools.event.events.readonly.PlayerLeaveEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.Controls;
import net.hollowbit.archipeloshared.Direction;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.HitCalculator;
import net.hollowbit.archipeloshared.RollableEntity;
import net.hollowbit.archipeloshared.TileSoundType;
import net.hollowbit.archipeloshared.UseTypeSettings;

public class Player extends LivingEntity implements PacketHandler, RollableEntity {
	
	public static final float ROLL_DOUBLE_CLICK_DURATION = 0.3f;
	public static final float DEFAULT_HIT_RANGE = 8;
	public static final float EMPTY_HAND_USE_ANIMATION_LENTH = 0.5f;
	
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
	
	public static final int CONTROLS_UPDATE_RATE = 1000 / 30;
	public static final float CONTROLS_DELTA_TIME = 1 / 30f;
	public static final int CONTROLS_UPDATE_DELAY = 35;
	public static final int WORLD_SNAPSHOT_DELAY = 100;
	
	String id;
	String address;
	boolean firstTimeLogin;
	boolean[] controls;
	Direction rollingDirection;
	boolean newOnMap = false;//This is to know if the player needs to be sent a new map.
	float rollDoubleClickTimer = 0;
	boolean wasMoving;
	Date lastPlayed, creationDate;
	HollowBitUser hbUser;
	PlayerNpcDialogManager npcDialogManager;
	PlayerFlagsManager flagsManager;
	PlayerInventory inventory;
	PlayerStatsManager statsManager;
	LinkedList<ControlsPacket> commandsToExecute;
	Random random;
	int seed;
	UnloadedLocation respawnLocation;
	EventHandler respawner;
	boolean movementEnabled = true;
	
	Thread controlsUpdater;
	boolean running = true;
	
	public Player (String name, String address, boolean firstTimeLogin) {
		this.create(name, 0, location, address, firstTimeLogin);
		commandsToExecute = new LinkedList<ControlsPacket>();
		
		controlsUpdater = new Thread(new Runnable() {
			@Override
			public void run() {
				while (running) {
					long startTime = System.currentTimeMillis();
					LinkedList<ControlsPacket> packetsToRemove = new LinkedList<ControlsPacket>();
					for (ControlsPacket cPacket : getCommandsClone()) {
						if (cPacket.time <= System.currentTimeMillis() - CONTROLS_UPDATE_DELAY) {
							packetsToRemove.add(cPacket);
							boolean[] newControls = cPacket.parse();
							
							if (newControls == null || newControls.length != Controls.TOTAL)//Prevent invalid controls packets
								return;
							
							//duplicate controls since they will be replaced
							boolean[] oldControls = new boolean[controls.length];
							for (int i = 0; i < controls.length; i++)
								oldControls[i] = controls[i];
							applyControlExceptions(newControls);
							controls = newControls;
							
							//Loops through all controls to handle them one by one.
							for (int i = 0; i < Controls.TOTAL; i++) {
								//Checks for control change and executes controlUp/Down if there is a one.
								if (oldControls[i]) {
									if (!controls[i])
										controlUp(i);
								} else {
									if (controls[i])
										controlDown(i);
								}
							}
							
							updateControls(newControls, CONTROLS_DELTA_TIME);
							sendPacket(new PositionCorrectionPacket(location.pos.x, location.pos.y, cPacket.id));
						}
					}
					removeAllCommands(packetsToRemove);
					
					//Sleep thread to make up for update rate
					long delta = System.currentTimeMillis() - startTime;
					if (delta < CONTROLS_UPDATE_RATE) {
						try {
							Thread.sleep(CONTROLS_UPDATE_RATE - delta);
						} catch (InterruptedException e) {
							
						}
					}
				}
			}
		});
		controlsUpdater.start();
		
		final Player player = this;
		//Prevents the player from being removed when dead. Simply teleports them.
		respawner = new EventHandler() {
			@Override
			public boolean onEntityDeath(EntityDeathEvent event) {
				if (event.getEntity() == player) {
					event.cancel();
					player.movementAnimationManager.clearAll();
					event.setNewHealth(EntityType.PLAYER.getMaxHealth());
					if (event.hasKiller())
						sendPacket(new PopupTextPacket("{youWereKilledBy} " + event.getKiller().getName(), PopupTextPacket.Type.NORMAL));
					else
						sendPacket(new PopupTextPacket("{youDied}", PopupTextPacket.Type.NORMAL));
						
					player.teleport(respawnLocation.getX(), respawnLocation.getY(), Direction.DOWN, respawnLocation.getMap(), respawnLocation.getIsland());
					return true;
				}
				return false;
			}
		};
		respawner.addToEventManager();
		respawner.registerEventPriority(EventType.EntityDeath, EventHandlerPriority.HIGHEST_EDITABLE);
	}
	
	/**
	 * Cleans up controls when there are certain conditions.
	 * @param controls
	 */
	private void applyControlExceptions (boolean[] controls) {
		if (isThrusting() || !movementEnabled) {
			controls[Controls.UP] = false;
			controls[Controls.LEFT] = false;
			controls[Controls.DOWN] = false;
			controls[Controls.RIGHT] = false;
		}
	}
	
	public void create (String name, int style, Location location, String address, boolean firstTimeLogin) {
		super.create(name, style, location, EntityType.PLAYER);
		this.npcDialogManager = new PlayerNpcDialogManager(this);
		this.address = address;
		this.firstTimeLogin = firstTimeLogin;
		controls = new boolean[Controls.TOTAL];
		this.components.add(new FootstepPlayerComponent(this, true, TileSoundType.GRASS));
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
		this.health = playerData.health;
		this.seed = StaticTools.getRandom().nextInt(1000000);
		this.random = new Random(seed);
		this.respawnLocation = new UnloadedLocation(playerData.respawnX, playerData.respawnY, playerData.respawnIsland, playerData.respawnMap);
		
		//Send player stats to itself
		this.sendPacket(new PlayerStatsPacket(health));
		
		PlayerJoinEvent event = new PlayerJoinEvent(this);//Triggers player join event
		event.trigger();
	}
	
	@Override
	public void tick60 (float deltaTime) {
		//Tick timer for roll double-click
		if (rollDoubleClickTimer >= 0) {
			rollDoubleClickTimer -= deltaTime;
			if (rollDoubleClickTimer < 0)
				rollDoubleClickTimer = 0;
		}
		
		super.tick60(deltaTime);
	}
	
	public void updateControls (boolean[] controls, float deltaTime) {
		if (isMoving()) {
			Direction direction = getMovementDirection();
			this.move(direction, deltaTime, true);
			
			//Changes direction if lock is off
			if (!controls[Controls.LOCK])
				this.setDirection(direction);
		}
	}
	
	public void setMovementEnabled(boolean movementEnabled) {
		this.movementEnabled = movementEnabled;
	}
	
	public Direction getMovementDirection () {
		if (controls[Controls.UP]) {
			if (controls[Controls.LEFT])
				return Direction.UP_LEFT;
			else if (controls[Controls.RIGHT])
				return Direction.UP_RIGHT;
			else
				return Direction.UP;
		} else if (controls[Controls.DOWN]) {
			if (controls[Controls.LEFT])
				return Direction.DOWN_LEFT;
			else if (controls[Controls.RIGHT])
				return Direction.DOWN_RIGHT;
			else
				return Direction.DOWN;
		} else if (controls[Controls.LEFT])
			return Direction.LEFT;
		else if (controls[Controls.RIGHT])
			return Direction.RIGHT;
		
		return null;
	}
	
	@Override
	public boolean isMoving () {
		return (controls[Controls.UP] || controls[Controls.LEFT] || controls[Controls.DOWN] || controls[Controls.RIGHT]) && !animationManager.getAnimationId().equals("thrust");
	}
	
	public boolean isSprinting () {
		return controls[Controls.ROLL];
	}
	
	public boolean isDirectionLocked () {
		return controls[Controls.LOCK];
	}
	
	@Override
	public boolean isRolling () {
		return animationManager.getAnimationId().equals("roll");
	}
	
	/**
	 * Tells whether the player is currently in a use animation.
	 * Will return false even if in thrust animation. This is only for use and usewalk.
	 * @return
	 */
	private boolean isUsing () {
		return animationManager.getAnimationId().equals("use") || animationManager.getAnimationId().equals("usewalk");
	}
	
	/**
	 * Returns whether the player is currently using an item, whether in thrust, use or usewalk.
	 * @return
	 */
	public boolean isCurrentlyUsingAnItem () {
		return isUsing() || isThrusting();
	}
	
	/**
	 * Tells whether the player is currently in a thrust animation.
	 * @return
	 */
	public boolean isThrusting () {
		return animationManager.getAnimationId().equals("thrust");
	}
	
	@Override
	public float getSpeed () {
		return statsManager.getSpeed(isSprinting(), isRolling());
	}
	
	@Override
	public int getMaxHealth () {
		return statsManager.getMaxHealth();
	}
	
	@Override
	public void interactFrom (Entity entity, String collisionRectName, EntityInteractionType interactionType) {
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
		
		//Close controls thread
		try {
			running = false;
			controlsUpdater.join();
		} catch (InterruptedException e) {
			ArchipeloServer.getServer().getLogger().caution("Was unable to join controls thread for player: " + name);
		}
		
		//Remove other resources
		statsManager.dispose();
		respawner.removeFromEventManager();
		ArchipeloServer.getServer().getNetworkManager().removePacketHandler(this);
		ArchipeloServer.getServer().getDatabaseManager().updatePlayer(this);
		
		PlayerLeaveEvent event = new PlayerLeaveEvent(this, reason, alt);
		event.trigger();
		reason = event.getReason();
		alt = event.getReasonAlt();
		
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
		case Controls.ATTACK:
			if (isCurrentlyUsingAnItem())
				animationManager.endCurrentAnimation();
			break;
		case Controls.ROLL:
			if (!isRolling() && !isCurrentlyUsingAnItem()) {
				if (isMoving())
					animationManager.change("walk");
				else
					animationManager.change("default");
			}
			break;
		case Controls.UP:
		case Controls.LEFT:
		case Controls.DOWN:
		case Controls.RIGHT:
			if (!isRolling() && !isThrusting()) {
				if (isMoving()) {
					if (isUsing())
						animationManager.changeWithoutReset("usewalk");
					else
						animationManager.change("walk");
				} else {
					if (isUsing())
						animationManager.changeWithoutReset("use");
					else
						animationManager.change("default");
				}
			}
			break;
		}
	}
	
	private void controlDown (int control) {
		switch (control) {
		case Controls.ROLL:
			if (!isCurrentlyUsingAnItem() && isMoving()) {
				animationManager.change("sprint");
				if (rollDoubleClickTimer <= 0) {
					rollDoubleClickTimer = ROLL_DOUBLE_CLICK_DURATION;
				} else {
					rollDoubleClickTimer = 0;
					if (!isRolling()) {//Don't roll if already rolling
						animationManager.change("roll", "" + getMovementDirection().ordinal());
						sendPacket(new PopupTextPacket("{youJustRolled}", PopupTextPacket.Type.NORMAL));
						sendPacket(new ChatMessagePacket("{serverTag}", "{youJustRolled}", "server"));
					}
				}
			}
			
			break;
		case Controls.ATTACK:
			if (!isRolling() && !isCurrentlyUsingAnItem()) {
				long time = System.currentTimeMillis() - CONTROLS_UPDATE_DELAY - WORLD_SNAPSHOT_DELAY - hbUser.getPing();
				ArrayList<Entity> entitiesOnMap = (ArrayList<Entity>) location.getMap().getEntities();
				boolean useHitAnimation = true;
				
				for (Entity entity : entitiesOnMap) {
					if (entity == this)
						continue;
					
					//Run hit event for every collision rect hit on entity
					for (String rectHit : HitCalculator.getCollRectsHit(this.getCenterPoint().x, this.getCenterPoint().y, entity.getCollisionRects(time), DEFAULT_HIT_RANGE, location.getDirection())) {
						this.interactWith(entity, rectHit, EntityInteractionType.HIT);
						
						//If the entity is not hittable, don't use the animation
						if (!entity.getEntityType().isHittable())
							useHitAnimation = false;
					}
				}
				
				//Use item if no "non-hittable" entity hit
				if (useHitAnimation) {
					Item item = inventory.getWeaponInventory().getRawStorage()[0];
					
					if (item != null) {
						UseTypeSettings settings = item.useTap(this, time);
						if (settings != null)
							playUseAnimation(item, settings.animationType, item.getType().getUseAnimationByUseType(settings.animationType).usesThrust(), settings.soundType);
					} else
						playUseAnimation(null, 0, false, 0);
				}
			}
			break;
		case Controls.UP:
		case Controls.LEFT:
		case Controls.DOWN:
		case Controls.RIGHT:
			if (!isRolling()) {
				if (isUsing())
					animationManager.changeWithoutReset("usewalk");
				else
					animationManager.change("walk");
			}
			break;
		}
	}
	
	/**
	 * Play use animation for current player with the specified item
	 * @param item
	 * @param animationType
	 * @param thrust
	 * @param soundType
	 */
	private void playUseAnimation (Item item, int animationType, boolean thrust, int soundType) {
		String animationMeta = "";
		float useAnimationLength = EMPTY_HAND_USE_ANIMATION_LENTH;
		
		if (item != null) {
			//Build the animation meta data
			Color color = new Color(item.color);
			animationMeta = item.getType() + ";" + animationType + ";" + item.style + ";" + color.r + ";" + color.g + ";" + color.b + ";" + color.a;
			useAnimationLength = item.getType().getUseAnimationLength(animationType);
			
			//Play sound of the item
			audioManager.playUnsafeSound(item.getType().getSoundById(item.style, soundType));
		}
		
		//Use appropriate animations depending
		ItemUseAnimation animationInfo = null;
		if (item != null)
			animationInfo = item.getType().getUseAnimationByUseType(animationType);
		else
			animationInfo = ItemUseAnimation.DEFAULT;
		if (thrust) {
			animationManager.change("thrust", animationMeta, useAnimationLength, animationInfo.doesStick(), animationInfo.canEndEarly());
		} else {
			if (isMoving())
				animationManager.change("usewalk", animationMeta, useAnimationLength, animationInfo.doesStick(), animationInfo.canEndEarly());
			else
				animationManager.change("use", animationMeta, useAnimationLength, animationInfo.doesStick(), animationInfo.canEndEarly());
		}
	}
	
	public void setNewOnMap (boolean newOnMap) {
		this.newOnMap = newOnMap;
	}
	
	public boolean isNewOnMap () {
		return newOnMap;
	}
	
	public WebSocket getConnection () {
		return ArchipeloServer.getServer().getNetworkManager().getConnectionByAddress(address);
	}
	
	private synchronized void addCommand (ControlsPacket packet) {
		commandsToExecute.add(packet);
	}
	
	private synchronized ArrayList<ControlsPacket> getCommandsClone () {
		ArrayList<ControlsPacket> packets = new ArrayList<ControlsPacket>();
		for (ControlsPacket command : commandsToExecute)
			packets.add(command);
		return packets;
	}
	
	private synchronized void removeAllCommands (LinkedList<ControlsPacket> commandsToRemove) {
		commandsToExecute.removeAll(commandsToRemove);
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putString("displayInventory", inventory.getDisplayInventoryJson());
		snapshot.putFloat("playerSpeed", statsManager.getSpeed());
		snapshot.putInt("seed", seed);
		return snapshot;
	}
	
	public EntitySnapshot getFullPrivateSnapshot() {
		EntitySnapshot snapshot = this.getFullSnapshot();
		snapshot.putFloat("health", health);
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
				ControlsPacket cPacket = (ControlsPacket) packet;
				cPacket.time = System.currentTimeMillis();
				addCommand(cPacket);
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
			this.sendPacket(new ChatMessagePacket("", "{pong} " + hbUser.getPing() + "ms", "server"));
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
	
	public UnloadedLocation getRespawnLocation() {
		return respawnLocation;
	}
	
	@Override
	public boolean heal(float amount, Entity healer) {
		boolean dead = super.heal(amount, healer);
		this.sendPacket(new PlayerStatsPacket(health));//Update health on client
		return dead;
	}
	
	public Random getRandom() {
		return random;
	}

	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		if (isMoving()) {
			if (isSprinting()) {
				return new EntityAnimationObject("sprint");
			} else {//Walking
				return new EntityAnimationObject("walk");
			}
		} else {//Idle
			return new EntityAnimationObject("default");
		}
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
		playerData.respawnX = config.spawnX;
		playerData.respawnY = config.spawnY;
		playerData.respawnIsland = config.spawnIsland;
		playerData.respawnMap = config.spawnMap;
		playerData.health = EntityType.PLAYER.getMaxHealth();
		playerData.lastPlayed = DatabaseManager.getCurrentDate();
		playerData.creationDate = DatabaseManager.getCurrentDate();
		playerData.flags = new String[0];
		
		//Default inventory
		playerData.inventory = new Item[PlayerInventory.INVENTORY_SIZE];
		playerData.cosmeticInventory = new Item[EQUIP_SIZE];
		playerData.bankInventory = new Item[0];
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
