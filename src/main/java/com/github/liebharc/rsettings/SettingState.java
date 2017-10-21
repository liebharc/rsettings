package com.github.liebharc.rsettings;

import com.google.common.collect.*;

public class SettingState {
	
	public static SettingState FromSettings(ReadOnlySetting<?>... settings) {
		ImmutableList.Builder<ReadOnlySetting<?>> immutable = new ImmutableList.Builder<>();
		// TODO find out if there is an easy way to use addAll
		for (ReadOnlySetting<?> setting : settings) {
			immutable.add(setting);
		}
		return new SettingState(immutable.build());
	}
	
	private static ImmutableMap<ReadOnlySetting<?>, ?> createResetValues(ImmutableList<ReadOnlySetting<?>> settings) {
		ImmutableMap.Builder<ReadOnlySetting<?>, Object> initState = new ImmutableMap.Builder<ReadOnlySetting<?>, Object>();
		for (ReadOnlySetting<?> setting : settings) {
			initState.put(setting, setting.getDefaultValue());
		}
		
		return initState.build();
	}
	
	private final ImmutableList<ReadOnlySetting<?>> settings;
	
    private final ImmutableMap<ReadOnlySetting<?>, ?> state;
    
    private final PropertyDependencies dependencies;

	public SettingState() {
		this(
			new ImmutableList.Builder<ReadOnlySetting<?>>().build(),
		    new ImmutableMap.Builder<ReadOnlySetting<?>, Object>().build());
	}
	
	public SettingState(ImmutableList<ReadOnlySetting<?>> settings) {
		this(settings, createResetValues(settings));
	}
	
	SettingState(
			ImmutableList<ReadOnlySetting<?>> settings, 
			ImmutableMap<ReadOnlySetting<?>, ?> state) {
		this.settings = settings;
		this.state = state;
		this.dependencies = new PropertyDependencies();
		for (ReadOnlySetting<?> setting : settings) {
			dependencies.register(setting);
		}
	}
	
	public SettingStateBuilder change() {
		return new SettingStateBuilder(settings, state);
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadOnlySetting<T> value) {
		return (T)state.get(value);
	}
}
