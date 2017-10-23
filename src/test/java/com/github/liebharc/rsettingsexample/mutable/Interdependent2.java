package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class Interdependent2 extends ReadWriteSettingMut<Sign> {
	private Interdependent other;
	
	public Interdependent2(Interdependent count) {
		super(Sign.Zero, count);
		this.other = count;
	}
	
	@Override
	public Optional<Sign> update(State state) throws CheckFailedException {
		int value = state.get(other);
		switch (state.get(this)) {
		case Zero:
			if (value != 0) {
				throw new CheckFailedException("Value must be 0");
			}
			break;
		case Negative:
			if (value >= 0) {
				throw new CheckFailedException("Value must be negative");
			}
			break;
		case Positve:
			if (value <= 0) {
				throw new CheckFailedException("Value must be positive");
			}
			break;
		}
		
		return Optional.empty();
	}
}
