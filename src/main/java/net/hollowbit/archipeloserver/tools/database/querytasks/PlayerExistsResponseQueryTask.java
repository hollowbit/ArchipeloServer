package net.hollowbit.archipeloserver.tools.database.querytasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.hollowbit.archipeloserver.tools.database.QueryTaskResponseHandler.PlayerExistsQueryTaskResponseHandler;
import net.hollowbit.archipeloserver.tools.database.QueryTaskType;
import net.hollowbit.archipeloserver.tools.database.ResponseQueryTask;

public class PlayerExistsResponseQueryTask extends ResponseQueryTask {
	
	private PlayerExistsQueryTaskResponseHandler handler;
	private String name;
	
	public PlayerExistsResponseQueryTask(String name, PlayerExistsQueryTaskResponseHandler handler) {
		super(QueryTaskType.PLAYER_EXISTS, handler);
		this.handler = handler;
		this.name = name;
	}

	@Override
	public void execute (Connection conn) {
		try {
			PreparedStatement statement = conn.prepareStatement("select uuid from players where name = ?");
			statement.setString(1, name);
			ResultSet rs = statement.executeQuery();
			handler.responseReceived(rs.next());
		} catch (SQLException e) {
			handler.responseReceived(true);
		}
	}

}
