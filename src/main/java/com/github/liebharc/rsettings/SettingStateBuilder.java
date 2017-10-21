package com.github.liebharc.rsettings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.Map.Entry;

public class SettingStateBuilder  {
	
	private final ImmutableList<Setting<?>> settings;
	
    private final ImmutableMap<Setting<?>, ?> prevState;
    
    private Map<Setting<?>, Object> newState = new HashMap<>();

	public SettingStateBuilder(
			ImmutableList<Setting<?>> settings, 
			ImmutableMap<Setting<?>, ?> state) {
		this.prevState =  state;
		this.settings = settings;
	}

	public <T> SettingStateBuilder set(Setting<T> setting, T value) {
		if (!settings.contains(setting)) {
			throw new IllegalArgumentException("Setting is not part of this state");
		}
		
		if (newState.containsKey(setting)) {
			newState.replace(setting, value);
		} else {
			newState.put(setting, value);
		}
		
		return this;
	}
	
	public SettingState build() {
		ImmutableMap.Builder<Setting<?>, Object> combinedState = new ImmutableMap.Builder<Setting<?>, Object>();

		combinedState.putAll(newState);
		for (Entry<Setting<?>, ?> settingValue : prevState.entrySet()) {
			if (!newState.containsKey(settingValue.getKey())) {
				combinedState.put(settingValue);
			}
		}
		
		return new SettingState(settings, combinedState.build());
	}
}
