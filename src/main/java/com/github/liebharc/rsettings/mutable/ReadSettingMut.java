package com.github.liebharc.rsettings.mutable;

import org.apache.commons.math3.exception.NullArgumentException;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.events.*;
import com.github.liebharc.rsettings.immutable.*;

public abstract class ReadSettingMut<T> extends ReadSetting<T> {

	private EventPublisher<T> valueChangedEvent = new EventPublisher<>(); 
	
	private CurrentSettingState state;
	
	public ReadSettingMut(T defaultValue, ReadSettingMut<?>... dependencies) {
		super(defaultValue, dependencies);
	}

	public T getValue() {
		return state.get().get(this);
	}

	void setState(CurrentSettingState state) {
		if (state == null) {
			throw new NullArgumentException();
		}
		
		this.state = state;
		state.getStateChangedEvent().subscribe((newState) -> {
			if (newState.getLastChanges().contains(this)) {
				T newValue = newState.get(this);
				valueChangedEvent.raise(newValue);
			}
		});
	}
	
	void updateState(State state) throws CheckFailedException {
		this.state.set(state);
	}
	
	protected State getState() {
		return this.state.get();
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
}
