package net.hollowbit.archipeloserver.entity;

import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloshared.EntitySnapshot;

public abstract class EntityComponent {
	
	protected Entity entity;
	
	public EntityComponent(Entity entity) {
		this.entity = entity;
	}
	
	public void tick20 (float deltaTime) {}
	public void tick60 (float deltaTime) {}
	
	public void remove () {}
	
	/**
	 * This is used by certain entities which don't always want a collision rect to be hard.
	 * Ex: Like a locked door that becomes unlocked for some players.
	 * @param player
	 * @param rectName
	 * @return
	 */
	public boolean ignoreHardnessOfCollisionRects (Player player, String rectName) {
		return false;
	}
	
	/**
	 * Modify the interp snapshot being generated
	 * @param snapshot
	 * @return
	 */
	public EntitySnapshot editInterpSnapshot (EntitySnapshot snapshot) {
		return snapshot;
	}
	
	/**
	 * Modify the full snapshot being generated
	 * @param snapshot
	 * @return
	 */
	public EntitySnapshot editFullSnapshot (EntitySnapshot snapshot) {
		return snapshot;
	}
	
	/**
	 * Modify the save snapshot being generated
	 * @param snapshot
	 * @return
	 */
	public EntitySnapshot editSaveSnapshot (EntitySnapshot snapshot) {
		return snapshot;
	}
	
}
