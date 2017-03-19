package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.tools.StaticTools;

public class EntityLog {
	
	public static final int ENTRY_LIFETIME = 2000;//Milliseconds until snapshot is dumped.
	
	ArrayList<EntityLogEntry> logEntries;
	
	public EntityLog () {
		logEntries = new ArrayList<EntityLogEntry>();
	}
	
	public synchronized void addEntry (EntityLogEntry entry) {
		logEntries.add(entry);
	}
	
	private synchronized EntityLogEntry getBefore (long millis) {
		if (logEntries.isEmpty())
			return null;
		
		EntityLogEntry closest = null;
		for (EntityLogEntry entry : logEntries) {
			if (entry.creationTime > millis)
				continue;
			
			if (closest == null)
				closest = entry;
			else {
				if (entry.creationTime > closest.creationTime)
					closest = entry;
			}
		}
		return closest;
	}
	
	private synchronized EntityLogEntry getAfter (long millis) {
		if (logEntries.isEmpty())
			return null;
		
		EntityLogEntry closest = null;
		for (EntityLogEntry entry : logEntries) {
			if (entry.creationTime <= millis)
				continue;
			
			if (closest == null)
				closest = entry;
			else {
				if (entry.creationTime > closest.creationTime)
					closest = entry;
			}
		}
		return closest;
	}
	
	/**
	 * Returns the interpolated position of this entity at the specified timestamp.
	 * Will return null if not enough entries have been recorded so far, or if the specified time is older than 2 seconds.
	 * @param millis
	 * @return
	 */
	public Vector2 getPositionAtTimestamp (long millis) {
		EntityLogEntry entryBefore = this.getBefore(millis);
		EntityLogEntry entryAfter = this.getAfter(millis);
		
		//If either is null, use current position instead
		if (entryBefore == null || entryAfter == null)
			return null;
		
		float fraction = this.getFraction(entryBefore, entryAfter, millis);
		float x = StaticTools.singleDimensionLerp(entryBefore.x, entryAfter.x, fraction);
		float y = StaticTools.singleDimensionLerp(entryBefore.y, entryAfter.y, fraction);
		
		return new Vector2(x, y);
	}
	
	private float getFraction (EntityLogEntry entryBefore, EntityLogEntry entryAfter, long millis) {
		return StaticTools.singleDimentionLerpFraction(entryBefore.creationTime, entryAfter.creationTime, millis);
	}
	
	public synchronized void removeOldEntitySnapshotsFromLog () {
		ArrayList<EntityLogEntry> entriesToRemove = new ArrayList<EntityLogEntry>();
		for (EntityLogEntry entry : logEntries) {
			long deltatime = System.currentTimeMillis() - entry.creationTime;
			if (deltatime > ENTRY_LIFETIME)
				entriesToRemove.add(entry);
		}
		logEntries.removeAll(entriesToRemove);
	}
	
	public synchronized void clearAll () {
		logEntries.clear();
	}
	
}
