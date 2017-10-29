package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.RegisterMut;

public final class Count extends ReadWriteSettingMut<Integer> {

	public Count(RegisterMut register) {
		super(register, 0, NoDependencies());
	}
}
