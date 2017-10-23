package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.mutable.ReadWriteProperty;

public final class Interdependent2Property extends ReadWriteProperty<Integer> {

	private InterdependentProperty other;
	
	public Interdependent2Property(InterdependentProperty count) {
		super(0);
		this.other = count;
	}
	
	@Override
	public Optional<Integer> update(SettingState state) throws CheckFailedException {
		int sum = state.get(other) + state.get(this);
		if (sum != 0) {
			throw new IllegalArgumentException("The sum of both values must be 0, but it is " + sum);
		}
		
		return Optional.empty();
	}
}
