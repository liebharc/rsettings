package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.CheckFailedException;
import com.github.liebharc.rsettings.mutable.OufOfRangeException;
import com.github.liebharc.rsettings.mutable.ReadProperty;

public final class DoubleCountProperty extends ReadProperty<Integer> {
	private CountProperty count;
	
	public DoubleCountProperty(CountProperty count) {
		super(0);
		this.count = count;
	}

	private static int doubleTheCount(CountProperty count) throws CheckFailedException {
		if (count.getValue() < 0 || count.getValue() > 10) {
			throw new OufOfRangeException(count.getValue(), 0, 10);
		}
		
		return 2 * count.getValue();
	}
	
	@Override
	protected void onSourceValueChanged() throws CheckFailedException {
		super.onSourceValueChanged();
		setValueInternal(doubleTheCount(this.count));
	}
}
