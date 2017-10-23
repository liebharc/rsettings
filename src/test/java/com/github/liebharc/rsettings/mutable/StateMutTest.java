package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettingsexample.mutable.BoundedDoubleProperty;
import com.github.liebharc.rsettingsexample.mutable.Count;
import com.github.liebharc.rsettingsexample.mutable.DoubleCountProperty;
import com.github.liebharc.rsettingsexample.mutable.EnableIfCountEquals5;
import com.github.liebharc.rsettingsexample.mutable.ExampleNetwork;
import com.github.liebharc.rsettingsexample.mutable.Interdependent;
import com.github.liebharc.rsettingsexample.mutable.Interdependent2;
import com.github.liebharc.rsettingsexample.mutable.Name;

public class StateMutTest {
	
	@Test
	public void createANetwork() {
		ExampleNetwork network = new ExampleNetwork();
		assertThat(network.listSettings().size()).isGreaterThan(0);
	}
	
	@Test
	public void derivedSettingsTest() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		DoubleCountProperty doubleCount = network.getDoubleCount();
		count.setValue(2);
		assertThat(doubleCount.getValue()).isEqualTo(4);
	}
	
	@Test
	public void derivedSettingThrowsExceptionTest() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		count.setValue(5);
		assertThatThrownBy(() -> {
			count.setValue(11);
		});
		
		assertThat(count.getValue()).isEqualTo(5);
	}
	
	@Test
	public void settingItselfThrowsAnException() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Name name = network.getName();
		name.setValue("Foobar");
		
		assertThatThrownBy(() -> {
			name.setValue("D'oh");
		});
		
		assertThat(name.getValue()).isEqualTo("Foobar");
	}
	
	@Test
	public void valueChangedEvent() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Name property = network.getName();
		IntBox numberOfCalls = new IntBox();
		Consumer<String> listener = i -> numberOfCalls.increment();
		property.getValueChangedEvent().subscribe(listener);
		property.setValue("Fish");
		assertThat(numberOfCalls).isEqualTo(1);
	}

	@Test
	public void enableDisableASetting() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		EnableIfCountEquals5 property = network.getEnableIfCountEquals5();
		assertThat(property.isEnabled()).isFalse();
		count.setValue(5);
		assertThat(property.isEnabled()).isTrue();
		count.setValue(6);
		assertThat(property.isEnabled()).isFalse();
	}

	@Test
	public void enableDisableASettingIfTransactionFails() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		EnableIfCountEquals5 property = network.getEnableIfCountEquals5();
		count.setValue(5);
		assertThat(property.isEnabled()).isTrue();
		
		assertThatThrownBy(() -> {
			count.setValue(20);
		});
		assertThat(property.isEnabled()).isTrue();
	}
			
	@Test
	public void executeInTransaction() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		Name name = network.getName();
		network
			.startTransaction()
			.set(count, 1)
			.set(name, "Fish")
			.complete();
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
		assertThatThrownBy(() -> {
			network
				.startTransaction()
				.set(count, 2)
				.set(name, "D'oh")
				.complete();
		});
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
	}
	
	@Test
	public void interDependence() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Interdependent prop1 = network.getInterdependentProperty();
		Interdependent2 prop2 = network.getInterdependent2Property();
		network
			.startTransaction()
			.set(prop1, 5)
			.set(prop2, -5)
			.complete();
		assertThat(prop1.getValue() + prop2.getValue()).isEqualTo(0);
	}
	
	@Test
	public void minMaxSetting() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		BoundedDoubleProperty property = network.getBoundedDoubleProperty();
		assertThat(property.getMin()).isEqualTo(-1.0);
		assertThat(property.getMax()).isEqualTo(1.0);
		property.setValue(0.5);
		assertThatThrownBy(() -> {
			property.setValue(2.0);
		});
		
		assertThat(property.getValue()).isEqualTo(0.5);

		assertThatThrownBy(() -> {
			property.setValue(-1.01);
		});
		assertThat(property.getValue()).isEqualTo(0.5);
	}
	
	@Test
	public void conflictingChanges() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Name name = network.getName();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(name, "Paul");
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(name, "Fish");
		transaction2.complete();
		transaction1.complete();
		assertThat(name.getValue()).isEqualTo("Paul");
	}
	
	@Test
	public void conflictingChangesReverseCompleteOrder() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Name name = network.getName();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(name, "Paul");
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(name, "Fish");
		transaction1.complete();
		transaction2.complete();
		assertThat(name.getValue()).isEqualTo("Fish");
	}
	
	@Test
	public void twoUpdatesNoConflict() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(network.getName(), "Paul");
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(network.getCount(), 2);
		transaction1.complete();
		transaction2.complete();
		assertThat(network.getName().getValue()).isEqualTo("Paul");
		assertThat(network.getCount().getValue()).isEqualTo(2);
	}
}
