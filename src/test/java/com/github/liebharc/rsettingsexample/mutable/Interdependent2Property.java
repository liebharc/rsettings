package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.mutable.*;

public final class Interdependent2Property extends ReadWriteProperty<Integer> {

	private InterdependentProperty other;
	
	public Interdependent2Property(InterdependentProperty count) {
		super(0);
		this.other = count;
	}
	
	@Override
	protected void onSourceValueChanged() {
		int sum = other.getValue() + this.getValue();
		if (sum != 0) {
			throw new IllegalArgumentException("The sum of both values must be 0, but it is " + sum);
		}
	}
}
