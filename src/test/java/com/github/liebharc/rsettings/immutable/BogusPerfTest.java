package com.github.liebharc.rsettings.immutable;

import java.util.*;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettingsexample.immutable.*;


public class BogusPerfTest {
	
	private static final int NANO_TO_MILLI = 1000000;

	public Register createRegister(int numberOfSettings, List<DistanceInM> distances) {
		Register reg = new Register();
		for (int i = 0; i < numberOfSettings / 2; i++) {
			DistanceInM m = new DistanceInM();
			reg.add(m);
			distances.add(m);
		}
		
		for (int i = 0; i < numberOfSettings / 2; i++) {
			reg.add(new DistanceInKm(distances.get(i)));
		}
		
		return reg;
	}
	
	@Test
	public void statePerfTest() throws CheckFailedException {
		List<Integer> initDurations = new ArrayList<>();
		List<Integer> setAllDurations = new ArrayList<>();
		List<Integer> setOneDurations = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			statePerfTestIteration(initDurations, setAllDurations, setOneDurations);
		}
		
		double initAverage = average(initDurations) / NANO_TO_MILLI;
		double setAllAverage = average(setAllDurations) / NANO_TO_MILLI;
		double setOneAverage = average(setOneDurations) / NANO_TO_MILLI;
		System.out.println("Average init time [ms]: " +  initAverage);
		System.out.println("Average set all time [ms]: " +  setAllAverage);
		System.out.println("Average set one time [ms]: " +  setOneAverage);
		assertThat(initAverage).isLessThan(10.0);
		assertThat(setAllAverage).isLessThan(20.0);
		assertThat(setOneAverage).isLessThan(2.0);
	}
	
	private double average(List<Integer> values) {
		return values.stream().mapToInt(i -> i).average().getAsDouble();
	}
	
	private void statePerfTestIteration(
			List<Integer> initDurations,
			List<Integer> setAllDurations,
			List<Integer> setOneDurations) throws CheckFailedException {
		long stateInitStart = System.nanoTime();
		List<DistanceInM> writers = new ArrayList<>();
		Register reg = createRegister(1000, writers);
		State state = new State(reg);
		long stateInitDuration =  System.nanoTime() - stateInitStart;
		initDurations.add((int) stateInitDuration);
		System.out.println("Init time [ms]: " + (stateInitDuration / NANO_TO_MILLI));
		
		long setAllStart = System.nanoTime();
		State.Builder builder = state.change();
		for (DistanceInM setting : writers) {
			builder.set(setting, 1.0);
		}
		state = builder.build();
		long setAllDuration= System.nanoTime() - setAllStart;
		setAllDurations.add((int) setAllDuration);
		System.out.println("Sell all time [ms]: " + (setAllDuration / NANO_TO_MILLI));

		Random random = new Random();
		long setOneStart = System.nanoTime();
		builder = state.change();
		builder.set(writers.get(random.nextInt(writers.size())), 2.0);
		state = builder.build();	
		long setOneDuration= System.nanoTime() - setOneStart;
		setOneDurations.add((int) setOneDuration);
		System.out.println("Sell one time [ms]: " + (setOneDuration / NANO_TO_MILLI));
	}
}
