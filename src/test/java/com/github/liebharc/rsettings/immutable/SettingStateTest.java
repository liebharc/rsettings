package com.github.liebharc.rsettings.immutable;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettingsexample.immutable.*;

import org.junit.Ignore;
import org.junit.Test;

public class SettingStateTest {

	@Test
	public void initASetting() {
		Name name = new Name();
		SettingState state = SettingState.FromSettings(name);
		assertThat(state.get(name)).isEqualTo("");
	}
	
	@Test
	public void changeASetting() throws CheckFailedException {
		Name name = new Name();
		SettingState state = SettingState.FromSettings(name);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
	}
	
	@Test
	public void trackStateRelations() throws CheckFailedException {
		Name name = new Name();
		SettingState parent = SettingState.FromSettings(name);
		SettingState child = parent.change()
				.set(name, "Peter")
				.build();
		SettingState childchild = child.change()
				.set(name, "Fish")
				.build();
		assertThat(parent.isDirectlyDerivedFrom(parent)).isFalse();
		assertThat(child.isDirectlyDerivedFrom(parent)).isTrue();
		assertThat(childchild.isDirectlyDerivedFrom(parent)).isFalse();
		assertThat(childchild.isDirectlyDerivedFrom(child)).isTrue();
	}
	
	@Test
	public void dependencies() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		SettingState state = SettingState.FromSettings(m, km);
		state = state.change()
				.set(m, 1000.0)
				.build();
		assertThat(state.get(m)).isEqualTo(1000.0);
		assertThat(state.get(km)).isEqualTo(1.0);
	}
	
	@Test
	public void isEnabled() throws CheckFailedException {
		DistanceInM m = new DistanceInM();
		SettingState state = SettingState.FromSettings(m);
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
		SettingState state = SettingState.FromSettings(name, m, km);
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
		SettingState state1 = SettingState.FromSettings(name, m, km);
		SettingState state2 = SettingState.FromSettings(name, m, km);
		final SettingState leftState = 
				state1.change()
				.set(name, "Peter")
				.build();
		final SettingState rightState = 
				state2.change()
				.set(m, 100.0)
				.build();
		assertThatThrownBy(() -> leftState.merge(rightState));
	}
	
	@Test
	@Ignore
	public void mergeTwoSettings() throws CheckFailedException {
		Name name = new Name();
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm(m);
		SettingState state = SettingState.FromSettings(name, m, km);
		SettingState state1 = 
				state.change()
				.set(name, "Peter")
				.build();
		SettingState state2 = 
				state.change()
				.set(m, 100.0)
				.build();
		SettingState merge = state1.merge(state2);
		assertThat(merge.get(name)).isEqualTo("Peter");
		assertThat(merge.get(m)).isEqualTo(100.0);
	}
}
