package net.hollowbit.archipeloserver;

import java.net.URISyntaxException;

import net.hollowbit.archipeloserver.hollowbitserver.HollowBitServerConnectivity;
import net.hollowbit.archipeloserver.network.NetworkManager;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.DatabaseManager;
import net.hollowbit.archipeloserver.tools.ExecutableManager;
import net.hollowbit.archipeloserver.tools.Logger;
import net.hollowbit.archipeloserver.tools.NpcDialogManager;
import net.hollowbit.archipeloserver.tools.PasswordHasher;
import net.hollowbit.archipeloserver.world.MapElementManager;
import net.hollowbit.archipeloserver.world.World;

public class ArchipeloServer {

	public static final float TICK30 = 1 / 30f;
	public static final float TICK60 = 1 / 60f;
	public static final float TILE_SIZE = 16;
	public static final String VERSION = "0.1a";
	public static final int MAX_CHARACTERS_PER_PLAYER = 4;
	
	//Static Methods
	private static ArchipeloServer server;	
	
	public static ArchipeloServer getServer () {
		return server;
	}
	////////////////
	
	private NetworkManager networkManager;
	private MapElementManager mapElementManager;
	private DatabaseManager databaseManager;
	private Configuration config;
	private PasswordHasher passwordHasher;
	private HollowBitServerConnectivity hollowBitServerConnectivity;
	private ExecutableManager executor;
	private NpcDialogManager npcDialogManager;
	private Logger logger;
	private World world;
	private Thread tick30;
	private Thread tick60;
	private boolean running = true;
	
	public ArchipeloServer () {
		server = this;
		logger = new Logger();
		config = new Configuration();
		passwordHasher = new PasswordHasher();
		networkManager = new NetworkManager(22122);
		networkManager.start();
		databaseManager = new DatabaseManager();
		try {
			hollowBitServerConnectivity = new HollowBitServerConnectivity();
			hollowBitServerConnectivity.connect();
		} catch (URISyntaxException e1) {}
		
		executor = new ExecutableManager();
		npcDialogManager = new NpcDialogManager();
		
		world = new World();
		mapElementManager = new MapElementManager();
		mapElementManager.loadMapElements();
		
		tick30 = new Thread(new Runnable() {

			@Override
			public synchronized void run() {
				while (running) {
					long startTime = System.currentTimeMillis();
					world.tick20();
					long timeToSleep = (long) (TICK30 * 1000 - (System.currentTimeMillis() - startTime));
					if (timeToSleep >= 0) {
						try {
							Thread.sleep(timeToSleep);
						} catch (InterruptedException e) {}
					} else {
						logger.caution("Server is running " + Math.abs(timeToSleep) + "ms behind.");
					}
				}
			}
			
		});
		tick30.start();
		
		tick60 = new Thread(new Runnable() {

			@Override
			public synchronized void run() {
				while (running) {
					long startTime = System.currentTimeMillis();
					networkManager.update();
					world.tick60();
					long timeToSleep = (long) (TICK60 * 1000 - (System.currentTimeMillis() - startTime));
					if (timeToSleep >= 0) {
						try {
							Thread.sleep(timeToSleep);
						} catch (InterruptedException e) {}
					} else {
						logger.caution("Server is running " + Math.abs(timeToSleep) + "ms behind.");
					}
				}
			}
			
		});
		tick60.start();
		
		logger.info("Server Started!");
	}
	
	public NetworkManager getNetworkManager () {
		return networkManager;
	}
	
	public MapElementManager getMapElementManager () {
		return mapElementManager;
	}
	
	public DatabaseManager getDatabaseManager () {
		return databaseManager;
	}
	
	public PasswordHasher getPasswordHasher () {
		return passwordHasher;
	}
	
	public Configuration getConfig () {
		return config;
	}
	
	public Logger getLogger () {
		return logger;
	}
	
	public World getWorld () {
		return world;
	}
	
	public HollowBitServerConnectivity getHollowBitServerConnectivity () {
		return hollowBitServerConnectivity;
	}
	
	public ExecutableManager getExecutor () {
		return executor;
	}
	
	public NpcDialogManager getNpcDialogManager () {
		return npcDialogManager;
	}
	
	public void stop () {
		hollowBitServerConnectivity.sendRemoveServerQuery();
		hollowBitServerConnectivity.close();
		running = false;
		try {
			if (tick30 != null && tick60 != null) {
				tick30.join();
				tick60.join();
			}
		} catch (InterruptedException e) {}
		logger.save();
		config.save();
	}
	
}