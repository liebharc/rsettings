package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

/**
 * Manages the current version of the state. Makes sure that changes to the state a thread safe.
 */
class CurrentSettingState {
	private EventPublisher<State> stateChanged = new EventPublisher<>();
	
	private final Object lock = new Object();
	
	private State current;
	
	public CurrentSettingState(State init) {
		current = init;
	}
	
	public void set(State state) throws CheckFailedException {
		synchronized (lock) {		
			if (state.isRoot() && current.isRoot()) {
				current = state;
			}
			else {
				current = state.merge(current);
			}
		}
		
		stateChanged.raise(state);
	}
	
	public State get() {
		synchronized (lock) {
			return current;
		}
	}
	
	public Event<State> getStateChangedEvent() {
		return stateChanged.getEvent();
	}
}
