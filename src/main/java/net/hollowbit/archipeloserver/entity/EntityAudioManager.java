package net.hollowbit.archipeloserver.entity;

import net.hollowbit.archipeloshared.EntitySnapshot;

public class EntityAudioManager {
	
	private String footstepSound;
	private float pitch;
	private Entity entity;
	
	public EntityAudioManager(Entity entity) {
		footstepSound = "";
		this.entity = entity;
		this.pitch = 1;
	}
	
	public void stopFootstepSound () {
		this.setFootstepSound("", 1);
	}
	
	public void setFootstepSound (String sound, float pitch) {
		if (entity.getEntityType().hasFootstepSound()) {
			this.footstepSound = sound;
			this.pitch = pitch;
		}
	}
	
	public void applyToInterpSnapshot (EntitySnapshot snapshot) {
		snapshot.footSound = footstepSound;
		snapshot.footPitch = pitch;
	}
	
	public void playSound (String sound) {
		if (entity.getEntityType().hasSound(sound))
			entity.changes.addSound(sound);
	}
	
	/**
	 * Plays a sound for this entity that is not necessarily for it.
	 * You can use any sound path here. This is used to avoid sound sync issues with clients.
	 * @param sound
	 */
	public void playUnsafeSound (String sound) {
		entity.changes.addUnsafeSound(sound);
	}
	
}
