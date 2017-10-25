package com.github.liebharc.rsettingsexample.immutable;

import com.github.liebharc.rsettings.immutable.*;

public final class Name extends ReadWriteSetting<String >{

	public Name() {
		super("", NoDependencies());
	}
}
