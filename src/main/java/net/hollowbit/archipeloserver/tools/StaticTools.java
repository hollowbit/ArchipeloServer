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
	
	public static float singleDimentionLerpFraction (double valueBefore, double valueAfter, double intermediateValue) {
		return (float) ((intermediateValue - valueBefore) / (valueAfter - valueBefore));
	}
	
	public static float singleDimensionLerp (double value1, double value2, double fraction) {
		return (float) (value1 + ((value2 - value1) * fraction));
	}
	
}
