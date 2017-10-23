package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.CheckFailedException;
import com.github.liebharc.rsettings.immutable.Setting;
import com.github.liebharc.rsettings.immutable.SettingState;

public class ReadWriteProperty<T> extends ReadProperty<T> {

	private Setting<T> writeSetting;

	ReadWriteProperty(CurrentSettingState state, Setting<T> setting) {
		super(state, setting);
		writeSetting = setting;
	}

	public void setValue(T value) throws CheckFailedException {
		SettingState newState = 
				state.get().change().
				set(writeSetting, value).build();
		state.set(newState);
	}
	
	public void reset() throws CheckFailedException {
		setValue(writeSetting.getDefaultValue());
	}
}
