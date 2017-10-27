package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException; 
import com.github.liebharc.rsettings.events.Event;
import com.github.liebharc.rsettings.immutable.*;
import java.util.*;

/**
 * The mutable version of the state is built on top of the immutable version.
 * The immutable version already solved the major issues which need to be addressed to get
 * a consistent state.
 */
public class StateMut {
	public class Builder {
		
		private final State.Builder builder;
		
		private Builder() {
			builder = state.get().change();
		}
		
		public 
			<TValue, 
			TSetting extends ReadSettingMut<TValue> & WriteableSetting<TValue>> 
				Builder set(TSetting setting, TValue value) {
			builder.set(setting, value);
			return this;
		}

		public 
			<TValue,
			 TConvertible extends CanConvertTo<TValue>,
			TSetting extends ReadSetting<TValue> & WriteableSetting<TValue>> 
				Builder set(TSetting setting, TConvertible value) {
			builder.set(setting, value);
			return this;
		}
		
		public void execute() throws CheckFailedException {
			State newState = builder.build();
			state.set(newState);
		}
	}
	
	private class RegisterMut implements Register {

		private final List<ReadSetting<?>> settings = new ArrayList<>();
		
		@Override
		public StateProvider register(ReadSettingMut<?> setting) {
			settings.add(setting);
			return state;
		}

		@Override
		public void complete() {
			state.reinitialize(new State(settings));
		}
		
	}
	
	private final CurrentStateProvider state;
	
	private final Register register = new RegisterMut();
	
	public StateMut() {
		state = new CurrentStateProvider(new State());
	}
	
	public Builder startTransaction() {
		return new Builder();
	}
	
	public State getImmutableState() {
		return state.get();
	}

	public Collection<ReadSetting<?>> listSettings() {
		return state.get().listSettings();
	}
	
	public Event<State> getStateChangedEvent() {
		return state.getStateChangedEvent();
	}
	
	protected Register getRegister() {
		return register;
	}
}
