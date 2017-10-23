package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;

import org.junit.*;

import com.github.liebharc.rsettings.mutable.AutoTransaction;
import com.github.liebharc.rsettings.mutable.ReadProperty;
import com.github.liebharc.rsettings.mutableexample.*;
import com.github.liebharc.rsettingsexample.mutable.CountProperty;
import com.github.liebharc.rsettingsexample.mutable.DoubleCountProperty;
import com.github.liebharc.rsettingsexample.mutable.EnableIfCountEquals5;
import com.github.liebharc.rsettingsexample.mutable.ExampleNetwork;
import com.github.liebharc.rsettingsexample.mutable.Interdependent2Property;
import com.github.liebharc.rsettingsexample.mutable.InterdependentProperty;
import com.github.liebharc.rsettingsexample.mutable.NameProperty;
public class PropertyNetworkTest {

	@Test
	public void createANetwork() {
		ExampleNetwork network = new ExampleNetwork();
		assertThat(network.getNumberOfProperties()).isGreaterThan(0);
	}
	
	@Test
	public void derivedPropertiesTest() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		CountProperty count = network.getCount();
		DoubleCountProperty doubleCount = network.getDoubleCount();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(2);
		}
		
		assertThat(doubleCount.getValue()).isEqualTo(4);
	}
	
	@Test
	public void derivedPropertyThrowsExceptionTest() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		CountProperty count = network.getCount();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(5);
		}
		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				count.setValue(11);
			}
		});
		
		assertThat(count.getValue()).isEqualTo(5);
	}
	
	@Test
	public void propertyItselfThrowsAnException() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		NameProperty name = network.getName();
		try (AutoTransaction t = network.startTransaction()) {
			name.setValue("Foobar");
		}
		
		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				name.setValue("D'oh");
			}
		});
		
		assertThat(name.getValue()).isEqualTo("Foobar");
	}

	@Test
	public void propertyChangeCanBeUndone() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		NameProperty property = network.getName();
		try (AutoTransaction t = network.startTransaction()) {
			property.setValue("Fish");
		}

		try (AutoTransaction t = network.startTransaction()) {
			property.setValue("Peter");
			t.rollback();
		}
		assertThat(property.getValue()).isEqualTo("Fish");
	}

	@Test
	public void valueAcceptedEvent() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		NameProperty property = network.getName();
		IntBox numberOfCalls = new IntBox();
		Consumer<ReadProperty<String>> listener = i -> numberOfCalls.increment();
		property.getValueAcceptedEvent().subscribe(listener);
		try (AutoTransaction t = network.startTransaction()) {
			property.setValue("Fish");
			assertThat(numberOfCalls).isEqualTo(0);
			assertThat(network.hasPendingTransaction()).isTrue();
		}
		
		assertThat(network.hasPendingTransaction()).isFalse();
		assertThat(numberOfCalls).isEqualTo(1);
	}
	
	@Test
	public void enableDisableAProperty() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		CountProperty count = network.getCount();
		EnableIfCountEquals5 property = network.getEnableIfCountEquals5();
		assertThat(property.IsEnabled()).isFalse();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(5);
		}
		assertThat(property.IsEnabled()).isTrue();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(6);
		}
		assertThat(property.IsEnabled()).isFalse();
	}
	
	@Test
	public void enableDisableAPropertyIfTransactionFails() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		CountProperty count = network.getCount();
		EnableIfCountEquals5 property = network.getEnableIfCountEquals5();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(5);
		}
		assertThat(property.IsEnabled()).isTrue();
		
		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				count.setValue(20);
			}
		});
		assertThat(property.IsEnabled()).isTrue();
	}
	
	@Test
	public void executeInTransaction() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		CountProperty count = network.getCount();
		NameProperty name = network.getName();
		try (AutoTransaction t = network.startTransaction()) {
			count.setValue(1);
			name.setValue("Fish");
		};
		assertThat(network.hasPendingTransaction()).isFalse();
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				count.setValue(2);
				name.setValue("D'oh");
			}
		});
		assertThat(count.getValue()).isEqualTo(1);
		assertThat(name.getValue()).isEqualTo("Fish");
		assertThat(network.hasPendingTransaction()).isFalse();
	}
	
	@Test
	public void interDependenceTest() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		InterdependentProperty prop1 = network.getInterdependentProperty();
		Interdependent2Property prop2 = network.getInterdependent2Property();
		try (AutoTransaction t = network.startTransaction()) {
			prop1.setValue(5);
			prop2.setValue(-5);
		}
		assertThat(prop1.getValue() + prop2.getValue()).isEqualTo(0);
	}
	
	@Test
	public void minMaxProperty() throws CheckFailedException {
		ExampleNetwork network = new ExampleNetwork();
		BoundedDoubleProperty property = network.getBoundedDoubleProperty();
		assertThat(property.getMin()).isEqualTo(-1.0);
		assertThat(property.getMax()).isEqualTo(1.0);
		try (AutoTransaction t = network.startTransaction()) {
			property.setValue(0.5);
		}
		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				property.setValue(2.0);
			}
		});
		
		assertThat(property.getValue()).isEqualTo(0.5);

		assertThatThrownBy(() -> {
			try (AutoTransaction t = network.startTransaction()) {
				property.setValue(-1.01);
			}
		});
		assertThat(property.getValue()).isEqualTo(0.5);
	}
}
