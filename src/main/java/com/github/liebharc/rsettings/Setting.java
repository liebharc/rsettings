package com.github.liebharc.rsettings;

public abstract class Setting<T> extends ReadOnlySetting<T> {
		
	public Setting(T defaultValue) {
		super(defaultValue);
	}
}
