package com.github.liebharc.rsettings;

import com.google.common.collect.*;
import java.util.*;

public class SettingState {
	
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
	
	public SettingStateBuilder change() {
		return new SettingStateBuilder(this, settings, state);
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadOnlySetting<T> value) {
		return (T)state.get(value);
	}
}
