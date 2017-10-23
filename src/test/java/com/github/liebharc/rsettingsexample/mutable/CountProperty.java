package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class CountProperty extends ReadWriteSettingMut<Integer> {

	public CountProperty() {
		super(0);
	}
}
