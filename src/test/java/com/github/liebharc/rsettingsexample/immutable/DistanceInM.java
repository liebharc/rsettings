package com.github.liebharc.rsettingsexample.immutable;

import com.github.liebharc.rsettings.immutable.*;

public class DistanceInM 
	extends ReadWriteSetting<Double>
	implements CanBeDisabled<Double> {
	
	public DistanceInM() {
		super(0.0, NoDependencies());
	}
	
	public boolean isEnabled(State state) {
		return state.get(this) > 0.0;
	}
}
