package net.hollowbit.archipeloserver.tools;

import com.badlogic.gdx.utils.Json;

public class StaticTools {
	
	private static final Json json = new Json();
	
	public static Json getJson () {
		return json;
	}
	
}
