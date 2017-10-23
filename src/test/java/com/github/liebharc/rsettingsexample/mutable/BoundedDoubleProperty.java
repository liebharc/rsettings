package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.CheckFailedException;
import com.github.liebharc.rsettings.mutable.Checks;
import com.github.liebharc.rsettings.mutable.MinMaxProperty;
import com.github.liebharc.rsettings.mutable.ReadWriteProperty;

public class BoundedDoubleProperty 
	extends ReadWriteProperty<Double> 
	implements MinMaxProperty<Double> {

	public BoundedDoubleProperty() {
		super(0.0);
	}

	@Override
	public Double getMin() {
		return -1.0;
	}

	@Override
	public Double getMax() {
		return 1.0;
	}
	
	@Override
	protected void onSourceValueChanged() throws CheckFailedException {
		Checks.CheckMinMax(getValue(), getMin(), getMax());
		super.onSourceValueChanged();
	}
}
