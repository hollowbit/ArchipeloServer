package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.database.DatabaseManager;
import net.hollowbit.archipeloserver.tools.database.QueryTask;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;

public class PlayerUpdateQueryTask extends QueryTask {
	
	private Player player;

	public PlayerUpdateQueryTask (Player player) {
		super(QueryTaskType.PLAYER_UPDATE);
		this.player = player;
	}

	@Override
	public void execute(Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("update players set `name`=?, `x`=?, `y`=?, `island`=?, `map`=?, `weaponInventory`=?, `consumablesInventory`=?, `buffsInventory`=?, `ammoInventory`=?, `uneditableEquippedInventory`=?, `equippedInventory`=?, `cosmeticInventory`=?, `bankInventory`=?, `inventory`=?, `lastPlayed`=?, `flags`=?, `health`=?, `respawnX`=?, `respawnY`=?, `respawnIsland`=?, `respawnMap`=? where uuid = ?");
			statement.setString(1, player.getName());
			statement.setFloat(2, player.getLocation().getX());
			statement.setFloat(3, player.getLocation().getY());
			statement.setString(4, player.getLocation().getIsland().getName());
			statement.setString(5, player.getLocation().getMap().getName());
			
			//Inventory
			statement.setString(6, player.getInventory().getWeaponInventory().getJson());
			statement.setString(7, player.getInventory().getConsumablesInventory().getJson());
			statement.setString(8, player.getInventory().getBuffsInventory().getJson());
			statement.setString(9, player.getInventory().getAmmoInventory().getJson());
			statement.setString(10, player.getInventory().getUneditableEquippedInventory().getJson());
			statement.setString(11, player.getInventory().getEquippedInventory().getJson());
			statement.setString(12, player.getInventory().getCosmeticInventory().getJson());
			statement.setString(13, player.getInventory().getBankInventory().getJson());
			statement.setString(14, player.getInventory().getMainInventory().getJson());
			
			statement.setDate(15, DatabaseManager.getCurrentDate());
			statement.setString(16, player.getFlagsManager().getFlagsJson());
			
			statement.setFloat(17, player.getHealth());
			statement.setFloat(18, player.getRespawnLocation().getX());
			statement.setFloat(19, player.getRespawnLocation().getY());
			statement.setString(20, player.getRespawnLocation().getIsland());
			statement.setString(21, player.getRespawnLocation().getMap());
			
			statement.setString(22, player.getId());//Update where uuid is the same
			
			statement.executeUpdate();
		} catch (SQLException e) {
			ArchipeloServer.getServer().getLogger().caution("Was unable to update player data. " + e.getMessage());
		}
	}

}
