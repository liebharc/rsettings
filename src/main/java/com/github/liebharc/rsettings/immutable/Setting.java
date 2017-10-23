package com.github.liebharc.rsettings.immutable;

public abstract class Setting<T> 
	extends ReadOnlySetting<T> 
	implements WriteableSetting {
		
	public Setting(T defaultValue) {
		super(defaultValue);
	}
}
