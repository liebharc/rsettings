package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.Register;

public final class Interdependent extends ReadWriteSettingMut<Integer> {
	
	public Interdependent(Register register) {
		super(register, 0, NoDependencies());
	}
}
