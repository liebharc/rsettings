package com.github.liebharc.rsettingsexample.mutable;

import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException; 
import com.github.liebharc.rsettings.Checks;
import com.github.liebharc.rsettings.immutable.MinMaxLimited;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettings.mutable.ReadProperty;

public class BoundedDoubleProperty 
	extends ReadProperty<Double> 
	implements MinMaxLimited<Double> {

	public BoundedDoubleProperty() {
		super(0.0);
	}

	@Override
	public Double getMin(SettingState state) {
		return -1.0;
	}

	@Override
	public Double getMax(SettingState state) {
		return 1.0;
	}
	
	@Override
	public Optional<Double> update(SettingState state) throws CheckFailedException {
		Checks.CheckMinMax(state.get(this), getMin(state), getMax(state));
		return super.update(state);
	}
}
