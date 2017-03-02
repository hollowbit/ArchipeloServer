package net.hollowbit.archipeloserver.tools.database;

import java.sql.Connection;

public abstract class QueryTask {
	
	protected QueryTaskType type;
	
	protected QueryTask (QueryTaskType type) {
		this.type = type;
	}
	
	public QueryTaskType getType () {
		return this.type;
	}
	
	public abstract void execute (Connection conn);
	
}
