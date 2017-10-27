package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.CanBeDisabledMut;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.Register;

public final class EnableIfCountEquals5 
	extends ReadWriteSettingMut<String> 
	implements CanBeDisabledMut<String> {

	private Count count;
	
	public EnableIfCountEquals5(Register register, Count count) {
		super(register, "Hello", Dependencies(count));
		this.count = count;
	}

	@Override
	public boolean isEnabled(State state) {
		return getState(state).get(count) == 5;
	}
}
