package net.hollowbit.archipeloserver.items;

import java.io.IOException;

import net.hollowbit.archipeloshared.ItemUseAnimationData;

public class ItemUseAnimation {
	
	private boolean thrust;
	private boolean stick;
	private float[] timings;
	private float totalRuntime;
	
	public ItemUseAnimation(ItemType item, ItemUseAnimationData data) throws IllegalItemUseAnimationDataException {
		this.stick = data.stick;
		this.thrust = data.thrust;
		if (data.timings == null && data.runtime > 0)
			createRuntimeAnimation(item, data);
		else if (data.runtime <= 0 && data.timings != null)
			createFrameByFrameAnimation(item, data);
		else
			throw new IllegalItemUseAnimationDataException(item.id);
	}
	
	private void createRuntimeAnimation(ItemType item, ItemUseAnimationData data) {
		this.totalRuntime = data.runtime;
	}
	
	private void createFrameByFrameAnimation(ItemType item, ItemUseAnimationData data) {
		this.totalRuntime = 0;
		for (float timing : data.timings)
			totalRuntime += timing;
		this.timings = data.timings;
	}
	
	/**
	 * Returns whether the animation sticks on the last frame until the attack button is released.
	 * @return
	 */
	public boolean doesStick() {
		return stick;
	}
	
	public boolean usesThrust() {
		return thrust;
	}
	
	public boolean isRuntime() {
		return timings == null;
	}
	
	public boolean isFrameByFrame() {
		return timings != null;
	}
	
	public float getTotalRuntime() {
		return totalRuntime;
	}
	
	public class IllegalItemUseAnimationDataException extends IOException {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IllegalItemUseAnimationDataException(String itemId) {
			super("Invalid item use animation data found for " + itemId + ". Must either use runtime or timings.");
		}
		
	}
	
}

