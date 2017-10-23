package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.immutable.WriteableSetting;

public abstract class ReadWriteSettingMut<T> 
	extends ReadSettingMut<T> 
	implements WriteableSetting {

	public ReadWriteSettingMut(T defaultValue) {
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
