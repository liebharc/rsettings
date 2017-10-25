package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.Reject;
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
 * 
 * Note: This class isn't designed for inheritance. If additional functionality needs to be added 
 * then consider to use composition over inheritance. Meaning create a new class and have it reference
 * this class.
 */
public final class State {
	private static class VersionedValue {
		private long stateVersion;
		private Object value;

		public VersionedValue(long stateVersion, Object value) {
			this.stateVersion = stateVersion;
			this.value = value;
		}

		public long getVersion() {
			return stateVersion;
		}

		public Object getValue() {
			return value;
		}
	}
	
	public final static class Builder {
		
		private final State parent;
		
		private final List<ReadSetting<?>> settings;
		
	    private final Map<ReadSetting<?>, VersionedValue> prevState;
	    
	    private final long version;
	    
	    private Map<ReadSetting<?>, Object> newState = new HashMap<>();

		public Builder(
				State parent,
				List<ReadSetting<?>> settings, 
				Map<ReadSetting<?>, VersionedValue> state) {
			this.parent = parent;
			this.prevState =  state;
			this.settings = settings;
			this.version = parent.version + 1;
		}

		public 
			<TValue, 
			TSetting extends ReadSetting<TValue> & WriteableSetting<TValue>> 
				Builder set(TSetting setting, TValue value) {
			Reject.ifNull(setting);
			if (!settings.contains(setting)) {
				throw new IllegalArgumentException("Setting is not part of this state");
			}
			
			setUnchecked(setting, value);
			
			return this;
		}

		public 
			<TValue,
			 TConvertible extends CanConvertTo<TValue>,
			TSetting extends ReadSetting<TValue> & WriteableSetting<TValue>> 
				Builder set(TSetting setting, TConvertible value) {
			Reject.ifNull(value);			
			return set(setting, value.convertTo(get(setting)));
		}
		
		@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
		private <T> T get(ReadSetting<T> setting) {
			if (newState.containsKey(setting)) {
				return (T)newState.get(setting);
			}
			
			return (T)prevState.get(setting).getValue();
		}

		public void setUnchecked(ReadSetting<?> setting, Object value) {
			if (newState.containsKey(setting)) {
				newState.replace(setting, value);
			} else {
				newState.put(setting, value);
			}
		}
		
		public State build() throws CheckFailedException {
			Map<ReadSetting<?>, VersionedValue> combinedState = createCombinedState();
			List<ReadSetting<?>> allChanges =
					propagateChanges(
						combinedState, 
						this.newState.keySet().stream().collect(Collectors.toList()));
			return new State(parent, makeImmutable(combinedState), allChanges);
		}

		private Map<ReadSetting<?>, VersionedValue> createCombinedState() {
			Map<ReadSetting<?>, VersionedValue> combinedState = new HashMap<>();
			combinedState.putAll(prevState);
			for (Entry<ReadSetting<?>, ?> settingValue : newState.entrySet()) {
				combinedState.replace(
						settingValue.getKey(), 
						new VersionedValue(version, settingValue.getValue()));
			}
			return combinedState;
		}
		
		private List<ReadSetting<?>> propagateChanges(Map<ReadSetting<?>, VersionedValue> state, List<ReadSetting<?>> settings) throws CheckFailedException {
			SettingsChangeListBuilder allChanges = new SettingsChangeListBuilder(settings);
			List<ReadSetting<?>> settingsToResolve = new ArrayList<>(settings);
			while (!settingsToResolve.isEmpty()) {
				List<ReadSetting<?>> settingDependencies = new ArrayList<>();
				for (ReadSetting<?> setting : settingsToResolve) {
					Optional<?> result = setting.update(new State(this.parent, state, allChanges.build()));
					if ((result.isPresent())) {
						state.replace(
								setting, 
								new VersionedValue(version, result.get()));					
						allChanges.add(setting);
					}
					
					if (!ObjectHelper.NullSafeEquals(state.get(setting), prevState.get(setting))) {
						settingDependencies.addAll(parent.dependencies.getDependencies(setting));
					}
				}
				
				settingsToResolve = settingDependencies;
			}
			
			return allChanges.build();
		}
		
