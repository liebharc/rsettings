package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.Reject;
import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

public abstract class ReadSettingMut<T> extends ReadSetting<T> {

	private final EventPublisher<T> valueChangedEvent = new EventPublisher<>(); 
	
	private final StateProvider state;
	
	public ReadSettingMut(RegisterMut register, T defaultValue, Dependencies dependencies) {
		super(defaultValue, dependencies);
		Reject.ifNull(register);
		state = register.add(this);
		state.getStateChangedEvent().subscribe((newState) -> {
			if (newState.getChanges().contains(this)) {
				T newValue = newState.get(this);
				valueChangedEvent.raise(newValue);
			}
		});
	}

	public T getValue() {
		return state.get().get(this);
	}
	
	void updateState(State state) throws CheckFailedException {
		this.state.set(state);
	}
	
	protected State getState() {	
		return state.get();
	}
	
	/**
	 * Return the given state or if that is null the default state. 
	 * @param state A state.
	 * @return The given state or the default state.
	 */
	protected State getState(State state) {
		if (state != null) {
			return state;
		}
		
		return getState();
	}
	
	public Event<T> getValueChangedEvent() {
		return valueChangedEvent.getEvent();
	}
	
	@Override
	public boolean shouldBeStored() {
		return false;
	}
}
