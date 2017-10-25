package com.github.liebharc.rsettings.immutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.Reject;
import java.util.*;

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
			allChanges.add(setting);
			
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
		}
		
		public State build() throws CheckFailedException {
			List<ReadSetting<?>> allChanges =
					propagateChanges(
						newState);
			return new State(parent, newState.build(), allChanges);
		}
		
		private List<ReadSetting<?>> propagateChanges(Values.Builder values) throws CheckFailedException {
			List<ReadSetting<?>> settingsToResolve = new ArrayList<>(settings);
			while (!settingsToResolve.isEmpty()) {
				List<ReadSetting<?>> settingDependencies = new ArrayList<>();
				for (ReadSetting<?> setting : settingsToResolve) {
					Optional<?> result = setting.update(new State(this.parent, values.build(), allChanges.build()));
					if ((result.isPresent())) {
						values.replace(
								setting, 
								result.get(),
								version);		
					}
					
					if (!ObjectHelper.NullSafeEquals(values.get(setting).getValue(), prevValues.get(setting).getValue())) {
						settingDependencies.addAll(parent.dependencies.getDependencies(setting));			
						allChanges.add(setting);
					}
				}
				
				settingsToResolve = settingDependencies;
			}
			
			return allChanges.build();
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
			Values values) {
		this.settings = settings;
		this.values = values;
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
	
	public List<ReadSetting<?>> getLastChanges() {
		return lastChanges;
	}

	public boolean isRoot() {
		return version == 0;
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
	
	public boolean hasChanged(ReadSetting<?> setting) {
		return lastChanges.stream().anyMatch(s -> s.getStorageToken() == setting.getStorageToken());
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
}
