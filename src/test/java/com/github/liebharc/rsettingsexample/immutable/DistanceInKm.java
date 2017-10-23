package com.github.liebharc.rsettingsexample.immutable;

import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.ReadOnlySetting;
import com.github.liebharc.rsettings.immutable.SettingState;

public class DistanceInKm extends ReadOnlySetting<Double> {

	private DistanceInM distanceInM;
	
	public DistanceInKm(DistanceInM distanceInM) {
		super(0.0);
		this.distanceInM = distanceInM;
	}
	
	@Override
	public Optional<Double> update(SettingState state) throws CheckFailedException {
		double m = state.get(distanceInM);
		return Optional.of(m / 1000.0);
	}
}
