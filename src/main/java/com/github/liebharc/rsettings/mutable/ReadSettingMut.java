package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.ReadSetting;

public abstract class ReadSettingMut<T> extends ReadSetting<T> {

	private CurrentSettingState state;
	
	public ReadSettingMut(T defaultValue) {
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
