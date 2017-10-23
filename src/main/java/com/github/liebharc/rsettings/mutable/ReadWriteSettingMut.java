package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.immutable.WriteableSetting;

public abstract class ReadWriteSettingMut<T> 
	extends ReadSettingMut<T> 
	implements WriteableSetting {

	public ReadWriteSettingMut(T defaultValue, ReadSettingMut<?>... dependencies) {
		super(defaultValue, dependencies);
	}

	public void setValue(T value) throws CheckFailedException {
		State newState = 
				getState().change().
				set(this, value).build();
		updateState(newState);
	}
	
	public void reset() throws CheckFailedException {
		setValue(this.getDefaultValue());
	}
}
