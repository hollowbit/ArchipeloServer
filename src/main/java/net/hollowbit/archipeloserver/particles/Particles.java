package net.hollowbit.archipeloserver.particles;

import java.util.Random;

import net.hollowbit.archipeloshared.CollisionRect;
import net.hollowbit.archipeloshared.ParticlesData;

public abstract class Particles {
	
	private static Random random = new Random();
	
	private ParticlesData data;
	
	public Particles(int type, int amount, CollisionRect spawnRegion, String meta) {
		data = new ParticlesData();
		data.type = type;
		data.x = new int[amount];
		data.y = new int[amount];
		data.w = new int[amount];
		
		//Pick random spawn locations within the spawnRegion for each particle
		for (int i = 0; i < amount; i++) {
			data.x[i] = (int) (random.nextInt((int) spawnRegion.width) + spawnRegion.xWithOffset());
			data.y[i] = (int) (random.nextInt((int) spawnRegion.height) + spawnRegion.yWithOffset());
			data.w[i] = random.nextInt();
		}
		data.meta = meta;
	}

	public ParticlesData getData() {
		return data;
	}
	
}
