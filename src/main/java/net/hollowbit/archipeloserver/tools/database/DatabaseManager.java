package net.hollowbit.archipeloserver.tools.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.util.ArrayList;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.Configuration;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerCountQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerDataQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerExistsQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerListQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerCountResponseQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerCreateQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerDataResponseQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerDeleteQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerExistsResponseQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerListResponseQueryTask;
import net.hollowbit.archipeloserver.tools.database.querytasks.PlayerUpdateQueryTask;

public class DatabaseManager {
	
	private static final int MIN_MILLIS = 1000 / 20;//Minimum time to spend in a loop
	
	Connection connection;
	volatile ArrayList<QueryTask> queryTasks;
	Thread asyncThread;
	boolean running = false;
	
	public DatabaseManager () {
		this.queryTasks = new ArrayList<QueryTask>();
		
		//Connect to database
		try {
			Configuration config = ArchipeloServer.getServer().getConfig();
			connection = DriverManager.getConnection("jdbc:mysql://" + config.dbAddress + "/archipelo_server?autoReconnect=true", config.dbUsername, config.dbPassword);
			ArchipeloServer.getServer().getLogger().info("Connected to database!");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().error("Unable to connect to database!");
		}
	}
	
	/**
	 * Starts async tasks
	 */
	public void start () {
		if (asyncThread != null && running)
			stop();
		
		running = true;
		asyncThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (running) {
					long startTime = System.currentTimeMillis();
					
					//Process the currently queued query tasks
					ArrayList<QueryTask> tasks = cloneQueryTasks();
					for (QueryTask task : tasks) {
						task.execute(connection);
					}
					removeQueryTasks(tasks);//Remove them once finished
					
					//Make thread sleep if it has not reached minimum run time
					long timeDiff = System.currentTimeMillis() - startTime;
					if (timeDiff < MIN_MILLIS) {
						try {
							Thread.sleep(MIN_MILLIS - timeDiff);
						} catch (InterruptedException e) {}
					}
				}
			}
			
		});
		asyncThread.start();
	}
	
	/**
	 * 
	 */
	private synchronized ArrayList<QueryTask> cloneQueryTasks () {
		ArrayList<QueryTask> tasks = new ArrayList<QueryTask>();
		for (QueryTask task : queryTasks)
			tasks.add(task);
		return tasks;
	}
	
	private synchronized void removeQueryTasks (ArrayList<QueryTask> tasks) {
		queryTasks.removeAll(tasks);
	}
	
	private synchronized void addQueryTask (QueryTask task) {
		queryTasks.add(task);
	}
	
	/**
	 * Stops async tasks
	 */
	public void stop () {
		try {
			running = false;
			asyncThread.join();
		} catch (Exception e) {
			ArchipeloServer.getServer().getLogger().error("Could not stop database async thread.");
		}
	}
	
	/**
	 * Get player data of a specified player.
	 * @param name
	 * @param hbUuid
	 * @param handler Will be called once query response has been received
	 */
	public void getPlayerData (String name, String hbUuid, PlayerDataQueryTaskResponseHandler handler) {
		this.addQueryTask(new PlayerDataResponseQueryTask(name, hbUuid, handler));
	}
	
	/**
	 * Gets player data of all players belonging to the specified user
	 * @param hbUuid
	 * @param handler Will be called once query response has been received
	 */
	public void getPlayerListFromUser (String hbUuid, PlayerListQueryTaskResponseHandler handler) {
		this.addQueryTask(new PlayerListResponseQueryTask(hbUuid, handler));
	}
	
	/**
	 * Returns whether a player with that name exists.
	 * @param name
	 * @param handler Will be called once query response has been received
	 */
	public void doesPlayerExist (String name, PlayerExistsQueryTaskResponseHandler handler) {
		this.addQueryTask(new PlayerExistsResponseQueryTask(name, handler));
	}
	
	/**
	 * Gets player count for this user
	 * @param hbUuid UUID of user to test for
	 * @param handler Will be called once query response has been received
	 */
	public void getPlayerCount (String hbUuid, PlayerCountQueryTaskResponseHandler handler) {
		this.addQueryTask(new PlayerCountResponseQueryTask(hbUuid, handler));
	}
	
	/**
	 * Create a new table row with player data
	 * @param player
	 */
	public void createPlayer (Player player) {
		this.addQueryTask(new PlayerCreateQueryTask(player));
	}
	/**
	 * Update data of a player.
	 * @param player
	 */
	public void updatePlayer (Player player) {
		this.addQueryTask(new PlayerUpdateQueryTask(player));
	}
	
	/**
	 * Deletes player belonging to HBU. If this HBU doesn't own this player, it won't be deleted
	 * @param name
	 * @param hbUuid
	 */
	public void deletePlayer (String name, String hbUuid) {
		this.addQueryTask(new PlayerDeleteQueryTask(name, hbUuid));
	}
	
	/**
	 * Returns the current datetime in java.sql.Date
	 * @return
	 */
	public static final Date getCurrentDate () {
		return new Date(System.currentTimeMillis());
	}
	
}
