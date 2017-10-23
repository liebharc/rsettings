package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.ReadOnlySetting;

public abstract class ReadProperty<T> extends ReadOnlySetting<T> {

	private CurrentSettingState state;
	
	public ReadProperty(T defaultValue) {
		super(defaultValue);
	}

	public T getValue() {
		return state.get().get(this);
	}

	void setState(CurrentSettingState state) {
		this.state = state;
	}
	
	protected CurrentSettingState getState() {
		return this.state;
	}
}
