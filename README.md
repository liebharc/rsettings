A sandbox area to try out a few code patterns for settings, not intended to be really used at some point. If someone looks for a good library for reactive settings there is this promising project: https://github.com/cpdevoto/reactive-properties

The implementation is based on an immutable state where each update to the state gives a new state. Updates to a state are done with a builder pattern which allows to combine several changes
into one. Consistency of the state therefore only needs to be checked every time a builder builds a new state. If a state is inconsistent then the builder can just throw an exception. 
Because the previous version of the state will always still exist (because it has never been changed) rollbacks don't need to be implemented. In the end changes to the state appear to be atomic.

On top of the immutable implementation there is also a mutable wrapper. This mutable state adds some additional convenience methods, some events and a thread safe access
to the immutable state.

```java
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
    * A simple example of code which reacts to setting changes. It will always
    * receive a consistent set of settings.
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
```