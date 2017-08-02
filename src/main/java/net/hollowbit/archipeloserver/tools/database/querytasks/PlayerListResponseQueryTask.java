package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.badlogic.gdx.utils.Json;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.player.PlayerData;
import net.hollowbit.archipeloserver.items.Item;
import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerListQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;
import net.hollowbit.archipeloserver.tools.database.ResponseQueryTask;

public class PlayerListResponseQueryTask extends ResponseQueryTask {
	
	private PlayerListQueryTaskResponseHandler handler;
	private String hbUuid;
	
	public PlayerListResponseQueryTask(String hbUuid, PlayerListQueryTaskResponseHandler handler) {
		super(QueryTaskType.PLAYER_LIST, handler);
		this.handler = handler;
		this.hbUuid = hbUuid;
	}

	@Override
	public void execute(Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("select name, map, lastPlayed, creationDate, weaponInventory, uneditableEquippedInventory, equippedInventory, cosmeticInventory from players where hbUuid = ? and active = 1");
			statement.setString(1, hbUuid);
			ResultSet rs = statement.executeQuery();
			
			ArrayList<PlayerData> playerDatas = new ArrayList<PlayerData>();
			
			//Loop through rows and add them to list
			while (rs.next()) {
				//Fill player data object with info and return
				PlayerData pd = new PlayerData();
				pd.name = rs.getString("name");
				pd.map = rs.getString("map");
				pd.lastPlayed = rs.getDate("lastPlayed");
				pd.creationDate = rs.getDate("creationDate");
				
				//Inventory
				Json json = new Json();
				pd.weaponInventory = json.fromJson(Item[].class, rs.getString("weaponInventory"));
				pd.uneditableEquippedInventory = json.fromJson(Item[].class, rs.getString("uneditableEquippedInventory"));
				pd.equippedInventory = json.fromJson(Item[].class, rs.getString("equippedInventory"));
				pd.cosmeticInventory = json.fromJson(Item[].class, rs.getString("cosmeticInventory"));
				
				playerDatas.add(pd);
			}
			handler.responseReceived(playerDatas);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get user data for user " + hbUuid);
			handler.responseReceived(null);
		}
	}

}
