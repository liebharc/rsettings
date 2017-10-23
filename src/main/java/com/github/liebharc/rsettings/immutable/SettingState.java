package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.google.common.collect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The state remembers the current values of all settings.
 * 
 * An immutable state solves all issues which are more commonly solved with transactions, e.g.:
 * - Changes to an immutable state can be rolled back just by discarding/not-using a state
 * - Changes to several settings at the same time can be implemented with a @see Builder. All changes
 *   in a builder are considered to happen at the same time.
 * - A states consistency can be checked every time the @see Builder.build() routine is called.
 */
public class SettingState {
	public class Builder {
		
		private final SettingState parent;
		
		private final List<ReadSetting<?>> settings;
		
	    private final Map<ReadSetting<?>, ?> prevState;
	    
	    private Map<ReadSetting<?>, Object> newState = new HashMap<>();

		public Builder(
				SettingState parent,
				List<ReadSetting<?>> settings, 
				Map<ReadSetting<?>, ?> state) {
			this.parent = parent;
			this.prevState =  state;
			this.settings = settings;
		}

		public 
			<TValue, 
			TSetting extends ReadSetting<TValue> & WriteableSetting> 
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
			Map<ReadSetting<?>, Object> combinedState = createCombinedState();
			List<ReadSetting<?>> allChanges =
					propagateChanges(
						combinedState, 
						this.newState.keySet().stream().collect(Collectors.toList()));
			return new SettingState(parent, makeImmutable(combinedState), allChanges);
		}

		private Map<ReadSetting<?>, Object> createCombinedState() {
			Map<ReadSetting<?>, Object> combinedState = new HashMap<>();
			combinedState.putAll(prevState);
			for (Entry<ReadSetting<?>, ?> settingValue : newState.entrySet()) {
				combinedState.replace(settingValue.getKey(), settingValue.getValue());
			}
			return combinedState;
		}
		
		private List<ReadSetting<?>> propagateChanges(Map<ReadSetting<?>, Object> state, List<ReadSetting<?>> settings) throws CheckFailedException {
			SettingsChangeListBuilder allChanges = new SettingsChangeListBuilder(settings);
			List<ReadSetting<?>> settingsToResolve = new ArrayList<>(settings);
			while (!settingsToResolve.isEmpty()) {
				List<ReadSetting<?>> settingDependencies = new ArrayList<>();
				for (ReadSetting<?> setting : settingsToResolve) {
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
		
		private Map<ReadSetting<?>, Object> makeImmutable(Map<ReadSetting<?>, Object> state) {
			ImmutableMap.Builder<ReadSetting<?>, Object> immutable = new ImmutableMap.Builder<ReadSetting<?>, Object>();
			immutable.putAll(state);
			return immutable.build();
		}
	}
	
	public static SettingState FromSettings(ReadSetting<?>... settings) {
		return FromSettings(Arrays.asList(settings));
	}
	
	public static SettingState FromSettings(Collection<ReadSetting<?>> settings) {
		ImmutableList.Builder<ReadSetting<?>> immutable = new ImmutableList.Builder<>();
		immutable.addAll(settings);
		return new SettingState(immutable.build());
	}
	
	private static Map<ReadSetting<?>, ?> createResetValues(ImmutableList<ReadSetting<?>> settings) {
		ImmutableMap.Builder<ReadSetting<?>, Object> initState = new ImmutableMap.Builder<ReadSetting<?>, Object>();
		for (ReadSetting<?> setting : settings) {
			initState.put(setting, setting.getDefaultValue());
		}
		
		return initState.build();
	}
	
	private final List<ReadSetting<?>> settings;
	
    private final Map<ReadSetting<?>, ?> state;
    
    private final SettingDependencies dependencies;
	
	private final List<ReadSetting<?>> lastChanges;
    
    private final UUID id;
    
    private final Optional<UUID> parentId;

	public SettingState() {
		this(
			new ImmutableList.Builder<ReadSetting<?>>().build(),
		    new ImmutableMap.Builder<ReadSetting<?>, Object>().build());
	}
	
	public SettingState(ImmutableList<ReadSetting<?>> settings) {
		this(settings, createResetValues(settings));
	}
	
	private SettingState(
			List<ReadSetting<?>> settings, 
			Map<ReadSetting<?>, ?> state) {
		this.settings = settings;
		this.state = state;
		this.id = UUID.randomUUID();
		this.parentId = Optional.empty();
		this.dependencies = new SettingDependencies();
		this.lastChanges = settings;
		for (ReadSetting<?> setting : settings) {
			dependencies.register(setting);
		}
	}
	
	SettingState(
			SettingState parent, 
			Map<ReadSetting<?>, ?> state,
			List<ReadSetting<?>> lastChanges) {
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
	
	public List<ReadSetting<?>> getLastChanges() {
		return lastChanges;
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadSetting<T> setting) {
		return (T)state.get(setting);
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadSetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMin(TSetting setting) {
		return setting.getMin(this);
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadSetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMax(TSetting setting) {
		return setting.getMax(this);
	}
	
	public <TValue, 
			TSetting extends ReadSetting<TValue> & CanBeDisabled> 
				boolean isEnabled(TSetting setting) {
		return setting.isEnabled(this);
	}

	public int getNumberOfSettings() {
		return settings.size();
	}

	public List<ReadSetting<?>> listSettings() {
		return settings;
	}
}
