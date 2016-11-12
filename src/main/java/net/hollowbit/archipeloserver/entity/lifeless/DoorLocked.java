package net.hollowbit.archipeloserver.entity.lifeless;

import net.hollowbit.archipeloserver.entity.EntitySnapshot;
import net.hollowbit.archipeloserver.entity.EntityType;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.world.Map;

public class DoorLocked extends Door {
	
	String unlockFlag;
	
	@Override
	public void create(EntitySnapshot fullSnapshot, Map map, EntityType entityType) {
		super.create(fullSnapshot, map, entityType);
		this.unlockFlag = fullSnapshot.getString("unlockFlag", map.getIsland().getName() + "-" + map.getName() + "-" + this.name + "unlock");//Default flag is perfectly valid to use
	}
	
	@Override
	public boolean ignoreHardnessOfCollisionRects(Player player, String rectName) {
		//If the player has the needed flag and this is the bottom rect, ignore the collision since it is unlocked
		if (rectName.equalsIgnoreCase("bottom") && player.getFlagsManager().hasFlag(unlockFlag))
			return true;
		else
			return super.ignoreHardnessOfCollisionRects(player, rectName);
	}
	
	@Override
	public EntitySnapshot getFullSnapshot() {
		EntitySnapshot snapshot = super.getFullSnapshot();
		snapshot.putString("unlockFlag", unlockFlag);
		return snapshot;
	}
	
	@Override
	public EntitySnapshot getSaveSnapshot() {
		EntitySnapshot snapshot = super.getSaveSnapshot();
		snapshot.putString("unlockFlag", unlockFlag);
		return snapshot;
	}
	
}
