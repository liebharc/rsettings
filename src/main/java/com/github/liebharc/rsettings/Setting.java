package com.github.liebharc.rsettings;

public abstract class Setting<T> {
	private T defaultValue;
	
	public Setting(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
}
