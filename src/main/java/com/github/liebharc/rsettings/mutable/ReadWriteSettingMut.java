package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.CanConvertTo;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.immutable.WriteableSetting;
import com.github.liebharc.rsettings.immutable.Dependencies;

public abstract class ReadWriteSettingMut<T> 
	extends ReadSettingMut<T> 
	implements WriteableSetting<T> {

	public ReadWriteSettingMut(Register register, T defaultValue, Dependencies dependencies) {
		super(register, defaultValue, dependencies);
	}

	public void setValue(T value) throws CheckFailedException {
		State newState = 
				getState().change().
				set(this, value).build();
		updateState(newState);
	}
	
	public <TConvertible extends CanConvertTo<T>>
		void setValue(TConvertible value) throws CheckFailedException {
		setValue(value.convertTo(getValue()));
	}
	
	public void reset() throws CheckFailedException {
		setValue(this.getDefaultValue());
	}
	
	@Override
	public boolean shouldBeStored() {
		return true;
	}
}
