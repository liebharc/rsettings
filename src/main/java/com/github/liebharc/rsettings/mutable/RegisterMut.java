package com.github.liebharc.rsettings.mutable;

public interface RegisterMut {
	StateProvider add(ReadSettingMut<?> setting);
	void complete();
}
