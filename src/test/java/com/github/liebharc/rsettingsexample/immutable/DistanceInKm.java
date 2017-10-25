package com.github.liebharc.rsettingsexample.immutable;

import java.util.Optional;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.ReadSetting;
import com.github.liebharc.rsettings.immutable.State;

public class DistanceInKm extends ReadSetting<Double> {

	private DistanceInM distanceInM;
	
	public DistanceInKm(DistanceInM distanceInM) {
		super(0.0, new ReadSetting<?>[] { distanceInM });
		this.distanceInM = distanceInM;
	}
	
	@Override
	public Optional<Double> update(State state) throws CheckFailedException {
		double m = state.get(distanceInM);
		return Optional.of(m / 1000.0);
	}
}
