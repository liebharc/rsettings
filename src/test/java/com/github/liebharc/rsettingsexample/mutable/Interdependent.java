package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.RegisterMut;

public final class Interdependent extends ReadWriteSettingMut<Integer> {
	
	public Interdependent(RegisterMut register) {
		super(register, 0, NoDependencies());
	}
}
