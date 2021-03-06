package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.RegisterMut;

public final class Name extends ReadWriteSettingMut<String> {

	public Name(RegisterMut register) {
		super(register, "", NoDependencies());
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
