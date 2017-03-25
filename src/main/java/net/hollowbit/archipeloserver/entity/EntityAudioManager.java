package net.hollowbit.archipeloserver.entity;

public class EntityAudioManager {
	
	private String continuousSound;
	private Entity entity;
	
	public EntityAudioManager(Entity entity) {
		continuousSound = "";
		this.entity = entity;
	}
	
	public void setContinuousSound (String sound) {
		this.continuousSound = sound;
	}
	
	public void applyToInterpSnapshot (EntitySnapshot snapshot) {
		snapshot.sound = continuousSound;
	}
	
	public void playSound (String sound) {
		if (entity.getEntityType().hasSound(sound))
			entity.changes.addSound(sound);
	}
	
}
