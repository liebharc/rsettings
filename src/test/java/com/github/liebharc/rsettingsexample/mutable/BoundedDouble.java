package com.github.liebharc.rsettingsexample.mutable;

 import java.util.Optional; 

import com.github.liebharc.rsettings.CheckFailedException; 
import com.github.liebharc.rsettings.Checks;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettings.mutable.MinMaxLimitedMut;
import com.github.liebharc.rsettings.mutable.ReadWriteSettingMut;
import com.github.liebharc.rsettings.mutable.RegisterMut;

public class BoundedDouble 
	extends ReadWriteSettingMut<Double> 
	implements MinMaxLimitedMut<Double> {

	public BoundedDouble(RegisterMut register) {
		super(register, 0.0, NoDependencies());
	}

	@Override
	public Double getMin(State state) {
		return -1.0;
	}

	@Override
	public Double getMax(State state) {
		return 1.0;
	}
	
	@Override
	public Optional<Double> update(State state) throws CheckFailedException {
		Checks.CheckMinMax(state.get(this), getMin(), getMax());
		return super.update(state);
	}
}
