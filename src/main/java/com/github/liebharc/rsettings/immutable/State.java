package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.Reject;
import java.util.*;
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
	
	public final static class Builder {
		
		private final State parent;
		
		private final List<ReadSetting<?>> settings;
		
	    private final Values prevValues;
	    
	    private final SettingsChangeListBuilder allChanges = new SettingsChangeListBuilder();
	    
	    private final long version;
	    
	    private final Values.Builder newState;

		public Builder(
				State parent,
				List<ReadSetting<?>> settings, 
				Values values) {
			this.parent = parent;
			this.prevValues =  values;
			this.newState = values.change();
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
				return (T)newState.get(setting).getValue();
			}
			
			return (T)prevValues.get(setting).getValue();
		}

		public void setUnchecked(ReadSetting<?> setting, Object value) {
			newState.update(setting, value, version);
			allChanges.add(setting);
		}
		
		public State build() throws CheckFailedException {
			List<ReadSetting<?>> allChanges =
					propagateChanges(
						newState);
			return new State(parent, newState.build(), allChanges);
		}
		
		private List<ReadSetting<?>> propagateChanges(Values.Builder values) throws CheckFailedException {
			List<ReadSetting<?>> settingsNextPass = new ArrayList<>(allChanges.build());
			settingsNextPass = updatePass(values, prevValues, settingsNextPass);
			
			while (!settingsNextPass.isEmpty()) {
				settingsNextPass = updatePass(values,  values.build(), settingsNextPass);
			}
			
			return allChanges.build();
		}

		private List<ReadSetting<?>> updatePass(
				Values.Builder values,
				Values reference,
				List<ReadSetting<?>> settings)
				throws CheckFailedException {
			List<ReadSetting<?>> settingDependencies = new ArrayList<>();
			for (ReadSetting<?> setting : settings) {
				Optional<?> result = setting.update(new State(this.parent, values.build(), allChanges.build()));
				Object previousValue = reference.get(setting).getValue();
				if ((result.isPresent())) {
					values.replace(
							setting, 
							result.get(),
							version);		
				}
				
				if (!ObjectHelper.NullSafeEquals(values.get(setting).getValue(), previousValue)) {
					settingDependencies.addAll(parent.dependencies.getDependencies(setting));			
					allChanges.add(setting);
				}
			}
			
			return settingDependencies;
		}
	}
	
	private static Values createResetValues(List<ReadSetting<?>> settings) {
		Values.Builder values = new Values().change();
		for (ReadSetting<?> setting : settings) {
			values.put(setting, setting.getDefaultValue(), 0);
		}
		
		return values.build();
	}
	
	private final List<ReadSetting<?>> settings;
	
    private final Values values;
    
    private final DependencyGraph dependencies;
	
	private final List<ReadSetting<?>> lastChanges;
	
	private final UUID kind;
    
    private final long version;
    
    private static List<ReadSetting<?>> removePlaceholders(Iterable<ReadSetting<?>> settings) {
    	List<ReadSetting<?>> list = new ArrayList<>();
    	settings.forEach((s) -> {
    		if (!Placeholder.isPlaceholder(s)) {
    			list.add(s);
    		}
    	});
    	
    	return Collections.unmodifiableList(list);
    }
	
	public State(Register settings) {
		this(settings.asList());
	}
	
	private State(List<ReadSetting<?>> settings) {
		this.settings = removePlaceholders(settings);
		this.values = createResetValues(this.settings);
		this.kind = UUID.randomUUID();
		this.dependencies = new DependencyGraph(settings);
		this.version = 0;
		this.lastChanges = settings;
	}
	
	State(
			State parent, 
			Values values,
			List<ReadSetting<?>> lastChanges) {
		this.settings = parent.settings;
		this.values = values;
		this.kind = parent.kind;
		this.version = parent.version + 1;
		this.dependencies = parent.dependencies;
		this.lastChanges = lastChanges;
	}
	
	public Builder change() {
		return new Builder(this, settings, values);
	}
	
	@SuppressWarnings("unchecked") // The type cast should always succeed even if the compile can't verify that
	public <T> T get(ReadSetting<T> setting) {
		Reject.ifNull(setting);
		return (T)values.get(setting).getValue();
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
	
	/**
	 * Returns all settings which have changed. See @see hasChanged(ReadSetting<?>) for more details.   
	 * @return All setting which have changed.
	 */
	public List<ReadSetting<?>> getChanges() {
		return lastChanges;
	}

	/**
	 * Returns all settings which have changed. See @see hasChanged(ReadSetting<?>, State) for more details.
	 * @param since A previous state.
	 * @return All setting which have changed.
	 */
	public List<ReadSetting<?>> getChanges(State since) {
		Reject.ifNull(since);
		
		return settings.stream()
				.filter(s -> hasChanged(s, since))
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns all settings which have been touched. See @see hasBeenTouched(ReadSetting<?>, State) for more details.
	 * @param since A previous state.
	 * @return All setting which have been touched.
	 */
	public List<ReadSetting<?>> getTouchedSettings(State since) {
		Reject.ifNull(since);
		
		return settings.stream()
				.filter(s -> hasBeenTouched(s, since))
				.collect(Collectors.toList());
	}

	/**
	 * Indicates whether or not a setting has changed during the call of @see change().
	 * @param setting A setting.
	 * @return True if the setting has changed.
	 */
	public boolean hasChanged(ReadSetting<?> setting) {
		return lastChanges.contains(setting);
	}

	/**
	 * Indicates whether or not a setting has changed compared to the state specified by since.
	 * @param setting A setting.
	 * @param since A previous state.
	 * @return True if the setting has changed.
	 */
	public boolean hasChanged(ReadSetting<?> setting, State since) {
		Reject.ifNull(since);
		Reject.ifNull(setting);
		
		requireStateToBeOlder(since);
		
		return !ObjectHelper.NullSafeEquals(this.get(setting), since.get(setting));
	}
	
	/**
	 * Indicates whether or not any of the settings has changed during the call of @see change().
	 * @param setting A list of settings.
	 * @return True if any setting has changed.
	 */
	public boolean hasAnyChanged(List<ReadSetting<?>> list) {
		return list.stream().anyMatch(s -> hasChanged(s));
	}

	/**
	 * Indicates whether or not a setting has been touched compared to the state specified by since.
	 * In contrast to @see hasBeenChanged(ReadSetting<?>, State) this method also returns true
	 * if a value has been changed back to its original value. 
	 * @param setting A setting.
	 * @param since A previous state.
	 * @return True if the setting has been touched.
	 */
	public boolean hasBeenTouched(ReadSetting<?> setting, State since) {
		Reject.ifNull(since);
		Reject.ifNull(setting);
		requireStateToBeOlder(since);
		
		return this.values.get(setting).getVersion() > since.values.get(setting).getVersion();
	}

	private void requireStateToBeOlder(State since) {
		if (!since.kind.equals(this.kind) )
			throw new IllegalArgumentException("Can't merge two states which don't have a common ancestor");
		
		if (since.version > this.version)
			throw new IllegalArgumentException("since argument must be at least as old as this state");
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
		for (ReadSetting<?> setting : settings) {
			VersionedValue otherValue = other.values.get(setting);
			VersionedValue thisValue = this.values.get(setting);
			if (otherValue.getVersion() > thisValue.getVersion()) {
				builder.setUnchecked(setting, otherValue.getValue());
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Returns a set of settings and values which when stored and restored will recreate the same state.
	 * 
	 * @return Settings and values
	 */
	public Set<Map.Entry<ReadSetting<?>, Object>> getPersistenceValues() {
		return settings.stream()
			.filter((s) -> s.shouldBeStored())
			.map(s -> new AbstractMap.SimpleEntry<ReadSetting<?>, Object>(s, values.get(s).getValue()))
			.collect(Collectors.toSet());
	}
}
