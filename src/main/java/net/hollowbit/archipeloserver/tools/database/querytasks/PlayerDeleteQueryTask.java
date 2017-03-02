package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.database.QueryTask;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;

public class PlayerDeleteQueryTask extends QueryTask {

	private String name;
	private String hbUuid;
	
	public PlayerDeleteQueryTask (String name, String hbUuid) {
		super(QueryTaskType.PLAYER_DELETE);
		this.name = name;
		this.hbUuid = hbUuid;
	}

	@Override
	public void execute(Connection conn) {
		try {
			//If this HBU doesn't own this player, it won't be deleted. Prevents deleting another player's account
			PreparedStatement statement = conn.prepareStatement("update players set `active`=0 where name = ? and hbUuid = ?");
			statement.setString(1, name);
			statement.setString(2, hbUuid);
			
			statement.executeUpdate();
			ArchipeloServer.getServer().getLogger().info("DatabaseManager.java Player deleted: " + name);
		} catch (SQLException e) {
			ArchipeloServer.getServer().getLogger().caution("Was unable to delete player: " + name + "   Error: " + e.getMessage());
		}
	}

}
