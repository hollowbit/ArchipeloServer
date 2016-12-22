package net.hollowbit.archipeloserver.tools.event;

public abstract class CancelableEvent extends Event {
	
	private boolean canceled = false;
	
	public CancelableEvent(EventType type) {
		super(type);
	}

	public void cancel () {
		this.canceled = true;
	}
	
	@Override
	public boolean wasCanceled () {
		return canceled;
	}
	
}
