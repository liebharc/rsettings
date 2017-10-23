package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.immutable.WriteableSetting;

public abstract class ReadWriteProperty<T> 
	extends ReadProperty<T> 
	implements WriteableSetting {

	public ReadWriteProperty(T defaultValue) {
		super(defaultValue);
	}

	public void setValue(T value) throws CheckFailedException {
		SettingState newState = 
				getState().get().change().
				set(this, value).build();
		getState().set(newState);
	}
	
	public void reset() throws CheckFailedException {
		setValue(this.getDefaultValue());
	}
}
