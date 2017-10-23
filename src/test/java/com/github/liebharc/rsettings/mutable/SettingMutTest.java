package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat; 

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettingsexample.mutable.Name;

public class SettingMutTest {	

	private static class NameSettingMut extends SettingStateMut {
		private Name name;
		
		public NameSettingMut() {
			this.name = register(new Name());
		}

		public ReadWriteSettingMut<String> get() {
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
