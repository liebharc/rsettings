package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat; 

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettingsexample.mutable.Name;

public class SettingMutTest {	

	private static class NameSettingMut extends StateMut {
		private Name name;
		
		public NameSettingMut() {
			this.name = register(new Name());
		}

		public ReadWriteSettingMut<String> get() {
			return name;
		}
	}
	
	@Test
	public void settingCanBeConstructed() {
		NameSettingMut name = new NameSettingMut();
		assertThat(name.get().getValue()).isEqualTo("");
	}

	@Test
	public void resetValue() throws CheckFailedException {
		NameSettingMut name = new NameSettingMut();
		name.get().setValue("Peter");
		assertThat(name.get().getValue()).isEqualTo("Peter");
	}

	@Test
	public void settingCanBeReset() throws CheckFailedException {
		NameSettingMut name = new NameSettingMut();
		name.get().setValue("Peter");
		name.get().reset();
		assertThat(name.get().getValue()).isEqualTo("");
	}
}
