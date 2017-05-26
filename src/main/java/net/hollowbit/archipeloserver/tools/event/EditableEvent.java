package net.hollowbit.archipeloserver.tools.event;

import java.io.Closeable;

import net.hollowbit.archipeloserver.ArchipeloServer;

public abstract class EditableEvent extends Event implements Closeable {
	
	protected boolean canceled = false;
	protected boolean editingPrevented = false;
	
	public EditableEvent(EventType type) {
		super(type);
	}
	
	/**
	 * Will canceled the event if possible.
	 * Returns true if the event was canceled, false otherwise.
	 */
	public boolean cancel () {
		if (!editingPrevented) {
			this.canceled = true;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean wasCanceled () {
		return canceled;
	}
	
	/**
	 * Prevent this event from being canceled and edited.
	 */
	public void preventEditing() {
		editingPrevented = true;
	}
	
	/**
	 * Trigger this event after the changes were applied, if the event wasn't canceled.
	 */
	public void close() {
		if (!canceled)
			ArchipeloServer.getServer().getEventManager().triggerEvent(this, false);
	}
	
	@Override
	public boolean editable() {
		return !editingPrevented;
	}
	
}
