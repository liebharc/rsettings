package com.github.liebharc.rsettings.immutable;

import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;

/**
 * A setting which only can be read by the user but not changed directly. The value of a read-only
 * setting might still change during the update phase dependent on the value of other settings.
 * @param <T> The type of a setting.
 */
public abstract class ReadSetting<T> 
	implements Setting<T> {
	protected static Dependencies NoDependencies() {
		return Dependencies.empty();
	}
	
	protected static Dependencies Dependencies(ReadSetting<?>... dependencies) {
		return new Dependencies(dependencies);
	}
	
	private final SettingId id;
	
	private final T defaultValue;
		
	private final Dependencies dependencies;
	
	/**
	 * Creates a new setting.
	 * @param defaultValue The default value of the setting. The default values of all settings 
	 * in a state should give a consistent state.
	 * @param dependencies The dependencies of this setting. If any of the settings in this list
	 * is changed then the @see update(SettingState state) routine will be called. The dependencies
	 * must be set in the constructor because that should ensure that the dependency tree can be linearized.
	 */
	public ReadSetting(T defaultValue, Dependencies dependencies) {
		this.id = new SettingId();
		this.defaultValue = defaultValue;
		this.dependencies = dependencies; 
	}
	
	public final T getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * The update routine is called every time the setting or one of the values
	 * it depends on is changed.
	 * @param state The current state. Every operation which is done in the update routine
	 * should take the values from the state argument and not from somewhere else.
	 * @return may return a value if the current value of the setting should be different.
	 * @throws CheckFailedException may be thrown by the implementation if the value is incorrect
	 * or not consistent with the other values in the state.
	 */
	protected Optional<T> update(State state) throws CheckFailedException {
		return Optional.empty();
	}
	
	Dependencies getDependencies() {
		return this.dependencies;
	}
	
	protected SettingId getId() {
		return id;
	}
	
	@Override
	public boolean shouldBeStored() {
		return false;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ReadSetting<?>))
			return false;
		ReadSetting<?> other = (ReadSetting<?>) obj;
		if (getId() != other.getId())
			return false;
		return true;
	}
}
