package net.hollowbit.archipeloserver.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
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
	
	public PlayerData getPlayerData (String name) {
		//Query database to get info on a player
		try {
			PreparedStatement statement = connection.prepareStatement("select * from players where name = ?");
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			if (!rs.next())
				return null;
			
			//Fill player data object with info and return
			PlayerData pd = new PlayerData();
			pd.uuid = rs.getString("uuid");
			pd.name = rs.getString("name");
			pd.hashedPassword = rs.getBytes("password");
			pd.salt = rs.getBytes("salt");
			pd.x = rs.getFloat("x");
			pd.y = rs.getFloat("y");
			pd.island = rs.getString("island");
			pd.map = rs.getString("map");
			pd.hasCreatedPlayer = rs.getBoolean("hasCreatedPlayer");
			
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
	
	public void createPlayer (Player player) {
		//Insert row to database for player
		try {
			PreparedStatement statement = connection.prepareStatement("insert into players (`uuid`, `name`, `password`, `salt`, `x`, `y`, `island`, `map`, `equippedInventory`, `inventory`, `hasCreatedPlayer`) values (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getUUID());
			statement.setString(2, player.getName());
			statement.setBytes(3, player.getHashedPassword());
			statement.setBytes(4, player.getSalt());
			statement.setFloat(5, player.getLocation().getX());
			statement.setFloat(6, player.getLocation().getY());
			statement.setString(7, player.getLocation().getIsland().getName());
			statement.setString(8, player.getLocation().getMap().getName());
			
			//Inventory
			Json json = new Json();
			statement.setString(9, json.toJson(player.getEquippedInventory()));
			statement.setString(10, json.toJson(player.getInventory()));

			statement.setBoolean(11, player.hasCreatedPlayer());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			ArchipeloServer.getServer().getLogger().caution("Could not save player data to server.");
		}
	}
	
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
	
	public void updatePlayer (Player player) {
		//Update player row in database
		try {
			PreparedStatement statement = connection.prepareStatement("update players set `name`=?, `password`=?, `salt`=?, `x`=?, `y`=?, `island`=?, `map`=?, `equippedInventory`=?, `inventory`=?, `hasCreatedPlayer`=? where uuid = ?");
			statement.setString(1, player.getName());
			statement.setBytes(2, player.getHashedPassword());
			statement.setBytes(3, player.getSalt());
			statement.setFloat(4, player.getLocation().getX());
			statement.setFloat(5, player.getLocation().getY());
			statement.setString(6, player.getLocation().getIsland().getName());
			statement.setString(7, player.getLocation().getMap().getName());
			statement.setString(11, player.getUUID());//Update where uuid is the same
			
			//Inventory
			Json json = new Json();
			statement.setString(8, json.toJson(player.getEquippedInventory()));
			statement.setString(9, json.toJson(player.getInventory()));
			
			statement.setBoolean(10, player.hasCreatedPlayer());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			ArchipeloServer.getServer().getLogger().caution("Was unable to update player data. " + e.getMessage());
		}
	}
	
}
