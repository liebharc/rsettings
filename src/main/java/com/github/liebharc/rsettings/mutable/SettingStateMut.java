package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.ReadOnlySetting;

public class SettingStateMut {
	public <TValue, TSetting extends ReadOnlySetting<TValue>> TSetting register(TSetting setting) {
		return setting;
	}
}
