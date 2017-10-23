package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.*;

public final class NameProperty extends ReadWriteProperty<String> {

	public NameProperty() {
		super("");
	}
	
	@Override
	protected void onSourceValueChanged() throws CheckFailedException {
		String value = getValue();
		if (value.equals("D'oh")) {
			throw new CheckFailedException("Setting this value to \"D'oh\" will throw an exception.");
		}
	}
}
