package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.SettingState;
import com.github.liebharc.rsettingsexample.mutable.NameProperty;

public class PropertyTest {

	private static class NameSettingMut {
		private CurrentSettingState state;
		
		private ReadWriteProperty<String> name;
		
		public NameSettingMut() {
			NameProperty name = new NameProperty();
			SettingState init = SettingState.FromSettings(name);
			state = new CurrentSettingState(init);
			this.name = new ReadWriteProperty<>(state, name);
		}

		public ReadWriteProperty<String> get() {
			return name;
		}
	}
	
	@Test
	public void propertyCanBeConstructed() {
		NameSettingMut name = new NameSettingMut();
		assertThat(name.get().getValue()).isEqualTo("");
	}

	@Test
	public void resetValueTestSet() throws CheckFailedException {
		NameSettingMut name = new NameSettingMut();
		name.get().setValue("Peter");
		assertThat(name.get().getValue()).isEqualTo("Peter");
	}

	@Test
	public void propertyCanBeReset() throws CheckFailedException {
		NameSettingMut name = new NameSettingMut();
		name.get().setValue("Peter");
		name.get().reset();
		assertThat(name.get().getValue()).isEqualTo("");
	}
}
