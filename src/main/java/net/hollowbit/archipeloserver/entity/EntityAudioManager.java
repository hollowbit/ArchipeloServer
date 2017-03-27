package net.hollowbit.archipeloserver.entity;

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
	
}
