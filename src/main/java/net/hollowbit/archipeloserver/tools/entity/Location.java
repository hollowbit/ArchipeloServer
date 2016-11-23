package net.hollowbit.archipeloserver.tools.entity;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.world.Island;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloserver.world.World;
import net.hollowbit.archipeloshared.Direction;

public class Location {
	
	public Direction direction = Direction.UP;
	public Vector2 pos;
	public Map map;
	
	public Location (Map map, Vector2 pos) {
		this.map = map;
		this.pos = pos;
	}
	
	public Location (Map map, Vector2 pos, int direction) {
		this (map, pos, Direction.values()[direction]);
	}
	
	public Location (Map map, Vector2 pos, Direction direction) {
		this (map, pos);
		this.direction = direction;
	}
	
	public void set (Vector2 newPos) {
		pos.set(newPos);
	}
	
	public float getX () {
		return pos.x;
	}
	
	public float getY () {
		return pos.y;
	}
	
	public int getDirectionInt () {
		return direction.ordinal();
	}
	
	public Direction getDirection () {
		return direction;
	}
	
	public void setDirection (Direction direction) {
		this.direction = direction;
	}
	
	public void setMap (Map map) {
		this.map = map;
	}
	
	public Map getMap () {
		return map;
	}
	
	public Island getIsland () {
		return map.getIsland();
	}
	
	public World getWorld () {
		return map.getIsland().getWorld();
	}
	
	public void addY (float amount) {
		pos.add(0, amount);
	}
	
	public void addX (float amount) {
		pos.add(amount, 0);
	}
	
}
