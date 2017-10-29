package com.github.liebharc.rsettings.mutable;

import com.github.liebharc.rsettings.immutable.ReadSetting;

public interface RegisterMut {
	StateProvider add(ReadSetting<?> setting);
	void complete();
}
