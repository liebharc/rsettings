package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

/**
 * Manages the current version of the state. Makes sure that changes to the state a thread safe.
 */
class CurrentSettingState {
	private EventPublisher<SettingState> stateChanged = new EventPublisher<>();
	
	private final Object lock = new Object();
	
	private SettingState current;
	
	public CurrentSettingState(SettingState init) {
		current = init;
	}
	
	public void set(SettingState state) throws ConflictingUpdatesException {
		synchronized (lock) {
			if (!state.isDirectlyDerivedFrom(current) && !state.isRoot()) {
				throw new ConflictingUpdatesException();
			}
			
			current = state;
		}
		stateChanged.raise(state);
	}
	
	public SettingState get() {
		synchronized (lock) {
			return current;
		}
	}
	
	public Event<SettingState> getStateChangedEvent() {
		return stateChanged.getEvent();
	}
}
