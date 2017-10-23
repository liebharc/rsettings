package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.github.liebharc.rsettings.mutable.ReadProperty;
import com.github.liebharc.rsettings.mutableexample.*;
import com.github.liebharc.rsettingsexample.mutable.CountProperty;
import com.github.liebharc.rsettingsexample.mutable.EnableIfCountEquals5;
import com.github.liebharc.rsettingsexample.mutable.NameProperty;

public class PropertyTest {

	@Test
	public void propertyCanBeConstructed() {
		ReadProperty<?> property = new NameProperty();
		assertThat(property.getValue()).isEqualTo("");
	}
	
	@Test
	public void resetValueTest() {
		EnableIfCountEquals5 property = new EnableIfCountEquals5(new CountProperty());
		assertThat(property.getValue()).isEqualTo("Hello");
	}

	@Test
	public void propertyCanBeSet() {
		NameProperty property = new NameProperty();
		property.setValue("Peter");
		assertThat(property.getValue()).isEqualTo("Peter");
	}

	@Test
	public void propertyCanBeReset() {
		NameProperty property = new NameProperty();
		property.setValue("Peter");
		property.Reset();
		assertThat(property.getValue()).isEqualTo("");
	}
}
