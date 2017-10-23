package com.github.liebharc.rsettingsexample.immutable;

import com.github.liebharc.rsettings.immutable.*;

public class DistanceInM 
	extends ReadWriteSetting<Double>
	implements CanBeDisabled {
	
	public DistanceInM() {
		super(0.0);
	}
	
	public boolean isEnabled(State state) {
		return state.get(this) > 0.0;
	}
}
