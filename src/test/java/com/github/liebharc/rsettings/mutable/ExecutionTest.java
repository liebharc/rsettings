package com.github.liebharc.rsettings.mutable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.*;

public class ExecutionTest {

	public static class Settings extends StateMut {
		
		private static class IntegerSetting extends ReadWriteSettingMut<Integer> {

			public IntegerSetting() {
				super(0);
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
		private Settings settings;
		
		public ExecutionModel(Settings settings) {
			this.settings = settings;
		}
		
		private void onStateChange(State state) {
			sum = state.get(settings.getLeft()) + state.get(settings.getRight());
		}

		public int getSum() {
			return sum;
		}
	}
	
	@Test
	public void execution() throws CheckFailedException {
		Settings settings = new Settings();
		ExecutionModel actions = new ExecutionModel(settings);
		settings.getStateChangedEvent().subscribe((state) -> actions.onStateChange(state));
		assertThat(actions.getSum()).isEqualTo(0);
		settings.getLeft().setValue(5);
		assertThat(actions.getSum()).isEqualTo(5);
		settings.getRight().setValue(3);
		assertThat(actions.getSum()).isEqualTo(8);
		settings.getLeft().setValue(-2);
		assertThat(actions.getSum()).isEqualTo(1);
		settings.startTransaction()
			.set(settings.getLeft(), 3)
			.set(settings.getRight(), 4)
			.complete();
		assertThat(actions.getSum()).isEqualTo(7);
	}
}
