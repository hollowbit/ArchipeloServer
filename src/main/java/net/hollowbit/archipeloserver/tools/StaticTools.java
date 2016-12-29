package net.hollowbit.archipeloserver.tools;

import java.util.Random;

import com.badlogic.gdx.utils.Json;

public class StaticTools {
	
	private static final Json json = new Json();
	private static final Random random = new Random();
	
	public static Json getJson () {
		return json;
	}
	
	public static Random getRandom () {
		return random;
	}
	
}
