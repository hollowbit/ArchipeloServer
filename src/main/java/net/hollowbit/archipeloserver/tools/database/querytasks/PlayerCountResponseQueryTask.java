package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerCountQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;
import net.hollowbit.archipeloserver.tools.database.ResponseQueryTask;

public class PlayerCountResponseQueryTask extends ResponseQueryTask {
	
	private PlayerCountQueryTaskResponseHandler handler;
	private String hbUuid;
	
	public PlayerCountResponseQueryTask(String hbUuid, PlayerCountQueryTaskResponseHandler handler) {
		super(QueryTaskType.PLAYER_COUNT, handler);
		this.hbUuid = hbUuid;
		this.handler = handler;
	}

	@Override
	public void execute (Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("select count(*) as 'count' from players where hbUuid = ? and active = 1");
			statement.setString(1, hbUuid);
			ResultSet rs = statement.executeQuery();
			if (!rs.next()) {
				handler.responseReceived(0);
			} else {
				handler.responseReceived(rs.getInt("count"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			ArchipeloServer.getServer().getLogger().caution("Unable to get player count for user " + hbUuid);
			handler.responseReceived(0);
		}
	}

}
