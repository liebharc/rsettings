package com.github.liebharc.rsettings;

import com.google.common.collect.*;

public class SettingState {
	
	public static SettingState FromSettings(Setting<?>... settings) {
		ImmutableList.Builder<Setting<?>> immutable = new ImmutableList.Builder<>();
		// TODO find out if there is an easy way to use addAll
		for (Setting<?> setting : settings) {
			immutable.add(setting);
		}
		return new SettingState(immutable.build());
	}
	
	private final ImmutableList<Setting<?>> settings;
	
    private final ImmutableMap<Setting<?>, ?> state;

	public SettingState() {
		settings = new ImmutableList.Builder<Setting<?>>().build();
		state = new ImmutableMap.Builder<Setting<?>, Object>().build();
	}
	
	public SettingState(ImmutableList<Setting<?>> settings) {
		this.settings = settings;
		ImmutableMap.Builder<Setting<?>, Object> initState = new ImmutableMap.Builder<Setting<?>, Object>();
		for (Setting<?> setting : settings) {
			initState.put(setting, setting.getDefaultValue());
		}
		
		this.state = initState.build();
	}
	
	private SettingState(
			ImmutableList<Setting<?>> settings, 
			ImmutableMap<Setting<?>, ?> state) {
		this.settings = settings;
		this.state = state;
	}
	
	// TODO we are returning the base type here. Think if we can solve this with composition or 
	// by providing a specialization in sub classes
	public <T> SettingState change(Setting<T> setting, T value) {
		if (!settings.contains(setting)) {
			throw new IllegalArgumentException("Setting is not part of this state");
		}
		
		ImmutableMap.Builder<Setting<?>, Object> newState = new ImmutableMap.Builder<Setting<?>, Object>();

		// TODO Find a better way to replace an item
		for (Setting<?> s : state.keySet()) {
			// TODO double check if reference equality is what we want here
			if (s == setting) {
				newState.put(s, value);
			} else {
				newState.put(s, state.get(s));
			}
		}
		return new SettingState(this.settings, newState.build());
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(Setting<T> value) {
		return (T)state.get(value);
	}
}
