package net.hollowbit.archipeloserver.entity.lifeless;

import java.util.LinkedList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.entity.EntityAnimationManager.EntityAnimationObject;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.LifelessEntity;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloserver.tools.event.EventHandler;
import net.hollowbit.archipeloserver.tools.event.EventType;
import net.hollowbit.archipeloserver.tools.event.events.editable.EntityDeathEvent;
import net.hollowbit.archipeloserver.world.Map;
import net.hollowbit.archipeloshared.EntitySnapshot;
import net.hollowbit.archipeloshared.Point;
import net.hollowbit.archipeloshared.SavedRectangle;

public class Spawner extends LifelessEntity implements EventHandler {
	
	private int nextSpawnId;
	private EntitySnapshot spawnSnapshot;
	private SavedRectangle spawnRect;
	private int spawnAmount;
	private float spawnRate;
	private float timer;
	private EntityType spawnType;
	private LinkedList<Entity> spawnedEntities;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.spawnedEntities = new LinkedList<Entity>();
		this.nextSpawnId = 0;
		this.spawnSnapshot = fullSnapshot.getObject("spawnSnapshot", null, EntitySnapshot.class);
		this.spawnRect = fullSnapshot.getObject("spawnRect", new SavedRectangle((int) this.location.getX(), (int) this.location.getY(), ArchipeloServer.TILE_SIZE, ArchipeloServer.TILE_SIZE), SavedRectangle.class);
		this.spawnAmount = fullSnapshot.getInt("spawnAmount", 4);
		this.spawnRate = fullSnapshot.getFloat("spawnRate", 3f);
		this.timer = 0;
		this.spawnType = EntityType.getEntityTypeById(spawnSnapshot.type);
		this.addToEventManager(EventType.EntityDeath);
	}
	
	@Override
	public void tick20(float deltaTime) {
		if (spawnedEntities.size() < spawnAmount) {//Only spawn entities if under max
			timer += deltaTime;
			if (timer > spawnRate) {
				if (spawn())
					timer = 0;
			}
		}
		
		super.tick20(deltaTime);
	}
	
	@Override
	public void remove() {
		this.removeFromEventManager();
		super.remove();
	}
	
	@Override
	public boolean onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity().getMap() == this.getMap() && event.getEntity().getEntityType() == spawnType)
			return spawnedEntities.remove(event.getEntity());
		return false;
	}
	
	/**
	 * Spawn a new entity.
	 * Returns whether entity was spawned.
	 * @return
	 */
	protected boolean spawn() {
		String name = spawnSnapshot.name + "_" + nextSpawnId;
		nextSpawnId++;
		EntitySnapshot duplicate = new EntitySnapshot(spawnSnapshot);
		duplicate.name = name;
		
		float x = StaticTools.getRandom().nextInt(spawnRect.getWidth()) + spawnRect.getX();
		float y = StaticTools.getRandom().nextInt(spawnRect.getHeight()) + spawnRect.getY();
		duplicate.putObject("pos", new Point(x, y));
		Entity entity = EntityType.createEntityBySnapshot(duplicate, getMap());
		
		if (getMap().collidesWithMap(entity.getCollisionRects(new Vector2(x, y)), entity))
			return false;
		
		this.spawnedEntities.add(entity);
		this.getMap().addEntity(entity);
		return true;
	}
	
	@Override
	public EntityAnimationObject animationCompleted(String animationId) {
		return null;
	}

}
