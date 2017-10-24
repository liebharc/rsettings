package com.github.liebharc.rsettingsexample.mutable;

import com.github.liebharc.rsettings.immutable.CanConvertTo;

public class IntSign implements CanConvertTo<Sign>{

	private int value;

	public IntSign(int value) {
		this.value = value;
		
	}

	@Override
	public Sign convertTo(Sign previousValue) {
		if (value < 0) {
			return Sign.Negative;
		}
		
		if (value > 0) {
			return Sign.Positve;
		}
		
		return Sign.Zero;
	}
}
