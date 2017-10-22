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
	public void changeASetting() {
		NameSetting name = new NameSetting();
		SettingState state = SettingState.FromSettings(name);
		state = state.change()
				.set(name, "Peter")
				.build();
		assertThat(state.get(name)).isEqualTo("Peter");
	}
	
	@Test
	public void dependencies() {
		DistanceInM m = new DistanceInM();
		DistanceInKm km = new DistanceInKm();
		SettingState state = SettingState.FromSettings(m, km);
		state = state.change()
				.set(m, 1000.0)
				.build();
		assertThat(state.get(m)).isEqualTo(1000.0);
		assertThat(state.get(km)).isEqualTo(1.0);
	}
}
