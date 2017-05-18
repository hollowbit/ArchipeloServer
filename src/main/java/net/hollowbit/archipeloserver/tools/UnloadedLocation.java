package net.hollowbit.archipeloserver.tools;

/**
 * Similar to Location however the map and island are not necessarily loaded up.
 * @author vedi0boy
 *
 */
public class UnloadedLocation {
	
	public float x, y;
	public String island;
	public String map;
	
	public UnloadedLocation(float x, float y, String island, String map) {
		this.x = x;
		this.y = y;
		this.island = island;
		this.map = map;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public String getIsland() {
		return island;
	}

	public void setIsland(String island) {
		this.island = island;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}
	
	
}
