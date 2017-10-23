package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.*;
import java.util.*;

public class SettingStateMut {
	private final CurrentSettingState state;
	
	public SettingStateMut() {
		state = new CurrentSettingState(SettingState.FromSettings());
	}
	
	public <TValue, TSetting extends ReadSettingMut<TValue>> TSetting register(TSetting setting) {
		addToState(setting);
		return setting;
	}
	
	private void addToState(ReadSettingMut<?> setting) {
		List<ReadSetting<?>> allSettings = new ArrayList<>(state.get().listSettings());
		allSettings.add(setting);
		SettingState newState = SettingState.FromSettings(allSettings);
		state.set(newState);
		setting.setState(state);
	}
	
	public int getNumberOfSettings() {
		return state.get().getNumberOfSettings();
	}
}
