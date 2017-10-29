package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Consumer;

import org.junit.*;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettingsexample.mutable.*;

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
		DoubleCount doubleCount = network.getDoubleCount();
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
		}).isInstanceOf(CheckFailedException.class);
		
		assertThat(count.getValue()).isEqualTo(5);
	}
	
	@Test
	public void settingItselfThrowsAnException() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Name name = network.getName();
		name.setValue("Foobar");
		
		assertThatThrownBy(() -> {
			name.setValue("D'oh");
		}).isInstanceOf(CheckFailedException.class);
		
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
		}).isInstanceOf(CheckFailedException.class);
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
			.execute();
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
		assertThatThrownBy(() -> {
			network
				.startTransaction()
				.set(count, 2)
				.set(name, "D'oh")
				.execute();
		}).isInstanceOf(CheckFailedException.class);
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
	}
	
	@Test
	public void interDependence() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Interdependent prop1 = network.getInterdependent();
		Interdependent2 prop2 = network.getInterdependent2();
		network
			.startTransaction()
			.set(prop1, 5)
			.set(prop2, Sign.Positve)
			.execute();
		assertThat(prop1.getValue()).isEqualTo(5);
		assertThat(prop2.getValue()).isEqualTo(Sign.Positve);
	}
	
	@Test
	public void minMaxSetting() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		BoundedDouble property = network.getBoundedDouble();
		assertThat(property.getMin()).isEqualTo(-1.0);
		assertThat(property.getMax()).isEqualTo(1.0);
		property.setValue(0.5);
		assertThatThrownBy(() -> {
			property.setValue(2.0);
		}).isInstanceOf(CheckFailedException.class);
		
		assertThat(property.getValue()).isEqualTo(0.5);

		assertThatThrownBy(() -> {
			property.setValue(-1.01);
		}).isInstanceOf(CheckFailedException.class);
		assertThat(property.getValue()).isEqualTo(0.5);
	}
	
	@Test
	public void conflictingChanges() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		DoubleCount doubleCount = network.getDoubleCount();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(count, 1);
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(count, 2);
		transaction2.execute();
		transaction1.execute();
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(doubleCount.getValue()).isEqualTo(2);
	}
	
	@Test
	public void conflictingChangesReverseCompleteOrder() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		Count count = network.getCount();
		DoubleCount doubleCount = network.getDoubleCount();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(count, 1);
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(count, 2);
		transaction1.execute();
		transaction2.execute();
		assertThat(count.getValue()).isEqualTo(2);
		assertThat(doubleCount.getValue()).isEqualTo(4);
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
		transaction1.execute();
		transaction2.execute();
		assertThat(network.getName().getValue()).isEqualTo("Paul");
		assertThat(network.getCount().getValue()).isEqualTo(2);
	}
	
	@Test
	public void twoUpdatesLeadToInconsistentState() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		StateMut.Builder transaction1 = 
				network.startTransaction()
				.set(network.getInterdependent(), -5)
				.set(network.getInterdependent2(), Sign.Negative);
		StateMut.Builder transaction2 = 
				network.startTransaction()
				.set(network.getInterdependent(), 0);
		transaction1.execute();
		assertThatThrownBy(() -> transaction2.execute())
			.isInstanceOf(CheckFailedException.class);
		assertThat(network.getInterdependent().getValue()).isEqualTo(-5);
		assertThat(network.getInterdependent2().getValue()).isEqualTo(Sign.Negative);
	}
	
	@Test
	public void convertTo() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		network.startTransaction()
			.set(network.getInterdependent(), 5)
			.set(network.getInterdependent2(), new IntSign(1))
			.execute();
		assertThat(network.getInterdependent().getValue()).isEqualTo(5);
		assertThat(network.getInterdependent2().getValue()).isEqualTo(Sign.Positve);
		
		network.getInterdependent2().setValue(new IntSign(2));
		assertThat(network.getInterdependent2().getValue()).isEqualTo(Sign.Positve);
	}
}
