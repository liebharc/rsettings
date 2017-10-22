package com.github.liebharc.rsettings;

public interface MinMaxLimited<T extends Comparable<T>> {

	T getMin(SettingState state);
	
	T getMax(SettingState state);
}
