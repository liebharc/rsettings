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
				super(0);
			}
			
			@Override
			protected Optional<Integer> update(State state) throws CheckFailedException {
				if (state.get(this) < 0) {
					throw new CheckFailedException("Value must be positive");
				}
				
				return super.update(state);
			}
		}
		
		private ReadWriteSettingMut<Integer> left;
		
		private ReadWriteSettingMut<Integer> right;
		
		public Settings() {
			left = register(new IntegerSetting());
			right = register(new IntegerSetting());
		}

		public ReadWriteSettingMut<Integer> getLeft() {
			return left;
		}

		public ReadWriteSettingMut<Integer> getRight() {
			return right;
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
			sum = state.get(settings.getLeft()) + state.get(settings.getRight());
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
		assertThat(actions.getSum()).isEqualTo(0);
		assertThat(actions.getCallCount()).isEqualTo(0);
		
		settings.getLeft().setValue(5);
		assertThat(actions.getSum()).isEqualTo(5);
		assertThat(actions.getCallCount()).isEqualTo(1);
		
		settings.getRight().setValue(3);
		assertThat(actions.getSum()).isEqualTo(8);
		assertThat(actions.getCallCount()).isEqualTo(2);
		
		settings.getLeft().setValue(1);
		assertThat(actions.getSum()).isEqualTo(4);
		assertThat(actions.getCallCount()).isEqualTo(3);

		// No changes are executed until a transaction completes
		StateMut.Builder transaction = settings.startTransaction()
			.set(settings.getLeft(), 3)
			.set(settings.getRight(), 4);
		assertThat(actions.getSum()).isEqualTo(4);
		assertThat(actions.getCallCount()).isEqualTo(3);
		
		// Transactions may overlap
		settings.startTransaction()
				.set(settings.getLeft(), 6)
				.set(settings.getRight(), 4)
				.complete();;
		assertThat(actions.getSum()).isEqualTo(10);
		assertThat(actions.getCallCount()).isEqualTo(4);
		
		transaction.complete();
		assertThat(actions.getSum()).isEqualTo(7);
		assertThat(actions.getCallCount()).isEqualTo(5);
		
		// Errors leave the state untouched
		assertThatThrownBy(()-> settings.getLeft().setValue(-1));
		assertThat(actions.getSum()).isEqualTo(7);
		assertThat(actions.getCallCount()).isEqualTo(5);
	}
}
