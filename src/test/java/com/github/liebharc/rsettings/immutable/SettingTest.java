package com.github.liebharc.rsettings.immutable;

import static org.assertj.core.api.Assertions.*;

import com.github.liebharc.rsettingsexample.immutable.*;

import org.junit.Test;

public class SettingTest {

	@Test
	public void testSetting() {
		Name name = new Name();
		assertThat(name.getDefaultValue()).isEqualTo("");
	}

}
