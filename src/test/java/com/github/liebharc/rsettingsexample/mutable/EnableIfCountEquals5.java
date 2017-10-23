package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.CanBeDisabledMut;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class EnableIfCountEquals5 
	extends ReadWriteSettingMut<String> 
	implements CanBeDisabledMut {

	private CountProperty count;
	
	public EnableIfCountEquals5(CountProperty count) {
		super("Hello", count);
		this.count = count;
	}

	@Override
	public boolean isEnabled() {
		return getState().get(count) == 5;
	}
}
