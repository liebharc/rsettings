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
		state = state.change(name, "Peter");
		assertThat(state.get(name)).isEqualTo("Peter");
	}
}
