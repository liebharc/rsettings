package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

/**
 * Manages the current version of the state. Makes sure that changes to the state a thread safe.
 */
class CurrentStateProvider implements StateProvider {
	private EventPublisher<State> stateChanged = new EventPublisher<>();
	
	private final Object lock = new Object();
	
	private State current;
	
	public CurrentStateProvider(State init) {
		current = init;
	}
	
	/* (non-Javadoc)
	 * @see com.github.liebharc.rsettings.mutable.StateProvider#set(com.github.liebharc.rsettings.immutable.State)
	 */
	@Override
	public void set(State state) throws CheckFailedException {
		synchronized (lock) {
			current = state.merge(current);
		}
		
		stateChanged.raise(state);
	}
	
	void reinitialize(State state) {
		current = state;
		stateChanged.raise(state);
	}
	
	/* (non-Javadoc)
	 * @see com.github.liebharc.rsettings.mutable.StateProvider#get()
	 */
	@Override
	public State get() {
		synchronized (lock) {
			return current;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.github.liebharc.rsettings.mutable.StateProvider#getStateChangedEvent()
	 */
	@Override
	public Event<State> getStateChangedEvent() {
		return stateChanged.getEvent();
	}
}
