package net.hollowbit.archipeloserver.particles.types;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.particles.ParticleType;
import net.hollowbit.archipeloserver.particles.Particles;
import net.hollowbit.archipeloshared.CollisionRect;

public class HealthParticles extends Particles {

	public HealthParticles(Entity entity, int health) {
		super(ParticleType.HEALTH, 1, new CollisionRect(entity.getX() + entity.getEntityType().getViewWidth() / 4, entity.getTopOfHead() + 3, entity.getEntityType().getViewWidth() / 2, 5), "" + health);
	}

}
