package net.hollowbit.archipeloserver.tools.audio;

public class SoundCalculator {
	
	public static final int HEARING_DISTANCE = 50;//tiles
	
	/**
	 * Calculate the modified pitch depending on the speed enhancement.
	 * It is calculated relative to the base speed.
	 * @param baseSpeed
	 * @param enhancedSpeed
	 * @return
	 */
	public static float calculatePitch (float baseSpeed, float enhancedSpeed) {
		if (baseSpeed <= 0)
			return 1;

		return enhancedSpeed / (baseSpeed * 20) + 0.95f;
	}
	
}
