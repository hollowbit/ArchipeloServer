package net.hollowbit.archipeloserver.entity.living.movementanimation;

import java.util.LinkedList;

/**
 * Class to manager MovementAnimations for a specific LivingEntity.
 * It will automatically remove expired animations.
 * @author vedi0boy
 *
 */
public class MovementAnimationManager {
	
	private LinkedList<MovementAnimation> animations;
	
	public MovementAnimationManager() {
		this.animations = new LinkedList<MovementAnimation>();
	}
	
	public synchronized void tick60(float deltaTime) {
		LinkedList<MovementAnimation> animationsToRemove = new LinkedList<MovementAnimation>();
		for (MovementAnimation animation : animations) {
			animation.tick60(deltaTime);
			
			if (animation.isExpired())
				animationsToRemove.add(animation);
		}
		animations.removeAll(animationsToRemove);
	}
	
	public synchronized void addAnimation(MovementAnimation animation) {
		animations.add(animation);
	}
	
}
