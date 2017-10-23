package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

class CurrentSettingState {
	private EventPublisher<SettingState> stateChanged = new EventPublisher<>();
	
	private final Object lock = new Object();
	
	private SettingState current;
	
	public CurrentSettingState(SettingState init) {
		current = init;
	}
	
	public void set(SettingState state) {
		synchronized (lock) {
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
