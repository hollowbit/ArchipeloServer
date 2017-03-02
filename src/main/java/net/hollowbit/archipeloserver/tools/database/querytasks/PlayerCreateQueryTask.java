package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.tools.database.QueryTask;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;

public class PlayerCreateQueryTask extends QueryTask {
	
	private Player player;
	
	public PlayerCreateQueryTask (Player player) {
		super(QueryTaskType.PLAYER_CREATE);
		this.player = player;
	}

	@Override
	public void execute(Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("insert into players (`uuid`, `hbUuid`, `name`, `x`, `y`, `island`, `map`, `weaponInventory`, `consumablesInventory`, `buffsInventory`, `ammoInventory`, `uneditableEquippedInventory`, `equippedInventory`, `cosmeticInventory`, `bankInventory`, `inventory`, `lastPlayed`, `creationDate`, `flags`) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, player.getId());
			statement.setString(2, player.getHollowBitUser().getUUID());
			statement.setString(3, player.getName());
			statement.setFloat(4, player.getLocation().getX());
			statement.setFloat(5, player.getLocation().getY());
			statement.setString(6, player.getLocation().getIsland().getName());
			statement.setString(7, player.getLocation().getMap().getName());
			
			//Inventory
			statement.setString(8, player.getInventory().getWeaponInventory().getJson());
			statement.setString(9, player.getInventory().getConsumablesInventory().getJson());
			statement.setString(10, player.getInventory().getBuffsInventory().getJson());
			statement.setString(11, player.getInventory().getAmmoInventory().getJson());
			statement.setString(12, player.getInventory().getUneditableEquippedInventory().getJson());
			statement.setString(13, player.getInventory().getEquippedInventory().getJson());
			statement.setString(14, player.getInventory().getCosmeticInventory().getJson());
			statement.setString(15, player.getInventory().getBankInventory().getJson());
			statement.setString(16, player.getInventory().getMainInventory().getJson());

			statement.setDate(17, player.getLastPlayedDate());
			statement.setDate(18, player.getCreationDate());
			statement.setString(19, player.getFlagsManager().getFlagsJson());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			ArchipeloServer.getServer().getLogger().caution("Could not save player data to server.");
		}
	}

}
