package com.github.liebharc.rsettings.immutable;

public abstract class ReadWriteSetting<T> 
	extends ReadSetting<T> 
	implements WriteableSetting {
		
	public ReadWriteSetting(T defaultValue, ReadSetting<?>... dependencies) {
		super(defaultValue, dependencies);
	}
}
