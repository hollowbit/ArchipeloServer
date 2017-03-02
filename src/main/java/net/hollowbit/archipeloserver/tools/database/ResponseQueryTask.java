package net.hollowbit.archipeloserver.tools.database;

import java.sql.Connection;

public abstract class ResponseQueryTask extends QueryTask {
	
	protected ResponseQueryTask (QueryTaskType type, QueryTaskResponseHandler handler) {//Response handler in constructor to ensure it is being asked for
		super(type);
	}

	@Override
	public abstract void execute (Connection conn);

}
