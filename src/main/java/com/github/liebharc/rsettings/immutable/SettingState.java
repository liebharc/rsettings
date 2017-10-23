package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.CheckFailedException;
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

		public 
			<TValue, 
			TSetting extends ReadOnlySetting<TValue> & WriteableSetting> 
				Builder set(TSetting setting, TValue value) {
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
			List<ReadOnlySetting<?>> allChanges =
					propagateChanges(
						combinedState, 
						this.newState.keySet().stream().collect(Collectors.toList()));
			return new SettingState(parent, makeImmutable(combinedState), allChanges);
		}

		private Map<ReadOnlySetting<?>, Object> createCombinedState() {
			Map<ReadOnlySetting<?>, Object> combinedState = new HashMap<>();
			combinedState.putAll(prevState);
			for (Entry<ReadOnlySetting<?>, ?> settingValue : newState.entrySet()) {
				combinedState.replace(settingValue.getKey(), settingValue.getValue());
			}
			return combinedState;
		}
		
		private List<ReadOnlySetting<?>> propagateChanges(Map<ReadOnlySetting<?>, Object> state, List<ReadOnlySetting<?>> settings) throws CheckFailedException {
			SettingsChangeListBuilder allChanges = new SettingsChangeListBuilder(settings);
			List<ReadOnlySetting<?>> settingsToResolve = new ArrayList<>(settings);
			while (!settingsToResolve.isEmpty()) {
				List<ReadOnlySetting<?>> settingDependencies = new ArrayList<>();
				for (ReadOnlySetting<?> setting : settingsToResolve) {
					Optional<?> result = setting.update(new SettingState(this.parent, state, allChanges.build()));
					if ((result.isPresent())) {
						state.replace(setting, result.get());					
						allChanges.add(setting);
					}
					
					if (!ObjectHelper.NullSafeEquals(state.get(setting), prevState.get(setting))) {
						settingDependencies.addAll(dependencies.getDependencies(setting));
					}
				}
				
				settingsToResolve = settingDependencies;
			}
			
			return allChanges.build();
		}
		
		private Map<ReadOnlySetting<?>, Object> makeImmutable(Map<ReadOnlySetting<?>, Object> state) {
			ImmutableMap.Builder<ReadOnlySetting<?>, Object> immutable = new ImmutableMap.Builder<ReadOnlySetting<?>, Object>();
			immutable.putAll(state);
			return immutable.build();
		}
	}
	
	public static SettingState FromSettings(ReadOnlySetting<?>... settings) {
		return FromSettings(Arrays.asList(settings));
	}
	
	public static SettingState FromSettings(Collection<ReadOnlySetting<?>> settings) {
		ImmutableList.Builder<ReadOnlySetting<?>> immutable = new ImmutableList.Builder<>();
		immutable.addAll(settings);
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
	
	private final List<ReadOnlySetting<?>> lastChanges;
    
    private final UUID id;
    
    private final Optional<UUID> parentId;

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
		this.id = UUID.randomUUID();
		this.parentId = Optional.empty();
		this.dependencies = new PropertyDependencies();
		this.lastChanges = settings;
		for (ReadOnlySetting<?> setting : settings) {
			dependencies.register(setting);
		}
	}
	
	SettingState(
			SettingState parent, 
			Map<ReadOnlySetting<?>, ?> state,
			List<ReadOnlySetting<?>> lastChanges) {
		this.settings = parent.settings;
		this.state = state;
		this.id = UUID.randomUUID();
		this.parentId = Optional.of(parent.id);
		this.dependencies = parent.dependencies;
		this.lastChanges = lastChanges;
	}
	
	public Builder change() {
		return new Builder(this, settings, state);
	}
	
	public boolean isDirectlyDerivedFrom(SettingState possibleParent) {
		if (!parentId.isPresent()) {
			return false;
		}
		
		return possibleParent.id == parentId.get();
	}
	
	public List<ReadOnlySetting<?>> getLastChanges() {
		return lastChanges;
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

	public int getNumberOfSettings() {
		return settings.size();
	}

	public List<ReadOnlySetting<?>> listSettings() {
		return settings;
	}
}
