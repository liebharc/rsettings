package com.github.liebharc.rsettings;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettingsexample.*;

import org.junit.Test;

public class SettingStateTest {

	@Test
	public void initASetting() {
		NameSetting name = new NameSetting();
		SettingState state = SettingState.FromSettings(name);
		assertThat(state.get(name)).isEqualTo("");
	}
	
	@Test
	public void changeASetting() throws CheckFailedException {
		NameSetting name = new NameSetting();
		SettingState state = SettingState.FromSettings(name);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
	}
	
	@Test
	public void trackStateRelations() throws CheckFailedException {
		NameSetting name = new NameSetting();
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
}
