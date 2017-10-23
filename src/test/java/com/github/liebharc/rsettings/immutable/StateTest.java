package com.github.liebharc.rsettings.immutable;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettingsexample.immutable.*;

import org.junit.*;

public class StateTest {

	@Test
	public void initASetting() {
		Name name = new Name();
		State state = State.FromSettings(name);
		assertThat(state.get(name)).isEqualTo("");
	}
	
	@Test
	public void changeASetting() throws CheckFailedException {
		Name name = new Name();
		State state = State.FromSettings(name);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
	}
	
	@Test
	public void dependencies() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state = State.FromSettings(m, km);
		state = state.change()
				.set(m, 1000.0)
				.build();
		assertThat(state.get(m)).isEqualTo(1000.0);
		assertThat(state.get(km)).isEqualTo(1.0);
	}
	
	@Test
	public void isEnabled() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		State state = State.FromSettings(m);
		assertThat(state.isEnabled(m)).isFalse();
		state = state.change()
				.set(m, 1.0)
				.build();
		assertThat(state.isEnabled(m)).isTrue();
	}
	
	@Test
	public void trackLastChanges() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state = State.FromSettings(name, m, km);
		assertThat(state.getLastChanges()).containsExactly(name, m, km);
		
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.getLastChanges()).containsExactly(name);
		
		state = state.change()
				.set(m, 1.0)
				.build();
		assertThat(state.getLastChanges()).containsExactly(m, km);
	}
	
	@Test
	public void mergeTwoDifferentSettings() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state1 = State.FromSettings(name, m, km);
		State state2 = State.FromSettings(name, m, km);
		final State leftState = 
				state1.change()
				.set(name, "Peter")
				.build();
		final State rightState = 
				state2.change()
				.set(m, 100.0)
				.build();
		assertThatThrownBy(() -> leftState.merge(rightState));
	}
	
	@Test
	public void mergeTwoSettings() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state = State.FromSettings(name, m, km);
		State state1 = 
				state.change()
				.set(name, "Peter")
				.build();
		State state2 = 
				state.change()
				.set(m, 100.0)
				.build();
		State merge = state1.merge(state2);
		assertThat(merge.get(name)).isEqualTo("Peter");
		assertThat(merge.get(m)).isEqualTo(100.0);
	}
}