		private Map<ReadSetting<?>, VersionedValue> makeImmutable(Map<ReadSetting<?>, VersionedValue> state) {
			return Collections.unmodifiableMap(state);
		}
	}
	
	private static Map<ReadSetting<?>, VersionedValue> createResetValues(List<ReadSetting<?>> settings) {
		ImmutableMap.Builder<ReadSetting<?>, VersionedValue> initState = new ImmutableMap.Builder<ReadSetting<?>, VersionedValue>();
		for (ReadSetting<?> setting : settings) {
			initState.put(setting, new VersionedValue(0, setting.getDefaultValue()));
		}
		
		return initState.build();
	}
	
	private final List<ReadSetting<?>> settings;
	
    private final Map<ReadSetting<?>, VersionedValue> state;
    
    private final SettingDependencies dependencies;
	
	private final List<ReadSetting<?>> lastChanges;
	
	private final UUID kind;
    
    private final long version;

	private State(List<ReadSetting<?>> settings) {
		this(settings, createResetValues(settings));
	}
	
	public State(ReadSetting<?>... settings) {
		this(Arrays.asList(settings));
	}
	
	public State(Collection<ReadSetting<?>> settings) {
		this(Collections.unmodifiableList(new ArrayList<>(settings)));
	}
	
	private State(
			List<ReadSetting<?>> settings, 
			Map<ReadSetting<?>, VersionedValue> state) {
		this.settings = settings;
		this.state = state;
		this.kind = UUID.randomUUID();
		this.dependencies = new SettingDependencies();
		this.version = 0;
		this.lastChanges = settings;
		for (ReadSetting<?> setting : settings) {
			dependencies.register(setting);
		}
	}
	
	State(
			State parent, 
			Map<ReadSetting<?>, VersionedValue> state,
			List<ReadSetting<?>> lastChanges) {
		this.settings = parent.settings;
		this.state = state;
		this.kind = parent.kind;
		this.version = parent.version + 1;
		this.dependencies = parent.dependencies;
		this.lastChanges = lastChanges;
	}
	
	public Builder change() {
		return new Builder(this, settings, state);
	}
	
	public List<ReadSetting<?>> getLastChanges() {
		return lastChanges;
	}

	public boolean isRoot() {
		return version == 0;
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadSetting<T> setting) {
		Reject.ifNull(setting);
		return (T)state.get(setting).getValue();
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadSetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMin(TSetting setting) {
		Reject.ifNull(setting);
		return setting.getMin(this);
	}
	
	public <TValue extends Comparable<TValue>, 
			TSetting extends ReadSetting<TValue> & MinMaxLimited<TValue>> 
				TValue getMax(TSetting setting) {
		Reject.ifNull(setting);
		return setting.getMax(this);
	}
	
	public <TValue, 
			TSetting extends ReadSetting<TValue> & CanBeDisabled<TValue>> 
				boolean isEnabled(TSetting setting) {
		Reject.ifNull(setting);
		
		return setting.isEnabled(this);
	}

	public Collection<ReadSetting<?>> listSettings() {
		return settings;
	}
	
	/**
	 * Merges two settings. In case of a conflict the values from this instance are used.
	 * @param other Another @see SettingState, must be derived from the same base @see SettingState.
	 * @return
	 * @throws CheckFailedException if the resulting state after the merge isn't valid.
	 */
	public State merge(State other) throws CheckFailedException {
		Reject.ifNull(other);
				
		if (!other.kind.equals(this.kind) ) {
			throw new CheckFailedException("Can't merge two states which don't have a common ancestor");
		}
		
		Builder builder = change();
		for (Entry<ReadSetting<?>, VersionedValue> thisSettingValue : state.entrySet()) {
			VersionedValue otherValue = other.state.get(thisSettingValue.getKey());
			VersionedValue thisValue = thisSettingValue.getValue();
			if (otherValue.getVersion() > thisValue.getVersion()) {
				builder.setUnchecked(thisSettingValue.getKey(), otherValue.getValue());
			}
		}
		
		return builder.build();
	}
}
