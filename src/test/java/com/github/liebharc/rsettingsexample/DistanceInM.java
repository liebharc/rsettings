package com.github.liebharc.rsettingsexample;

import com.github.liebharc.rsettings.*;

public class DistanceInM 
	extends Setting<Double>
	implements CanBeDisabled {
	
	public DistanceInM() {
		super(0.0);
	}
	
	public boolean isEnabled(SettingState state) {
		return state.get(this) > 0.0;
	}
}
