package com.github.liebharc.rsettings.immutable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;

/**
 * A setting which only can be read by the user but not changed directly. The value of a read-only
 * setting might still change during the update phase dependent on the value of other settings.
 * @param <T> The type of a setting.
 */
public abstract class ReadSetting<T> {
	private T defaultValue;
	
	private List<ReadSetting<?>> dependencies;
	
	/**
	 * Creates a new setting.
	 * @param defaultValue The default value of the setting. The default values of all settings 
	 * in a state should give a consistent state.
	 * @param dependencies The dependencies of this setting. If any of the settings in this list
	 * is changed then the @see update(SettingState state) routine will be called.
	 */
	public ReadSetting(T defaultValue, ReadSetting<?>... dependencies) {
		this.defaultValue = defaultValue;
		this.dependencies = Arrays.asList(dependencies); 
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
	protected Optional<T> update(SettingState state) throws CheckFailedException {
		return Optional.empty();
	}
	
	List<ReadSetting<?>> getDependencies() {
		return this.dependencies;
	}
}
