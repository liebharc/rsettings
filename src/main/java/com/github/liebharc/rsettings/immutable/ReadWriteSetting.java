package com.github.liebharc.rsettings.immutable;

public abstract class ReadWriteSetting<T> 
	extends ReadSetting<T> 
	implements WriteableSetting {
		
	public ReadWriteSetting(T defaultValue) {
		super(defaultValue);
	}
}
