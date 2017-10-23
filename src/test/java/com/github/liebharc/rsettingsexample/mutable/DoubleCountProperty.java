package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.OufOfRangeException;
import com.github.liebharc.rsettings.immutable.ReadOnlySetting;
import com.github.liebharc.rsettings.immutable.SettingState;

public final class DoubleCountProperty extends ReadOnlySetting<Integer> {
	private CountProperty count;
	
	public DoubleCountProperty(CountProperty count) {
		super(0);
		this.count = count;
	}

	private static int doubleTheCount(int count) throws CheckFailedException {
		if (count < 0 || count > 10) {
			throw new OufOfRangeException(count, 0, 10);
		}
		
		return 2 * count;
	}
	
	@Override
	public Optional<Integer> update(SettingState state) throws CheckFailedException {
		return Optional.of(doubleTheCount(state.get(count)));
	}
}