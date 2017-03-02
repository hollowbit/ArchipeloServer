package net.hollowbit.archipeloserver.tools;

public enum LogType {
	
	INFO(),
	CAUTION(),
	ERROR();
	
	@Override
	public String toString () {
		return name().replace("_", " ");
	}
	
}
