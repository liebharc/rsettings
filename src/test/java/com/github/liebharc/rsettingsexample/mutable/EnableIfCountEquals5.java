package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.immutable.CanBeDisabled; 
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.mutable.ReadWriteProperty;

public final class EnableIfCountEquals5 
	extends ReadWriteProperty<String> 
	implements CanBeDisabled {

	private CountProperty count;
	
	public EnableIfCountEquals5(CountProperty count) {
		super("Hello");
		this.count = count;
	}

	@Override
	public boolean isEnabled(SettingState state) {
		return state.get(count) == 5;
	}
}
