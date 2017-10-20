package com.github.liebharc.rsettings;

public abstract class Setting<T> {
	private T value;
	
	private T defaultValue;
	
	public Setting(T value, T defaultValue) {
		this.value = value;
		this.defaultValue = defaultValue;
	}
	
	public T getValue() {
		return value;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
}
