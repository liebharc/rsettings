package com.github.liebharc.rsettings;

import com.google.common.collect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
			Map<ReadOnlySetting<?>, Object> combinedState = createCombinedState();
			propagateChanges(
					combinedState, 
					this.newState.keySet().stream().collect(Collectors.toList()));
			return new SettingState(parent, makeImmutable(combinedState));
		}

		private Map<ReadOnlySetting<?>, Object> createCombinedState() {
			Map<ReadOnlySetting<?>, Object> combinedState = new HashMap<>();
			combinedState.putAll(prevState);
			for (Entry<ReadOnlySetting<?>, ?> settingValue : newState.entrySet()) {
				combinedState.replace(settingValue.getKey(), settingValue.getValue());
			}
			return combinedState;
		}
		
		private void propagateChanges(Map<ReadOnlySetting<?>, Object> state, List<ReadOnlySetting<?>> settings) throws CheckFailedException {
			if (settings.isEmpty()) {
				return;
			}
			
			List<ReadOnlySetting<?>> settingDependencies = new ArrayList<>();
			for (ReadOnlySetting<?> setting : settings) {
				Optional<?> result = setting.update(new SettingState(this.parent, state));
				if ((result.isPresent())) {
					state.replace(setting, result.get());
				}
				
				if (!ObjectHelper.NullSafeEquals(state.get(setting), prevState.get(setting))) {
					settingDependencies.addAll(dependencies.getDependencies(setting));
				}
			}
			
			propagateChanges(state, settingDependencies);
		}
		
		private Map<ReadOnlySetting<?>, Object> makeImmutable(Map<ReadOnlySetting<?>, Object> state) {
			ImmutableMap.Builder<ReadOnlySetting<?>, Object> immutable = new ImmutableMap.Builder<ReadOnlySetting<?>, Object>();
			immutable.putAll(state);
			return immutable.build();
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
	public <T> T get(ReadOnlySetting<T> setting) {
		return (T)state.get(setting);
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadOnlySetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMin(TSetting setting) {
		return setting.getMin(this);
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadOnlySetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMax(TSetting setting) {
		return setting.getMax(this);
	}
	
	public <TValue, 
			TSetting extends ReadOnlySetting<TValue> & CanBeDisabled> 
				boolean isEnabled(TSetting setting) {
		return setting.isEnabled(this);
	}
}
