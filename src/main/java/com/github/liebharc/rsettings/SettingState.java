package com.github.liebharc.rsettings;

import com.google.common.collect.*;
import java.util.*;
import java.util.Map.Entry;

public class SettingState {
	public class Builder {
		
		private final SettingState parent;
		
		private final List<ReadOnlySetting<?>> settings;
		
	    private final Map<ReadOnlySetting<?>, ?> prevState;
	    
	    private Map<ReadOnlySetting<?>, Object> newState = new HashMap<>();

		public Builder(
				SettingState parent,
				List<ReadOnlySetting<?>> settings, 
				Map<ReadOnlySetting<?>, ?> state) {
			this.parent = parent;
			this.prevState =  state;
			this.settings = settings;
		}

		public <T> Builder set(Setting<T> setting, T value) {
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
	
	public static SettingState FromSettings(ReadOnlySetting<?>... settings) {
		ImmutableList.Builder<ReadOnlySetting<?>> immutable = new ImmutableList.Builder<>();
		// TODO find out if there is an easy way to use addAll
		for (ReadOnlySetting<?> setting : settings) {
			immutable.add(setting);
		}
		return new SettingState(immutable.build());
	}
	
	private static Map<ReadOnlySetting<?>, ?> createResetValues(ImmutableList<ReadOnlySetting<?>> settings) {
		ImmutableMap.Builder<ReadOnlySetting<?>, Object> initState = new ImmutableMap.Builder<ReadOnlySetting<?>, Object>();
		for (ReadOnlySetting<?> setting : settings) {
			initState.put(setting, setting.getDefaultValue());
		}
		
		return initState.build();
	}
	
	private final List<ReadOnlySetting<?>> settings;
	
    private final Map<ReadOnlySetting<?>, ?> state;
    
    private final PropertyDependencies dependencies;

	public SettingState() {
		this(
			new ImmutableList.Builder<ReadOnlySetting<?>>().build(),
		    new ImmutableMap.Builder<ReadOnlySetting<?>, Object>().build());
	}
	
	public SettingState(ImmutableList<ReadOnlySetting<?>> settings) {
		this(settings, createResetValues(settings));
	}
	
	private SettingState(
			List<ReadOnlySetting<?>> settings, 
			Map<ReadOnlySetting<?>, ?> state) {
		this.settings = settings;
		this.state = state;
		this.dependencies = new PropertyDependencies();
		for (ReadOnlySetting<?> setting : settings) {
			dependencies.register(setting);
		}
	}
	
	SettingState(
			SettingState parent, 
			Map<ReadOnlySetting<?>, ?> state) {
		this.settings = parent.settings;
		this.state = state;
		this.dependencies = parent.dependencies;
	}
	
	public Builder change() {
		return new Builder(this, settings, state);
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadOnlySetting<T> value) {
		return (T)state.get(value);
	}
}
