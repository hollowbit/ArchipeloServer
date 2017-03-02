package net.hollowbit.archipeloserver.tools.database;

import java.util.ArrayList;

import net.hollowbit.archipeloserver.entity.living.player.PlayerData;

public abstract interface QueryTaskResponseHandler {
	
	public interface PlayerDataQueryTaskResponseHandler extends QueryTaskResponseHandler {
		
		public abstract void responseReceived (PlayerData playerData);
		
	}
	
	public interface PlayerListQueryTaskResponseHandler extends QueryTaskResponseHandler {
	
		public abstract void responseReceived (ArrayList<PlayerData> playerList);
		
	}
	
	public interface PlayerExistsQueryTaskResponseHandler extends QueryTaskResponseHandler {
	
		public abstract void responseReceived (boolean playerExists);
		
	}
	
	public interface PlayerCountQueryTaskResponseHandler extends QueryTaskResponseHandler {
	
		public abstract void responseReceived (int playerCount);
		
	}
}
