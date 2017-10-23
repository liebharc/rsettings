package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.*;

class CurrentSettingState {

	private final Object lock = new Object();
	
	private SettingState current;
	
	public CurrentSettingState(SettingState init) {
		current = init;
	}
	
	public void set(SettingState state) {
		synchronized (lock) {
			current = state;
		}
	}
	
	public SettingState get() {
		synchronized (lock) {
			return current;
		}
	}
}
