package com.github.liebharc.rsettings;

// TODO Create a read-only and read-write version of thIs class
public abstract class Setting<T> {
	private T defaultValue;
	
	public Setting(T defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
}
