package com.github.liebharc.rsettings.mutable;

public interface Register {
	StateProvider register(ReadSettingMut<?> setting);
	void complete();
}
