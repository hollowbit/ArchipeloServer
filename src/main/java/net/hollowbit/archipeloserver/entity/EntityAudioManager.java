package net.hollowbit.archipeloserver.entity;

public class EntityAudioManager {
	
	private String footstepSound;
	private Entity entity;
	
	public EntityAudioManager(Entity entity) {
		footstepSound = "";
		this.entity = entity;
	}
	
	public void setFootstepSound (String sound) {
		if (entity.getEntityType().hasFootstepSound())
			this.footstepSound = sound;
	}
	
	public void applyToInterpSnapshot (EntitySnapshot snapshot) {
		snapshot.footSound = footstepSound;
	}
	
	public void playSound (String sound) {
		if (entity.getEntityType().hasSound(sound))
			entity.changes.addSound(sound);
	}
	
}
