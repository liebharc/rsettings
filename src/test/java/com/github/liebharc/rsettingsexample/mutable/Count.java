package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;

public final class Count extends ReadWriteSettingMut<Integer> {

	public Count() {
		super(0, NoDependencies());
	}
}
