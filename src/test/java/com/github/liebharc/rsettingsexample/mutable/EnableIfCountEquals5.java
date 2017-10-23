package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.CheckFailedException;
import com.github.liebharc.rsettings.mutable.ReadProperty;

public final class EnableIfCountEquals5 extends ReadProperty<String> {

	private CountProperty count;
	
	public EnableIfCountEquals5(CountProperty count) {
		super("Hello", false);
		this.count = count;
	}

	@Override
	protected void onSourceValueChanged() throws CheckFailedException {
		super.onSourceValueChanged();
		
		boolean countEquals5 = count.getValue() == 5;
		setEnabled(countEquals5);
	}
}
