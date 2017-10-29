package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.Placeholder;
import com.github.liebharc.rsettings.immutable.PlaceholderType;

public class PlaceholderMut<T> extends Placeholder<T> {

	public PlaceholderMut(RegisterMut register) {
		super();
		register.add(this);
	}
	
	public PlaceholderMut(PlaceholderType type, RegisterMut register) {
		super(type);
		register.add(this);
	}
}
