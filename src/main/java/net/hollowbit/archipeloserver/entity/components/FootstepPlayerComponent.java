package net.hollowbit.archipeloserver.entity.components;

import java.util.HashSet;

import com.badlogic.gdx.math.Vector2;

import net.hollowbit.archipeloserver.entity.EntityComponent;
import net.hollowbit.archipeloserver.entity.LivingEntity;
import net.hollowbit.archipeloserver.tools.audio.SoundCalculator;
import net.hollowbit.archipeloshared.RollableEntity;
import net.hollowbit.archipeloshared.TileSoundType;

public class FootstepPlayerComponent extends EntityComponent {
	
	protected RollableEntity rollableEntity;
	protected LivingEntity livingEntity;
	protected boolean canRoll = false;
	protected HashSet<String> possibleSoundTypes;
	
	public FootstepPlayerComponent(LivingEntity entity, boolean canRoll, TileSoundType... possibleSoundTypes) {
		super(entity);
		this.livingEntity = entity;
		
		if (entity instanceof RollableEntity && canRoll) {
			rollableEntity = (RollableEntity) entity;
			this.canRoll = true;
		}
		
		this.possibleSoundTypes = new HashSet<String>();
		for (TileSoundType type : possibleSoundTypes)
			this.possibleSoundTypes.add(type.getId());
	}
	
	@Override
	public void tick20(float deltaTime) {
		super.tick20(deltaTime);
		Vector2 tilePos = entity.getFeetTile();
		
		String tileSound = entity.getMap().getTileTypeAtLocation((int) tilePos.x, (int) tilePos.y).getFootstepSound();
		if (!possibleSoundTypes.contains(tileSound))
			tileSound = "default";
		
		if (livingEntity.isMoving()) {
			if (canRoll && rollableEntity.isRolling())
				entity.getAudioManager().setFootstepSound(entity.getEntityType().getFootstepSound() + "/" + tileSound + "-roll", 1);
			else
				entity.getAudioManager().setFootstepSound(entity.getEntityType().getFootstepSound() + "/" + tileSound + "-walk", SoundCalculator.calculatePitch(entity.getEntityType().getSpeed(), livingEntity.getSpeed()));
		} else
			entity.getAudioManager().stopFootstepSound();
	}

}
