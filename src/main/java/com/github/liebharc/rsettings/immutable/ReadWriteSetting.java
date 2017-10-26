package com.github.liebharc.rsettings.immutable;

/**
 * A setting which can be changed by an user.
 * @param <T> The type of the setting.
 */
public abstract class ReadWriteSetting<T> 
	extends ReadSetting<T> 
	implements WriteableSetting<T> {
		
	public ReadWriteSetting(T defaultValue, Dependencies dependencies) {
		super(defaultValue, dependencies);
	}
	
	@Override
	public boolean shouldBeStored() {
		return true;
	}
}
