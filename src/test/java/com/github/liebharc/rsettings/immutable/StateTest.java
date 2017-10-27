package com.github.liebharc.rsettings.immutable;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.State;
import com.github.liebharc.rsettingsexample.immutable.*;
import com.github.liebharc.rsettingsexample.immutable.MetricDouble.Prefix;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;

public class StateTest {

	@Test
	public void initASetting() {
		Name name = new Name();
		State state = new State(name);
		assertThat(state.get(name)).isEqualTo("");
	}
	
	@Test
	public void changeASetting() throws CheckFailedException {
		Name name = new Name();
		State state = new State(name);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
	}
	
	@Test
	public void dependencies() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state = new State(m, km);
		state = state.change()
				.set(m, 1000.0)
				.build();
		assertThat(state.get(m)).isEqualTo(1000.0);
		assertThat(state.get(km)).isEqualTo(1.0);
	}
	
	@Test
	public void isEnabled() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		State state = new State(m);
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
		State state = new State(name, m, km);
		assertThat(state.getChanges()).containsExactly(name, m, km);
		
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.getChanges()).containsExactly(name);
		assertThat(state.hasChanged(name)).isTrue();
		assertThat(state.hasChanged(m)).isFalse();
		assertThat(state.hasChanged(km)).isFalse();
		assertThat(state.hasAnyChanged(Arrays.asList(name, m))).isTrue();
		assertThat(state.hasAnyChanged(Arrays.asList(km, m))).isFalse();
		
		state = state.change()
				.set(m, 1.0)
				.build();
		assertThat(state.getChanges()).containsExactly(m, km);
		assertThat(state.hasChanged(name)).isFalse();
		assertThat(state.hasChanged(m)).isTrue();
		assertThat(state.hasChanged(km)).isTrue();
	}
	
	@Test
	public void trackChangesToAReferencePoint() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State reference = new State(name, m, km);
		assertThat(reference.getChanges()).containsExactly(name, m, km);
		
		reference = reference.change()
				.set(name, "Peter")
				.build();
		
		State withSomeChanges = reference.change()
				.set(m, 1.0)
				.set(name,  "Paul")
				.build();
		withSomeChanges = withSomeChanges.change()
				.set(name,  "Peter")
				.build();
		assertThat(withSomeChanges.getChanges(reference)).containsExactly(m, km);
		assertThat(withSomeChanges.hasChanged(name, reference)).isFalse();
		assertThat(withSomeChanges.hasChanged(m, reference)).isTrue();
		assertThat(withSomeChanges.hasChanged(km, reference)).isTrue();
	}
	
	@Test
	public void trackTouchedValues() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State reference = new State(name, km, m);
		assertThat(reference.getChanges()).containsExactly(name, km, m);
		
		reference = reference.change()
				.set(name, "Peter")
				.build();
		
		State withSomeChanges = reference.change()
				.set(m, 1.0)
				.set(name,  "Paul")
				.build();
		withSomeChanges = withSomeChanges.change()
				.set(name,  "Peter")
				.build();
		assertThat(withSomeChanges.getTouchedSettings(reference)).containsExactly(name, km, m);
		assertThat(withSomeChanges.hasBeenTouched(name, reference)).isTrue();
		assertThat(withSomeChanges.hasBeenTouched(m, reference)).isTrue();
		assertThat(withSomeChanges.hasBeenTouched(km, reference)).isTrue();
	}
	
	@Test
	public void mergeTwoDifferentSettings() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state1 = new State(name, m, km);
		State state2 = new State(name, m, km);
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
		State state = new State(name, m, km);
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
	
	@Test
	public void convertTo() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		State state = new State(m);
		MetricDouble value = new MetricDouble(7, Prefix.Kilo);
		state = state.change()
				.set(m, value)
				.build();
		
		assertThat(state.get(m)).isEqualTo(7000.0);
	}
	
	@Test
	public void storageTest() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		State state = new State(name, m, km);
		Set<Map.Entry<ReadSetting<?>, Object>> values = state.getPersistenceValues();
		Set<ReadSetting<?>> keys = values.stream().map(e -> e.getKey()).collect(Collectors.toSet());
		assertThat(keys).doesNotContain(km);
		assertThat(keys).contains(m);
		assertThat(keys).contains(name);
		assertThat(values.size()).isEqualTo(2);
		assertThat(values.stream().filter(e -> e.getKey() == m).findFirst().get().getValue()).isEqualTo(0.0);
	}
	
	private static class NameCopy extends ReadWriteSetting<String> {

		private Name name;

		public NameCopy(Name name) {
			super("", Dependencies(name));
			this.name = name;
		}
		
		@Override
		protected Optional<String> update(State state) throws CheckFailedException {
			if (state.hasChanged(this))
				return super.update(state);
			return Optional.of(state.get(name));
		}
	}
	
	@Test
	public void nonOverwritingDependency() throws CheckFailedException {
		Name name = new Name();
		NameCopy copy = new NameCopy(name);
		DistanceInM m = new DistanceInM();
		State state = new State(name, copy, m);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
		assertThat(state.get(copy)).isEqualTo("Peter");
		
		state = state.change()
				.set(name, "Paul")
				.set(copy, "Fish")
				.build();
		assertThat(state.get(name)).isEqualTo("Paul");
		assertThat(state.get(copy)).isEqualTo("Fish");
		
		state = state.change()
				.set(m, 10.0)
				.build();
		assertThat(state.get(name)).isEqualTo("Paul");
		assertThat(state.get(copy)).isEqualTo("Fish");
		
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
		assertThat(state.get(copy)).isEqualTo("Peter");
	}
}
