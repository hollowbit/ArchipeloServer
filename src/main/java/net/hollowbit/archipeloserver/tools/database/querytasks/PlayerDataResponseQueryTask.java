package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerDataQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;
import net.hollowbit.archipeloserver.tools.database.ResponseQueryTask;

public class PlayerDataResponseQueryTask extends ResponseQueryTask {
	
	private PlayerDataQueryTaskResponseHandler handler;
	private String name;
	private String hbUuid;
	
	public PlayerDataResponseQueryTask (String name, String hbUuid, PlayerDataQueryTaskResponseHandler handler) {
		super(QueryTaskType.PLAYER_DATA, handler);
		this.handler = handler;
		this.name = name;
		this.hbUuid = hbUuid;
	}

	@Override
	public void execute (Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("select * from players where name = ? and hbUuid = ?");
			statement.setString(1, name);
			statement.setString(2, hbUuid);
			ResultSet rs = statement.executeQuery();
			if (!rs.next()) {
				handler.responseReceived(null);
				return;
			}
			
			//Fill player data object with info and return
			PlayerData pd = new PlayerData();
			pd.id = rs.getString("uuid");
			pd.name = rs.getString("name");
			pd.bhUuid = rs.getString("hbUuid");
			pd.x = rs.getFloat("x");
			pd.y = rs.getFloat("y");
			pd.map = rs.getString("map");
			pd.lastPlayed = rs.getDate("lastPlayed");
			pd.creationDate = rs.getDate("creationDate");
			pd.health = rs.getFloat("health");
			pd.respawnX = rs.getFloat("respawnX");
			pd.respawnY = rs.getFloat("respawnY");
			pd.respawnMap = rs.getString("respawnMap");
			
			Json json = new Json();
			pd.flags = json.fromJson(String[].class, rs.getString("flags"));
			
			//Inventory
			pd.uneditableEquippedInventory = json.fromJson(Item[].class, rs.getString("uneditableEquippedInventory"));
			pd.equippedInventory = json.fromJson(Item[].class, rs.getString("equippedInventory"));
			pd.cosmeticInventory = json.fromJson(Item[].class, rs.getString("cosmeticInventory"));
			pd.bankInventory = json.fromJson(Item[].class, rs.getString("bankInventory"));
			pd.inventory = json.fromJson(Item[].class, rs.getString("inventory"));
			pd.weaponInventory = json.fromJson(Item[].class, rs.getString("weaponInventory"));
			pd.consumablesInventory = json.fromJson(Item[].class, rs.getString("consumablesInventory"));
			pd.buffsInventory = json.fromJson(Item[].class, rs.getString("buffsInventory"));
			pd.ammoInventory = json.fromJson(Item[].class, rs.getString("ammoInventory"));
			handler.responseReceived(pd);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get user data for " + name);
			handler.responseReceived(null);
		}
	}

}
