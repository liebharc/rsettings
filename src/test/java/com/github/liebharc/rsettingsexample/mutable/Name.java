package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class Name extends ReadWriteSettingMut<String> {

	public Name() {
		super("");
	}
	
	@Override
	public Optional<String> update(State state) throws CheckFailedException {
		String value = state.get(this);
		if (value.equals("D'oh")) {
			throw new CheckFailedException("Setting this value to \"D'oh\" will throw an exception.");
		}
		
		return Optional.empty();
	}
}
