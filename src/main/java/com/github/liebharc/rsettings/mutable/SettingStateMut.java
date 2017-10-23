package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.*;
import java.util.*;

public class SettingStateMut {
	private final CurrentSettingState state;
	
	public SettingStateMut() {
		state = new CurrentSettingState(SettingState.FromSettings());
	}
	
	public <TValue, TSetting extends ReadOnlySetting<TValue>> TSetting register(TSetting setting) {
		List<ReadOnlySetting<?>> allSettings = new ArrayList<>(state.get().listSettings());
		allSettings.add(setting);
		SettingState newState = SettingState.FromSettings(allSettings);
		state.set(newState);
		return setting;
	}
	
	public int getNumberOfSettings() {
		return state.get().getNumberOfSettings();
	}
}
