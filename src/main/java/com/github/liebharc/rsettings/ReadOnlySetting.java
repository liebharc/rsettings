package com.github.liebharc.rsettings;

import java.util.Optional;

public abstract class ReadOnlySetting<T> {
	private T defaultValue;
	
	public ReadOnlySetting(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public final T getDefaultValue() {
		return defaultValue;
	}
	
	public Optional<T> update() throws CheckFailedException {
		return Optional.empty();
	}
}
