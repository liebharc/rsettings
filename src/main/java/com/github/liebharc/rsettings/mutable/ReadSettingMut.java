package com.github.liebharc.rsettings.mutable;

import org.apache.commons.math3.exception.NullArgumentException;

import com.github.liebharc.rsettings.events.Event;
import com.github.liebharc.rsettings.events.EventPublisher;
import com.github.liebharc.rsettings.immutable.ReadSetting;
import com.github.liebharc.rsettings.immutable.State;

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
	
	void updateState(State state) throws ConflictingUpdatesException {
		this.state.set(state);
	}
	
	protected State getState() {
		return this.state.get();
	}
	
	public Event<T> getValueChangedEvent() {
		return valueChangedEvent.getEvent();
	}
}
