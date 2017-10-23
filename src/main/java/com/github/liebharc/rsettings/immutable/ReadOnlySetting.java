package com.github.liebharc.rsettings.immutable;

import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;

public abstract class ReadOnlySetting<T> {
	private T defaultValue;
	
	public ReadOnlySetting(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public final T getDefaultValue() {
		return defaultValue;
	}
	
	public Optional<T> update(SettingState state) throws CheckFailedException {
		return Optional.empty();
	}
}
