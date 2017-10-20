package com.github.liebharc.rsettings;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettingsexample.*;

import org.junit.Test;

public class SettingTest {

	@Test
	public void testSetting() {
		NameSetting name = new NameSetting();
		assertThat(name.getDefaultValue()).isEqualTo("");
	}

}
