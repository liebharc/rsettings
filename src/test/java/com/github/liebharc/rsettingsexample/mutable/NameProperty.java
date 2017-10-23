package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.mutable.ReadWriteProperty;

public final class NameProperty extends ReadWriteProperty<String> {

	public NameProperty() {
		super("");
	}
	
	@Override
	public Optional<String> update(SettingState state) throws CheckFailedException {
		String value = state.get(this);
		if (value.equals("D'oh")) {
			throw new CheckFailedException("Setting this value to \"D'oh\" will throw an exception.");
		}
		
		return Optional.empty();
	}
}
