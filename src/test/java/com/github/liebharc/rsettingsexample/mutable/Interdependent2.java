package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class Interdependent2 extends ReadWriteSettingMut<Integer> {

	private Interdependent other;
	
	public Interdependent2(Interdependent count) {
		super(0, count);
		this.other = count;
	}
	
	@Override
	public Optional<Integer> update(State state) throws CheckFailedException {
		int sum = state.get(other) + state.get(this);
		if (sum != 0) {
			throw new IllegalArgumentException("The sum of both values must be 0, but it is " + sum);
		}
		
		return Optional.empty();
	}
}
