package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.Point;

public class Spawner extends LifelessEntity {
	
	private int nextSpawnId;
	private EntitySnapshot spawnSnapshot;
	private int spawnWidth, spawnHeight;
	@SuppressWarnings("unused")
	private int spawnAmount;
	private float spawnRate;
	private float timer;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.nextSpawnId = 0;
		this.spawnSnapshot = fullSnapshot.getObject("spawnSnapshot", null, EntitySnapshot.class);
		this.spawnWidth = fullSnapshot.getInt("spawnWidth", 100);
		this.spawnHeight = fullSnapshot.getInt("spawnHeight", 100);
		this.spawnAmount = fullSnapshot.getInt("spawnAmount", 4);
		this.spawnRate = fullSnapshot.getFloat("spawnRate", 3f);
		this.timer = 0;
	}
	
	@Override
	public void tick20(float deltaTime) {
		timer += deltaTime;
		if (timer > spawnRate) {
			timer -= spawnRate;
			spawn();
		}
		
		super.tick20(deltaTime);
	}
	
	/**
	 * Spawn a new entity.
	 * @return
	 */
	protected void spawn() {
		float x = StaticTools.getRandom().nextInt(spawnWidth) + this.getX();
		float y = StaticTools.getRandom().nextInt(spawnHeight) + this.getY();
		Point p = new Point(x, y);
		
		String name = spawnSnapshot.name + "_" + nextSpawnId;
		nextSpawnId++;
		EntitySnapshot duplicate = new EntitySnapshot(spawnSnapshot);
		duplicate.name = name;
		duplicate.putObject("pos", p);
		
		Entity entity = EntityType.createEntityBySnapshot(duplicate, getMap());
		this.getMap().addEntity(entity);
	}
	
	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}

}
