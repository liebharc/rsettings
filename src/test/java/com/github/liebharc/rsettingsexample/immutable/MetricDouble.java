package com.github.liebharc.rsettingsexample.immutable;

import com.github.liebharc.rsettings.immutable.CanConvertTo;

public class MetricDouble implements CanConvertTo<Double> {

	private Prefix prefix;
	private double value;

	public enum Prefix {
		None,
		Kilo,
		Mega
	}
	
	public MetricDouble(double value, Prefix prefix) {
		this.value = value;
		this.prefix = prefix;
	}

	@Override
	public Double convertTo(Double previousValue) {
		return value * getMultiplier();
	}

	private double getMultiplier() {
		switch (prefix) {
		case None:
			return 1.0;
		case Kilo:
			return 1000.0;
		case Mega:
			return 1e6;
		default:
			throw new IllegalArgumentException("Unknown prefix");
		}
	}
}
