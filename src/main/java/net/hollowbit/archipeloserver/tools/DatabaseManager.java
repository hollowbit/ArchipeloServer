package net.hollowbit.archipeloserver.tools;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.items.Item;

public class DatabaseManager {
	
	Connection connection;
	
	public DatabaseManager () {
		//Connect to database
		try {
			Configuration config = ArchipeloServer.getServer().getConfig();
			connection = DriverManager.getConnection("jdbc:mysql://" + config.dbAddress + "/archipelo_server", config.dbUsername, config.dbPassword);
			ArchipeloServer.getServer().getLogger().info("Connected to database!");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().error("Unable to connect to database!");
		}
	}
	
	/**
	 * Get player data of a specified player.
	 * @param name
	 * @param hbUuid
	 * @return
	 */
	public PlayerData getPlayerData (String name, String hbUuid) {
		//Query database to get info on a player
		try {
			PreparedStatement statement = connection.prepareStatement("select * from players where name = ? and hbUuid = ?");
			statement.setString(1, name);
			statement.setString(2, hbUuid);
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return null;
			
			//Fill player data object with info and return
			PlayerData pd = new PlayerData();
			pd.uuid = rs.getString("uuid");
			pd.name = rs.getString("name");
			pd.bhUuid = rs.getString("hbUuid");
			pd.x = rs.getFloat("x");
			pd.y = rs.getFloat("y");
			pd.island = rs.getString("island");
			pd.map = rs.getString("map");
			pd.lastPlayed = rs.getDate("lastPlayed");
			pd.creationDate = rs.getDate("creationDate");
			
			//Inventory
			Json json = new Json();
			pd.equippedInventory = json.fromJson(Item[].class, rs.getString("equippedInventory"));
			pd.inventory = json.fromJson(Item[].class, rs.getString("inventory"));
			return pd;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get user data for " + name);
			return null;
		}
	}
	
	/**
	 * Gets player data of all players belonging to the specified user
	 * @param hbUuid
	 * @return
	 */
	public ArrayList<PlayerData> getPlayerDataFromUser (String hbUuid) {
		//Query database to get info on a player
		try {
			PreparedStatement statement = connection.prepareStatement("select name, island, lastPlayed, creationDate, equippedInventory from players where hbUuid = ?");
			statement.setString(1, hbUuid);
			ResultSet rs = statement.executeQuery();
			
			ArrayList<PlayerData> playerDatas = new ArrayList<PlayerData>();
			
			//Loop through rows and add them to list
			while (rs.next()) {
				//Fill player data object with info and return
				PlayerData pd = new PlayerData();
				pd.name = rs.getString("name");
				pd.island = rs.getString("island");
				pd.lastPlayed = rs.getDate("lastPlayed");
				pd.creationDate = rs.getDate("creationDate");
				
				//Inventory
				Json json = new Json();
				pd.equippedInventory = json.fromJson(Item[].class, rs.getString("equippedInventory"));
				
				playerDatas.add(pd);
			}
			return playerDatas;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get user data for user " + hbUuid);
			return null;
		}
	}
	
	/**
	 * Create a new table row with player data
	 * @param player
	 */
	public void createPlayer (Player player) {
		Thread thread = new Thread(new Runnable() {//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				//Insert row to database for player
				try {
					PreparedStatement statement = connection.prepareStatement("insert into players (`uuid`, `hbUuid`, `name`, `x`, `y`, `island`, `map`, `equippedInventory`, `inventory`, `lastPlayed`, `creationDate`, `conditions`) values (?,?,?,?,?,?,?,?,?,?,?,?)");
					statement.setString(1, player.getUUID());
					statement.setString(2, player.getHollowBitUser().getUUID());
					statement.setString(3, player.getName());
					statement.setFloat(4, player.getLocation().getX());
					statement.setFloat(5, player.getLocation().getY());
					statement.setString(6, player.getLocation().getIsland().getName());
					statement.setString(7, player.getLocation().getMap().getName());
					
					//Inventory
					Json json = new Json();
					statement.setString(8, json.toJson(player.getEquippedInventory()));
					statement.setString(9, json.toJson(player.getInventory()));

					statement.setDate(10, player.getLastPlayedDate());
					statement.setDate(11, player.getCreationDate());
					statement.setString(12, player.getConditionsManager().getConditionsJson());
					
					statement.executeUpdate();
				} catch (SQLException e) {
					ArchipeloServer.getServer().getLogger().caution("Could not save player data to server.");
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Returns whether a player with that name exists.
	 * @param name
	 * @return
	 */
	public boolean doesPlayerExist (String name) {
		try {
			PreparedStatement statement = connection.prepareStatement("select * from players where name = ?");
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			return true;
		}
	}
	
	/**
	 * Update data of a player.
	 * @param player
	 */
	public void updatePlayer (Player player) {
		Thread thread = new Thread(new Runnable(){//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				//Update player row in database
				try {
					PreparedStatement statement = connection.prepareStatement("update players set `name`=?, `x`=?, `y`=?, `island`=?, `map`=?, `equippedInventory`=?, `inventory`=?, `lastPlayed`=?, `conditions`=? where uuid = ?");
					statement.setString(1, player.getName());
					statement.setFloat(2, player.getLocation().getX());
					statement.setFloat(3, player.getLocation().getY());
					statement.setString(4, player.getLocation().getIsland().getName());
					statement.setString(5, player.getLocation().getMap().getName());
					
					//Inventory
					Json json = new Json();
					statement.setString(6, json.toJson(player.getEquippedInventory()));
					statement.setString(7, json.toJson(player.getInventory()));
					
					statement.setDate(8, getCurrentDate());
					statement.setString(9, player.getConditionsManager().getConditionsJson());
					
					statement.setString(10, player.getUUID());//Update where uuid is the same
					
					statement.executeUpdate();
				} catch (SQLException e) {
					ArchipeloServer.getServer().getLogger().caution("Was unable to update player data. " + e.getMessage());
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Deletes player belonging to HBU. If this HBU doesn't own this player, it won't be deleted
	 * @param name
	 * @param hbUuid
	 */
	public void deletePlayer (String name, String hbUuid) {
		Thread thread = new Thread(new Runnable(){//Use a thread so that this task is done asynchronously
			@Override
			public void run() {
				//Update player row in database
				try {
					PreparedStatement statement = connection.prepareStatement("delete from players where name = ? and hbUuid = ?");
					statement.setString(1, name);
					statement.setString(2, hbUuid);
					
					statement.executeUpdate();
					System.out.println("DatabaseManager.java Player deleted: " + name);
				} catch (SQLException e) {
					ArchipeloServer.getServer().getLogger().caution("Was unable to delete player: " + name + "   Error: " + e.getMessage());
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Gets player count for this user
	 * @param hbUuid UUID of user to test for
	 * @return
	 */
	public int getPlayerCount (String hbUuid) {
		try {
			PreparedStatement statement = connection.prepareStatement("select count(*) as 'count' from players where hbUuid = ?");
			statement.setString(1, hbUuid);
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return 0;
			
			return rs.getInt("count");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get player count for user " + hbUuid);
			return 0;
		}
	}
	
	public static final Date getCurrentDate () {
		return new Date(Calendar.getInstance().getTime().getTime());
	}
	
}
