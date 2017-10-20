package com.github.liebharc.rsettings;

import com.google.common.collect.ImmutableList;

public class SettingState {
	
	ImmutableList<Setting<?>> settings;
	
	ImmutableList<?> state;

	public SettingState() {
		settings = new ImmutableList.Builder<Setting<?>>().build();
		state = new ImmutableList.Builder<Object>().build();
	}
	
	public <T> void change(Setting<T> setting, T value) {
		
	}
}
