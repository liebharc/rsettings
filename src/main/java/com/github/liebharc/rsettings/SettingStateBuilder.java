package com.github.liebharc.rsettings;

import com.google.common.collect.*;
import java.util.*;
import java.util.Map.Entry;

public class SettingStateBuilder  {
	
	private final SettingState parent;
	
	private final List<ReadOnlySetting<?>> settings;
	
    private final Map<ReadOnlySetting<?>, ?> prevState;
    
    private Map<ReadOnlySetting<?>, Object> newState = new HashMap<>();

	public SettingStateBuilder(
			SettingState parent,
			List<ReadOnlySetting<?>> settings, 
			Map<ReadOnlySetting<?>, ?> state) {
		this.parent = parent;
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
	
	public SettingState build() throws CheckFailedException {
		ImmutableMap.Builder<ReadOnlySetting<?>, Object> combinedState = new ImmutableMap.Builder<ReadOnlySetting<?>, Object>();

		combinedState.putAll(newState);
		for (Entry<ReadOnlySetting<?>, ?> settingValue : prevState.entrySet()) {
			if (!newState.containsKey(settingValue.getKey())) {
				combinedState.put(settingValue);
			}
		}
		
		return new SettingState(parent, combinedState.build());
	}
}
