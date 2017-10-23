package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.ReadOnlySetting;

public class ReadProperty<T> {
	private ReadOnlySetting<T> setting;
	protected CurrentSettingState state;

	ReadProperty(CurrentSettingState state, ReadOnlySetting<T> setting) {
		this.state = state;
		this.setting = setting;
	}
	
	public T getValue() {
		return state.get().get(setting);
	}
}
