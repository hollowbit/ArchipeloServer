package net.hollowbit.archipeloserver.particles.types;

import net.hollowbit.archipeloserver.entity.Entity;
import net.hollowbit.archipeloserver.particles.ParticleType;
import net.hollowbit.archipeloserver.particles.Particles;
import net.hollowbit.archipeloserver.tools.StaticTools;
import net.hollowbit.archipeloshared.Direction;

public class EntityChunkParticles extends Particles {

	public EntityChunkParticles(Entity entity, Direction direction) {
		super(ParticleType.ENTITY_CHUNK, StaticTools.getRandom().nextInt(4) + 2, entity.getViewRect(), entity.getEntityType().getId() + ";" + entity.getStyle() + ";" + direction.ordinal());
	}

}
