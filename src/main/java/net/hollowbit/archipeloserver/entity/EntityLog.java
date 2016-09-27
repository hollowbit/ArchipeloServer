package net.hollowbit.archipeloserver.entity;

import java.util.ArrayList;

public class EntityLog {
	
	public static final int ENTRY_LIFETIME = 2000;//Milliseconds until snapshot is dumped.
	
	ArrayList<EntityLogEntry> logEntries;
	
	public EntityLog () {
		logEntries = new ArrayList<EntityLogEntry>();
	}
	
	public synchronized void addEntry (EntityLogEntry entry) {
		logEntries.add(entry);
	}
	
	public synchronized EntityLogEntry getClosestToTime (long millis) {
		long smallestDelta = Long.MAX_VALUE;
		EntityLogEntry smallestDeltaEntry = null;
		for (EntityLogEntry entry : logEntries) {
			long delta = Math.abs(millis - entry.creationTime);
			if (delta < smallestDelta) {
				smallestDelta = delta;
				smallestDeltaEntry = entry;
			}
		}
		return smallestDeltaEntry;
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
