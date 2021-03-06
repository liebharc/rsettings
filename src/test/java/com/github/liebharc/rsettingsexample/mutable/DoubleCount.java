package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.OufOfRangeException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.ReadSettingMut;
import com.github.liebharc.rsettings.mutable.RegisterMut;

public final class DoubleCount extends ReadSettingMut<Integer> {
	private Count count;
	
	public DoubleCount(RegisterMut register, Count count) {
		super(register, 0, Dependencies(count));
		this.count = count;
	}

	private static int doubleTheCount(int count) throws CheckFailedException {
		if (count < 0 || count > 10) {
			throw new OufOfRangeException(count, 0, 10);
		}
		
		return 2 * count;
	}
	
	@Override
	public Optional<Integer> update(State state) throws CheckFailedException {
		return Optional.of(doubleTheCount(state.get(count)));
	}
}
