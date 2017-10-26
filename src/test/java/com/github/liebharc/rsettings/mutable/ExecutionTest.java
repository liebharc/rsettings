package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.*; 

import java.util.Optional;

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.*;

public class ExecutionTest {

	public static class Settings extends StateMut {
		
		private static class IntegerSetting extends ReadWriteSettingMut<Integer> {

			public IntegerSetting() {
				super(0, NoDependencies());
			}
			
			@Override
			protected Optional<Integer> update(State state) throws CheckFailedException {
				if (state.get(this) < 0) {
					throw new CheckFailedException("Value must be positive");
				}
				
				return super.update(state);
			}
		}
		
		private static class ProductSetting extends ReadSettingMut<Integer> {
			private ReadWriteSettingMut<Integer> a;
			private ReadWriteSettingMut<Integer> b;

			public ProductSetting(ReadWriteSettingMut<Integer> a, ReadWriteSettingMut<Integer> b) {
				super(0, Dependencies(a, b));
				this.a = a;
				this.b = b;
			}
			
			@Override
			protected Optional<Integer> update(State state) throws CheckFailedException {
				return Optional.of(state.get(a) * state.get(b));
			}
		}
		
		private ReadWriteSettingMut<Integer> left;
		
		private ReadWriteSettingMut<Integer> right;
		
		private ReadSettingMut<Integer> product;
		
		public Settings() {
			left = register(new IntegerSetting());
			right = register(new IntegerSetting());
			product = register(new ProductSetting(left, right));
		}
	}
	
	/**
	 * At some point something should likely happen if the state is changed. The purpose
	 * of this class is to illustrate that this can be done just by reacting to the 
	 * state change event. What then happens with the state is part of the application domain and
	 * not dictated by the state, since the state is only responsible to provide a consistent set of
	 * values.
	 * 
	 * This example could be solely implemented as part of the state itself, but for illustration
	 * it has been implemented outside of it.
	 */
	public static class ExecutionModel {
		
		private int sum = 0;
		private int callCount = 0;
		private Settings settings;
		
		public ExecutionModel(Settings settings) {
			this.settings = settings;
		}
		
		private void onStateChange(State state) {
			sum = state.get(settings.left) + state.get(settings.right);
			callCount++;
		}

		public int getSum() {
			return sum;
		}

		public int getCallCount() {
			return callCount;
		}
	}
	
	@Test
	public void execution() throws CheckFailedException {
		Settings settings = new Settings();
		ExecutionModel actions = new ExecutionModel(settings);
		settings.getStateChangedEvent().subscribe((state) -> actions.onStateChange(state));
		assertThat(settings.product.getValue()).isEqualTo(0);
		assertThat(actions.getSum()).isEqualTo(0);
		assertThat(actions.getCallCount()).isEqualTo(0);
		
		settings.left.setValue(5);
		assertThat(settings.product.getValue()).isEqualTo(0);
		assertThat(actions.getSum()).isEqualTo(5);
		assertThat(actions.getCallCount()).isEqualTo(1);
		
		settings.right.setValue(3);
		assertThat(settings.product.getValue()).isEqualTo(15);
		assertThat(actions.getSum()).isEqualTo(8);
		assertThat(actions.getCallCount()).isEqualTo(2);
		
		settings.left.setValue(1);
		assertThat(settings.product.getValue()).isEqualTo(3);
		assertThat(actions.getSum()).isEqualTo(4);
		assertThat(actions.getCallCount()).isEqualTo(3);

		// No changes are executed until a transaction completes
		StateMut.Builder transaction = settings.startTransaction()
			.set(settings.left, 3)
			.set(settings.right, 4);
		assertThat(settings.product.getValue()).isEqualTo(3);
		assertThat(actions.getSum()).isEqualTo(4);
		assertThat(actions.getCallCount()).isEqualTo(3);
		
		// Transactions may overlap
		settings.startTransaction()
				.set(settings.left, 6)
				.set(settings.right, 4)
				.execute();
		assertThat(settings.product.getValue()).isEqualTo(24);
		assertThat(actions.getSum()).isEqualTo(10);
		assertThat(actions.getCallCount()).isEqualTo(4);
		
		transaction.execute();
		assertThat(settings.product.getValue()).isEqualTo(12);
		assertThat(actions.getSum()).isEqualTo(7);
		assertThat(actions.getCallCount()).isEqualTo(5);
		
		// Errors leave the state untouched
		assertThatThrownBy(()-> settings.left.setValue(-1));
		assertThat(settings.product.getValue()).isEqualTo(12);
		assertThat(actions.getSum()).isEqualTo(7);
		assertThat(actions.getCallCount()).isEqualTo(5);
	}
}
